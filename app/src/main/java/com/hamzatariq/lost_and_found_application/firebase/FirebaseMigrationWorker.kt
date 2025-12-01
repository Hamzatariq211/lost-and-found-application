package com.hamzatariq.lost_and_found_application.firebase

import android.content.Context
import android.util.Log
import androidx.work.*
import com.google.firebase.Timestamp
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Background worker to migrate all MySQL data to Firebase
 * Run this once to perform initial data migration
 */
class FirebaseMigrationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val firestoreHelper = FirestoreHelper()

    companion object {
        private const val TAG = "FirebaseMigration"
        const val WORK_NAME = "firebase_migration_work"

        fun scheduleInitialMigration(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<FirebaseMigrationWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }

    override suspend fun doWork(): Result {
        return try {
            Log.d(TAG, "Starting Firebase migration...")

            setProgress(workDataOf("status" to "Migrating users..."))
            migrateAllUsers()

            setProgress(workDataOf("status" to "Migrating posts..."))
            migrateAllPosts()

            setProgress(workDataOf("status" to "Migration complete!"))
            Log.d(TAG, "Firebase migration completed successfully")

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Migration failed: ${e.message}", e)
            Result.retry()
        }
    }

    private suspend fun migrateAllUsers() {
        try {
            val url = URL("${ApiConfig.getBaseUrl()}user/get_all_users.php")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            val response = connection.inputStream.bufferedReader().readText()
            val jsonResponse = JSONObject(response)

            if (jsonResponse.getBoolean("success")) {
                val usersArray = jsonResponse.getJSONArray("data")

                for (i in 0 until usersArray.length()) {
                    val user = usersArray.getJSONObject(i)

                    val firebaseUserData = mapOf(
                        "user_id" to user.getInt("user_id"),
                        "full_name" to user.getString("full_name"),
                        "username" to user.getString("username"),
                        "email" to user.getString("email"),
                        "mobile_number" to user.optString("mobile_number", ""),
                        "fcm_token" to user.optString("fcm_token", ""),
                        "is_active" to user.optBoolean("is_active", true),
                        "created_at" to Timestamp.now(),
                        "updated_at" to Timestamp.now()
                    )

                    firestoreHelper.createUser(firebaseUserData)
                    Log.d(TAG, "Migrated user: ${user.getString("username")}")
                }

                Log.d(TAG, "Total users migrated: ${usersArray.length()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating users: ${e.message}", e)
            // Continue with migration even if this fails
        }
    }

    private suspend fun migrateAllPosts() {
        try {
            val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.GET_LOST_ITEMS))
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

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

                    // Add small delay to avoid overwhelming Firestore
                    if (i % 10 == 0) {
                        kotlinx.coroutines.delay(500)
                    }
                }

                Log.d(TAG, "Total posts migrated: ${postsArray.length()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error migrating posts: ${e.message}", e)
        }
    }
}
