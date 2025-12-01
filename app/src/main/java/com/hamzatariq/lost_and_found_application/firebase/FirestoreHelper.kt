package com.hamzatariq.lost_and_found_application.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

/**
 * Firebase Firestore Helper Class
 * Mirrors MySQL database structure in Firestore
 */
class FirestoreHelper {

    private val db = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirestoreHelper"

        // Collection names matching MySQL tables
        const val COLLECTION_USERS = "users"
        const val COLLECTION_POSTS = "posts"
        const val COLLECTION_NOTIFICATIONS = "notifications"
        const val COLLECTION_USER_SESSIONS = "user_sessions"
        const val COLLECTION_SEARCH_HISTORY = "search_history"
        const val COLLECTION_CHAT_ROOMS = "chat_rooms"
        const val COLLECTION_CHAT_MESSAGES = "chat_messages"
    }

    // ========================================
    // USER OPERATIONS
    // ========================================

    suspend fun createUser(userData: Map<String, Any>): Boolean {
        return try {
            val userId = userData["user_id"].toString()
            db.collection(COLLECTION_USERS)
                .document(userId)
                .set(userData, SetOptions.merge())
                .await()
            Log.d(TAG, "User created successfully: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user: ${e.message}", e)
            false
        }
    }

    suspend fun getUserProfile(userId: String): Map<String, Any>? {
        return try {
            val document = db.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            if (document.exists()) {
                document.data
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile: ${e.message}", e)
            null
        }
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>): Boolean {
        return try {
            val data = updates.toMutableMap()
            data["updated_at"] = Timestamp.now()

            db.collection(COLLECTION_USERS)
                .document(userId)
                .update(data)
                .await()
            Log.d(TAG, "User profile updated: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile: ${e.message}", e)
            false
        }
    }

    suspend fun updateFCMToken(userId: String, fcmToken: String): Boolean {
        return try {
            db.collection(COLLECTION_USERS)
                .document(userId)
                .update("fcm_token", fcmToken)
                .await()
            Log.d(TAG, "FCM token updated for user: $userId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating FCM token: ${e.message}", e)
            false
        }
    }

    // ========================================
    // POST OPERATIONS
    // ========================================

    suspend fun createPost(postData: Map<String, Any>): String? {
        return try {
            val postRef = db.collection(COLLECTION_POSTS).document()
            val data = postData.toMutableMap()
            data["created_at"] = Timestamp.now()
            data["updated_at"] = Timestamp.now()
            data["sync_status"] = true

            postRef.set(data).await()
            Log.d(TAG, "Post created: ${postRef.id}")
            postRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post: ${e.message}", e)
            null
        }
    }

    suspend fun getPosts(
        itemType: String? = null,
        status: String = "active",
        limit: Int = 50
    ): List<Map<String, Any>> {
        return try {
            var query: Query = db.collection(COLLECTION_POSTS)
                .whereEqualTo("status", status)

            itemType?.let {
                query = query.whereEqualTo("item_type", it)
            }

            val documents = query
                .orderBy("created_at", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            documents.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting posts: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getUserPosts(userId: String, status: String = "active"): List<Map<String, Any>> {
        return try {
            val documents = db.collection(COLLECTION_POSTS)
                .whereEqualTo("user_id", userId)
                .whereEqualTo("status", status)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await()

            documents.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user posts: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun updatePost(postId: String, updates: Map<String, Any>): Boolean {
        return try {
            val data = updates.toMutableMap()
            data["updated_at"] = Timestamp.now()

            db.collection(COLLECTION_POSTS)
                .document(postId)
                .update(data)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating post: ${e.message}", e)
            false
        }
    }

    suspend fun deletePost(postId: String): Boolean {
        return try {
            db.collection(COLLECTION_POSTS)
                .document(postId)
                .update("status", "deleted", "updated_at", Timestamp.now())
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post: ${e.message}", e)
            false
        }
    }

    // ========================================
    // NOTIFICATION OPERATIONS
    // ========================================

    suspend fun createNotification(notificationData: Map<String, Any>): String? {
        return try {
            val notifRef = db.collection(COLLECTION_NOTIFICATIONS).document()
            val data = notificationData.toMutableMap()
            data["created_at"] = Timestamp.now()
            data["is_read"] = false

            notifRef.set(data).await()
            notifRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification: ${e.message}", e)
            null
        }
    }

    suspend fun getUserNotifications(userId: String): List<Map<String, Any>> {
        return try {
            val documents = db.collection(COLLECTION_NOTIFICATIONS)
                .whereEqualTo("user_id", userId)
                .orderBy("created_at", Query.Direction.DESCENDING)
                .get()
                .await()

            documents.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun markNotificationAsRead(notificationId: String): Boolean {
        return try {
            db.collection(COLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("is_read", true)
                .await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read: ${e.message}", e)
            false
        }
    }

    // ========================================
    // CHAT OPERATIONS
    // ========================================

    suspend fun createChatRoom(roomData: Map<String, Any>): String? {
        return try {
            // Check if room already exists
            val existingRoom = db.collection(COLLECTION_CHAT_ROOMS)
                .whereEqualTo("post_id", roomData["post_id"])
                .whereEqualTo("sender_id", roomData["sender_id"])
                .whereEqualTo("receiver_id", roomData["receiver_id"])
                .get()
                .await()

            if (!existingRoom.isEmpty) {
                return existingRoom.documents[0].id
            }

            val roomRef = db.collection(COLLECTION_CHAT_ROOMS).document()
            val data = roomData.toMutableMap()
            data["created_at"] = Timestamp.now()
            data["updated_at"] = Timestamp.now()
            data["is_active"] = true

            roomRef.set(data).await()
            roomRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error creating chat room: ${e.message}", e)
            null
        }
    }

    suspend fun getUserChatRooms(userId: String): List<Map<String, Any>> {
        return try {
            // Get rooms where user is sender
            val senderRooms = db.collection(COLLECTION_CHAT_ROOMS)
                .whereEqualTo("sender_id", userId)
                .whereEqualTo("is_active", true)
                .get()
                .await()

            // Get rooms where user is receiver
            val receiverRooms = db.collection(COLLECTION_CHAT_ROOMS)
                .whereEqualTo("receiver_id", userId)
                .whereEqualTo("is_active", true)
                .get()
                .await()

            val allRooms = mutableListOf<Map<String, Any>>()
            allRooms.addAll(senderRooms.documents.mapNotNull { it.data })
            allRooms.addAll(receiverRooms.documents.mapNotNull { it.data })

            allRooms.sortedByDescending {
                (it["updated_at"] as? Timestamp)?.toDate()?.time ?: 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat rooms: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun sendMessage(messageData: Map<String, Any>): String? {
        return try {
            val messageRef = db.collection(COLLECTION_CHAT_MESSAGES).document()
            val data = messageData.toMutableMap()
            data["created_at"] = Timestamp.now()
            data["is_read"] = false

            messageRef.set(data).await()

            // Update chat room's updated_at
            val roomId = messageData["room_id"] as? String
            roomId?.let {
                db.collection(COLLECTION_CHAT_ROOMS)
                    .document(it)
                    .update(
                        "updated_at", Timestamp.now(),
                        "last_message", messageData["message_text"]
                    )
                    .await()
            }

            messageRef.id
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}", e)
            null
        }
    }

    suspend fun getChatMessages(roomId: String, limit: Int = 100): List<Map<String, Any>> {
        return try {
            val documents = db.collection(COLLECTION_CHAT_MESSAGES)
                .whereEqualTo("room_id", roomId)
                .orderBy("created_at", Query.Direction.ASCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            documents.documents.mapNotNull { it.data }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat messages: ${e.message}", e)
            emptyList()
        }
    }

    fun listenToChatMessages(
        roomId: String,
        onMessagesChanged: (List<Map<String, Any>>) -> Unit
    ) {
        db.collection(COLLECTION_CHAT_MESSAGES)
            .whereEqualTo("room_id", roomId)
            .orderBy("created_at", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to messages: ${error.message}", error)
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull { it.data } ?: emptyList()
                onMessagesChanged(messages)
            }
    }

    // ========================================
    // SEARCH OPERATIONS
    // ========================================

    suspend fun addSearchHistory(userId: String, searchQuery: String, searchType: String): Boolean {
        return try {
            val searchRef = db.collection(COLLECTION_SEARCH_HISTORY).document()
            val data = mapOf(
                "user_id" to userId,
                "search_query" to searchQuery,
                "search_type" to searchType,
                "created_at" to Timestamp.now()
            )

            searchRef.set(data).await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error adding search history: ${e.message}", e)
            false
        }
    }

    suspend fun searchPosts(
        query: String,
        itemType: String? = null,
        status: String = "active"
    ): List<Map<String, Any>> {
        return try {
            // Note: Firestore doesn't support full-text search
            // You might want to use Algolia or similar service for better search
            val allPosts = getPosts(itemType, status, 100)

            allPosts.filter { post ->
                val itemName = post["item_name"] as? String ?: ""
                val description = post["item_description"] as? String ?: ""
                val location = post["location"] as? String ?: ""

                itemName.contains(query, ignoreCase = true) ||
                description.contains(query, ignoreCase = true) ||
                location.contains(query, ignoreCase = true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error searching posts: ${e.message}", e)
            emptyList()
        }
    }
}

