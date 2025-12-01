package com.hamzatariq.lost_and_found_application.repository

import android.content.Context
import android.util.Log
import com.hamzatariq.lost_and_found_application.api.ApiService
import com.hamzatariq.lost_and_found_application.api.CreatePostRequest
import com.hamzatariq.lost_and_found_application.api.Post
import com.hamzatariq.lost_and_found_application.api.PostData
import com.hamzatariq.lost_and_found_application.firebase.DataSyncService
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager

class PostRepository(
    private val apiService: ApiService,
    private val prefsManager: SharedPreferencesManager,
    private val context: Context
) {

    private val dataSyncService = DataSyncService(context)

    sealed class PostResult {
        data class Success(val postData: PostData) : PostResult()
        data class Error(val message: String) : PostResult()
        object Loading : PostResult()
    }

    sealed class PostsListResult {
        data class Success(val posts: List<Post>) : PostsListResult()
        data class Error(val message: String) : PostsListResult()
        object Loading : PostsListResult()
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

                    PostResult.Success(postData)
                } else {
                    PostResult.Error("Invalid response data")
                }
            } else {
                PostResult.Error(response.body()?.message ?: "Failed to create post")
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Create post error: ${e.message}")
            PostResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    suspend fun getPosts(
        itemType: String? = null,
        searchQuery: String? = null
    ): PostsListResult {
        return try {
            val response = apiService.getPosts(
                itemType = itemType,
                search = searchQuery,
                limit = 50,
                offset = 0
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val posts = response.body()?.data ?: emptyList()
                PostsListResult.Success(posts)
            } else {
                PostsListResult.Error("Failed to fetch posts")
            }
        } catch (e: Exception) {
            Log.e("PostRepository", "Get posts error: ${e.message}")
            PostsListResult.Error(e.message ?: "Unknown error occurred")
        }
    }
}
