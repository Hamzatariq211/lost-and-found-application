package com.hamzatariq.lost_and_found_application.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.hamzatariq.lost_and_found_application.api.ApiService
import com.hamzatariq.lost_and_found_application.api.CreatePostRequest
import com.hamzatariq.lost_and_found_application.api.Post
import com.hamzatariq.lost_and_found_application.api.PostData
import com.hamzatariq.lost_and_found_application.database.AppDatabase
import com.hamzatariq.lost_and_found_application.firebase.DataSyncService
import com.hamzatariq.lost_and_found_application.sync.OfflineSyncService
import com.hamzatariq.lost_and_found_application.sync.DataSyncWorker
import com.hamzatariq.lost_and_found_application.utils.NetworkUtils
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager

class PostRepository(
    private val apiService: ApiService,
    private val prefsManager: SharedPreferencesManager,
    private val context: Context
) {

    private val dataSyncService = DataSyncService(context)
    private val offlineSyncService = OfflineSyncService(context)
    private val networkUtils = NetworkUtils(context)
    private val database = AppDatabase.getDatabase(context)

    sealed class PostResult {
        data class Success(val postData: PostData) : PostResult()
        data class Error(val message: String) : PostResult()
        object Loading : PostResult()
    }

    sealed class PostsListResult {
        data class Success(val posts: List<Post>, val fromCache: Boolean = false) : PostsListResult()
        data class Error(val message: String) : PostsListResult()
        object Loading : PostsListResult()
    }

    // Get posts as LiveData from cache
    fun getPostsLiveData(itemType: String? = null): LiveData<List<Post>> {
        val postsLiveData = if (itemType != null) {
            database.postDao().getPostsByType(itemType)
        } else {
            database.postDao().getAllPosts()
        }

        return postsLiveData.map { entities ->
            entities.map { entity ->
                Post(
                    post_id = entity.post_id,
                    item_name = entity.item_name,
                    item_description = entity.item_description,
                    location = entity.location,
                    item_type = entity.item_type,
                    image_base64 = entity.image_base64,
                    status = entity.status,
                    created_at = entity.created_at,
                    user = com.hamzatariq.lost_and_found_application.api.PostUser(
                        user_id = entity.user_id,
                        username = entity.username,
                        full_name = entity.full_name,
                        mobile_number = entity.mobile_number
                    )
                )
            }
        }
    }

    suspend fun createPost(
        itemName: String,
        itemDescription: String,
        location: String,
        itemType: String,
        imageBase64: String?
    ): PostResult {
        return try {
            val token = prefsManager.getAuthToken()
            if (token.isNullOrEmpty()) {
                return PostResult.Error("Authentication required")
            }

            // Check network availability
            if (!networkUtils.isNetworkAvailable()) {
                // Save to pending posts for later sync
                val pendingId = offlineSyncService.savePendingPost(
                    itemName, itemDescription, location, itemType, imageBase64
                )

                if (pendingId > 0) {
                    Log.d("PostRepository", "Post saved offline, will sync when online")
                    // Schedule background sync
                    DataSyncWorker.scheduleSync(context)
                    return PostResult.Success(
                        PostData(
                            post_id = pendingId.toInt(),
                            item_name = itemName,
                            item_type = itemType,
                            location = location,
                            image_url = null
                        )
                    )
                } else {
                    return PostResult.Error("Failed to save post offline")
                }
            }

            val request = CreatePostRequest(
                item_name = itemName,
                item_description = itemDescription,
                location = location,
                item_type = itemType,
                item_image_base64 = imageBase64
            )

            val response = apiService.createPost("Bearer $token", request)

            if (response.isSuccessful && response.body()?.success == true) {
                val postData = response.body()?.data
                if (postData != null) {
                    // Sync posts to Firebase after successful creation
                    val userId = prefsManager.getUserId()
                    if (userId != -1) {
                        dataSyncService.syncPostsToFirebase(userId)
                        Log.d("PostRepository", "Post synced to Firebase")
                    }

                    // Trigger immediate cache refresh
                    DataSyncWorker.syncNow(context)

                    PostResult.Success(postData)
                } else {
                    PostResult.Error("Invalid response data")
                }
            } else {
                PostResult.Error(response.body()?.message ?: "Failed to create post")
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Create post error: ${e.message}")

            // Try saving offline as fallback
            val pendingId = offlineSyncService.savePendingPost(
                itemName, itemDescription, location, itemType, imageBase64
            )

            if (pendingId > 0) {
                DataSyncWorker.scheduleSync(context)
                PostResult.Success(
                    PostData(
                        post_id = pendingId.toInt(),
                        item_name = itemName,
                        item_type = itemType,
                        location = location,
                        image_url = null
                    )
                )
            } else {
                PostResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    suspend fun getPosts(
        itemType: String? = null,
        searchQuery: String? = null
    ): PostsListResult {
        return try {
            // Try to fetch from network if available
            if (networkUtils.isNetworkAvailable()) {
                val response = apiService.getPosts(
                    itemType = itemType,
                    search = searchQuery,
                    limit = 100,
                    offset = 0
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val posts = response.body()?.data ?: emptyList()

                    // Cache the posts for offline access
                    offlineSyncService.cachePosts(posts)

                    PostsListResult.Success(posts, fromCache = false)
                } else {
                    // Fallback to cache
                    val cachedPosts = offlineSyncService.getCachedPosts(itemType)
                    if (cachedPosts.isNotEmpty()) {
                        PostsListResult.Success(cachedPosts, fromCache = true)
                    } else {
                        PostsListResult.Error("Failed to fetch posts")
                    }
                }
            } else {
                // No network, use cache
                Log.d("PostRepository", "No network, using cached posts")
                val cachedPosts = offlineSyncService.getCachedPosts(itemType)
                if (cachedPosts.isNotEmpty()) {
                    PostsListResult.Success(cachedPosts, fromCache = true)
                } else {
                    PostsListResult.Error("No internet connection and no cached data available")
                }
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Get posts error: ${e.message}")

            // Try cache as fallback
            val cachedPosts = offlineSyncService.getCachedPosts(itemType)
            if (cachedPosts.isNotEmpty()) {
                PostsListResult.Success(cachedPosts, fromCache = true)
            } else {
                PostsListResult.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    // Force sync with server
    suspend fun syncWithServer() {
        if (networkUtils.isNetworkAvailable()) {
            DataSyncWorker.syncNow(context)
        }
    }
}
