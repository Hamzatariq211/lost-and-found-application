package com.hamzatariq.lost_and_found_application.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import com.hamzatariq.lost_and_found_application.utils.SharedPreferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Synchronizes data between MySQL (primary) and Firebase Firestore (real-time)
 */
class DataSyncService(private val context: Context) {

    private val firestoreHelper = FirestoreHelper()
    private val prefsManager = SharedPreferencesManager(context)

    companion object {
        private const val TAG = "DataSyncService"
    }

    /**
     * Sync user data from MySQL to Firebase
     */
    fun syncUserToFirebase(userId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_PROFILE) + "?user_id=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000

                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                if (jsonResponse.getBoolean("success")) {
                    val userData = jsonResponse.getJSONObject("data")

                    val firebaseUserData = mapOf(
                        "user_id" to userData.getInt("user_id"),
                        "full_name" to userData.getString("full_name"),
                        "username" to userData.getString("username"),
                        "email" to userData.getString("email"),
                        "mobile_number" to userData.optString("mobile_number", ""),
                        "fcm_token" to (prefsManager.getFCMToken() ?: ""),
                        "is_active" to true,
                        "created_at" to Timestamp.now(),
                        "updated_at" to Timestamp.now(),
                        "post_count" to userData.optInt("post_count", 0)
                    )

                    val success = firestoreHelper.createUser(firebaseUserData)
                    if (success) {
                        Log.d(TAG, "User synced to Firebase: $userId")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing user to Firebase: ${e.message}", e)
            }
        }
    }

    /**
     * Sync all posts from MySQL to Firebase
     */
    fun syncPostsToFirebase(userId: Int? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val endpoint = if (userId != null) {
                    ApiConfig.getUrl(ApiConfig.Endpoints.GET_LOST_ITEMS) + "?user_id=$userId"
                } else {
                    ApiConfig.getUrl(ApiConfig.Endpoints.GET_LOST_ITEMS)
                }

                val url = URL(endpoint)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000

                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                if (jsonResponse.getBoolean("success")) {
                    val postsArray = jsonResponse.getJSONArray("data")

                    for (i in 0 until postsArray.length()) {
                        val post = postsArray.getJSONObject(i)

                        val firebasePostData = mapOf(
                            "post_id" to post.getInt("post_id"),
                            "user_id" to post.getInt("user_id").toString(),
                            "item_name" to post.getString("item_name"),
                            "item_description" to post.getString("item_description"),
                            "location" to post.getString("location"),
                            "item_type" to post.getString("item_type"),
                            "item_image" to post.optString("item_image", ""),
                            "status" to post.optString("status", "active"),
                            "created_at" to Timestamp.now(),
                            "updated_at" to Timestamp.now(),
                            "sync_status" to true
                        )

                        firestoreHelper.createPost(firebasePostData)
                    }

                    Log.d(TAG, "Posts synced to Firebase: ${postsArray.length()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing posts to Firebase: ${e.message}", e)
            }
        }
    }

    /**
     * Sync notifications from MySQL to Firebase
     */
    fun syncNotificationsToFirebase(userId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_NOTIFICATIONS) + "?user_id=$userId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000

                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                if (jsonResponse.getBoolean("success")) {
                    val notificationsArray = jsonResponse.getJSONArray("data")

                    for (i in 0 until notificationsArray.length()) {
                        val notification = notificationsArray.getJSONObject(i)

                        val firebaseNotifData = mapOf(
                            "notification_id" to notification.getInt("notification_id"),
                            "user_id" to userId.toString(),
                            "post_id" to notification.getInt("post_id"),
                            "notification_type" to notification.optString("notification_type", "match"),
                            "title" to notification.getString("title"),
                            "message" to notification.getString("message"),
                            "is_read" to notification.optBoolean("is_read", false),
                            "created_at" to Timestamp.now()
                        )

                        firestoreHelper.createNotification(firebaseNotifData)
                    }

                    Log.d(TAG, "Notifications synced to Firebase: ${notificationsArray.length()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing notifications to Firebase: ${e.message}", e)
            }
        }
    }

    /**
     * Create post in both MySQL and Firebase
     */
    suspend fun createPostDualSync(postData: JSONObject): Boolean {
        return try {
            // First create in MySQL
            val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.CREATE_LOST_ITEM))
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            connection.outputStream.write(postData.toString().toByteArray())

            val response = connection.inputStream.bufferedReader().readText()
            val jsonResponse = JSONObject(response)

            if (jsonResponse.getBoolean("success")) {
                val postId = jsonResponse.optInt("post_id", -1)

                // Then sync to Firebase
                val firebasePostData = mapOf(
                    "post_id" to postId,
                    "user_id" to postData.getInt("user_id").toString(),
                    "item_name" to postData.getString("item_name"),
                    "item_description" to postData.getString("item_description"),
                    "location" to postData.getString("location"),
                    "item_type" to postData.getString("item_type"),
                    "item_image" to postData.optString("item_image", ""),
                    "status" to "active",
                    "created_at" to Timestamp.now(),
                    "updated_at" to Timestamp.now(),
                    "sync_status" to true
                )

                firestoreHelper.createPost(firebasePostData)
                Log.d(TAG, "Post created in both MySQL and Firebase")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post dual sync: ${e.message}", e)
            false
        }
    }

    /**
     * Update FCM token in both MySQL and Firebase
     */
    fun updateFCMTokenDualSync(userId: Int, fcmToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Update in MySQL
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.UPDATE_FCM_TOKEN))
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")

                val jsonPayload = JSONObject().apply {
                    put("user_id", userId)
                    put("fcm_token", fcmToken)
                }

                connection.outputStream.write(jsonPayload.toString().toByteArray())

                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                if (jsonResponse.getBoolean("success")) {
                    // Update in Firebase
                    firestoreHelper.updateFCMToken(userId.toString(), fcmToken)
                    Log.d(TAG, "FCM token updated in both MySQL and Firebase")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating FCM token: ${e.message}", e)
            }
        }
    }

    /**
     * Perform full sync of user data
     */
    fun performFullSync(userId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Starting full sync for user: $userId")

            // Sync user profile
            syncUserToFirebase(userId)

            // Wait a bit
            kotlinx.coroutines.delay(1000)

            // Sync user's posts
            syncPostsToFirebase(userId)

            // Wait a bit
            kotlinx.coroutines.delay(1000)

            // Sync notifications
            syncNotificationsToFirebase(userId)

            Log.d(TAG, "Full sync completed for user: $userId")
        }
    }
}

