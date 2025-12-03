package com.hamzatariq.lost_and_found_application.sync

import android.content.Context
import android.util.Log
import com.hamzatariq.lost_and_found_application.api.ApiService
import com.hamzatariq.lost_and_found_application.api.CreatePostRequest
import com.hamzatariq.lost_and_found_application.api.Post
import com.hamzatariq.lost_and_found_application.database.AppDatabase
import com.hamzatariq.lost_and_found_application.database.entities.*
import com.hamzatariq.lost_and_found_application.models.ChatRoom
import com.hamzatariq.lost_and_found_application.models.Message
import com.hamzatariq.lost_and_found_application.utils.NetworkUtils
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OfflineSyncService(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val prefsManager = SharedPreferencesManager(context)
    private val networkUtils = NetworkUtils(context)

    companion object {
        private const val TAG = "OfflineSyncService"
    }

    // ==================== POST OPERATIONS ====================

    suspend fun cachePost(post: Post) {
        withContext(Dispatchers.IO) {
            try {
                val postEntity = PostEntity(
                    post_id = post.post_id,
                    item_name = post.item_name,
                    item_description = post.item_description,
                    location = post.location,
                    item_type = post.item_type,
                    image_base64 = post.image_base64,
                    status = post.status,
                    created_at = post.created_at,
                    user_id = post.user.user_id,
                    username = post.user.username,
                    full_name = post.user.full_name,
                    mobile_number = post.user.mobile_number,
                    is_synced = true
                )
                database.postDao().insertPost(postEntity)
                Log.d(TAG, "Post cached: ${post.post_id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error caching post: ${e.message}")
            }
        }
    }

    suspend fun cachePosts(posts: List<Post>) {
        withContext(Dispatchers.IO) {
            try {
                val postEntities = posts.map { post ->
                    PostEntity(
                        post_id = post.post_id,
                        item_name = post.item_name,
                        item_description = post.item_description,
                        location = post.location,
                        item_type = post.item_type,
                        image_base64 = post.image_base64,
                        status = post.status,
                        created_at = post.created_at,
                        user_id = post.user.user_id,
                        username = post.user.username,
                        full_name = post.user.full_name,
                        mobile_number = post.user.mobile_number,
                        is_synced = true
                    )
                }
                database.postDao().insertPosts(postEntities)
                Log.d(TAG, "Cached ${posts.size} posts")
            } catch (e: Exception) {
                Log.e(TAG, "Error caching posts: ${e.message}")
            }
        }
    }

    suspend fun getCachedPosts(itemType: String? = null): List<Post> {
        return withContext(Dispatchers.IO) {
            try {
                val entities = if (itemType != null) {
                    database.postDao().getPostsByType(itemType).value ?: emptyList()
                } else {
                    database.postDao().getAllPosts().value ?: emptyList()
                }

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
            } catch (e: Exception) {
                Log.e(TAG, "Error getting cached posts: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun savePendingPost(
        itemName: String,
        itemDescription: String,
        location: String,
        itemType: String,
        imageBase64: String?
    ): Long {
        return withContext(Dispatchers.IO) {
            try {
                val userId = prefsManager.getUserId()
                val pendingPost = PendingPostEntity(
                    item_name = itemName,
                    item_description = itemDescription,
                    location = location,
                    item_type = itemType,
                    image_base64 = imageBase64,
                    user_id = userId
                )
                database.pendingPostDao().insertPendingPost(pendingPost)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving pending post: ${e.message}")
                -1L
            }
        }
    }

    suspend fun syncPendingPosts(apiService: ApiService): Int {
        return withContext(Dispatchers.IO) {
            var syncedCount = 0
            try {
                val pendingPosts = database.pendingPostDao().getAllPendingPosts()
                val token = prefsManager.getAuthToken()

                if (token.isNullOrEmpty()) {
                    Log.e(TAG, "No auth token available")
                    return@withContext 0
                }

                for (pending in pendingPosts) {
                    try {
                        val request = CreatePostRequest(
                            item_name = pending.item_name,
                            item_description = pending.item_description,
                            location = pending.location,
                            item_type = pending.item_type,
                            item_image_base64 = pending.image_base64
                        )

                        val response = apiService.createPost("Bearer $token", request)

                        if (response.isSuccessful && response.body()?.success == true) {
                            database.pendingPostDao().deletePendingPost(pending)
                            syncedCount++
                            Log.d(TAG, "Synced pending post: ${pending.item_name}")
                        } else {
                            // Update retry count
                            val updatedPending = pending.copy(retry_count = pending.retry_count + 1)
                            database.pendingPostDao().updatePendingPost(updatedPending)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error syncing pending post: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in syncPendingPosts: ${e.message}")
            }
            syncedCount
        }
    }

    // ==================== CHAT OPERATIONS ====================

    suspend fun cacheChatRooms(chatRooms: List<ChatRoom>) {
        withContext(Dispatchers.IO) {
            try {
                val entities = chatRooms.map { room ->
                    ChatRoomEntity(
                        room_id = room.room_id,
                        post_id = room.post_id,
                        sender_id = room.sender_id,
                        receiver_id = room.receiver_id,
                        item_name = room.item_name,
                        item_image = room.item_image,
                        other_user_name = room.other_user_name,
                        other_user_id = room.other_user_id,
                        last_message = room.last_message,
                        last_message_time = room.last_message_time,
                        unread_count = room.unread_count,
                        created_at = room.created_at,
                        is_synced = true
                    )
                }
                database.chatRoomDao().insertChatRooms(entities)
                Log.d(TAG, "Cached ${chatRooms.size} chat rooms")
            } catch (e: Exception) {
                Log.e(TAG, "Error caching chat rooms: ${e.message}")
            }
        }
    }

    suspend fun getCachedChatRooms(): List<ChatRoom> {
        return withContext(Dispatchers.IO) {
            try {
                val entities = database.chatRoomDao().getAllChatRooms().value ?: emptyList()
                entities.map { entity ->
                    ChatRoom(
                        room_id = entity.room_id,
                        post_id = entity.post_id,
                        sender_id = entity.sender_id,
                        receiver_id = entity.receiver_id,
                        item_name = entity.item_name,
                        item_image = entity.item_image,
                        other_user_name = entity.other_user_name,
                        other_user_id = entity.other_user_id,
                        last_message = entity.last_message,
                        last_message_time = entity.last_message_time,
                        unread_count = entity.unread_count,
                        created_at = entity.created_at
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting cached chat rooms: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun cacheMessages(messages: List<Message>) {
        withContext(Dispatchers.IO) {
            try {
                val entities = messages.map { message ->
                    MessageEntity(
                        message_id = message.message_id,
                        room_id = message.room_id,
                        sender_id = message.sender_id,
                        sender_name = message.sender_name,
                        message_text = message.message_text,
                        message_type = message.message_type,
                        is_read = message.is_read,
                        created_at = message.created_at,
                        is_synced = true
                    )
                }
                database.messageDao().insertMessages(entities)
                Log.d(TAG, "Cached ${messages.size} messages")
            } catch (e: Exception) {
                Log.e(TAG, "Error caching messages: ${e.message}")
            }
        }
    }

    suspend fun getCachedMessages(roomId: Int): List<Message> {
        return withContext(Dispatchers.IO) {
            try {
                val entities = database.messageDao().getMessagesByRoomSync(roomId)
                entities.map { entity ->
                    Message(
                        message_id = entity.message_id,
                        room_id = entity.room_id,
                        sender_id = entity.sender_id,
                        sender_name = entity.sender_name,
                        message_text = entity.message_text,
                        message_type = entity.message_type,
                        is_read = entity.is_read,
                        created_at = entity.created_at
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting cached messages: ${e.message}")
                emptyList()
            }
        }
    }

    suspend fun savePendingMessage(message: Message) {
        withContext(Dispatchers.IO) {
            try {
                val messageEntity = MessageEntity(
                    message_id = message.message_id,
                    room_id = message.room_id,
                    sender_id = message.sender_id,
                    sender_name = message.sender_name,
                    message_text = message.message_text,
                    message_type = message.message_type,
                    is_read = false,
                    created_at = message.created_at,
                    is_synced = false,
                    pending_sync = true
                )
                database.messageDao().insertMessage(messageEntity)
                Log.d(TAG, "Saved pending message")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving pending message: ${e.message}")
            }
        }
    }

    // ==================== UTILITY METHODS ====================

    suspend fun clearAllCache() {
        withContext(Dispatchers.IO) {
            try {
                database.postDao().deleteAllPosts()
                database.chatRoomDao().deleteAllChatRooms()
                Log.d(TAG, "All cache cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing cache: ${e.message}")
            }
        }
    }

    suspend fun getCacheStats(): CacheStats {
        return withContext(Dispatchers.IO) {
            try {
                CacheStats(
                    totalPosts = database.postDao().getPostCount(),
                    pendingPosts = database.pendingPostDao().getPendingPostCount(),
                    totalChatRooms = database.chatRoomDao().getAllChatRooms().value?.size ?: 0
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting cache stats: ${e.message}")
                CacheStats(0, 0, 0)
            }
        }
    }
}

data class CacheStats(
    val totalPosts: Int,
    val pendingPosts: Int,
    val totalChatRooms: Int
)

