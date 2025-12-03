package com.hamzatariq.lost_and_found_application.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.hamzatariq.lost_and_found_application.api.RetrofitClient
import com.hamzatariq.lost_and_found_application.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DataSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val offlineSyncService = OfflineSyncService(context)
    private val networkUtils = NetworkUtils(context)

    companion object {
        private const val TAG = "DataSyncWorker"
        const val WORK_NAME = "data_sync_work"

        fun scheduleSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<DataSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )

            Log.d(TAG, "Periodic sync scheduled")
        }

        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<DataSyncWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "immediate_sync",
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )

            Log.d(TAG, "Immediate sync requested")
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!networkUtils.isNetworkAvailable()) {
                Log.d(TAG, "No network available, skipping sync")
                return@withContext Result.retry()
            }

            Log.d(TAG, "Starting data sync...")

            // Sync pending posts
            val apiService = RetrofitClient.apiService
            val syncedPosts = offlineSyncService.syncPendingPosts(apiService)
            Log.d(TAG, "Synced $syncedPosts pending posts")

            // Fetch and cache latest posts
            try {
                val response = apiService.getPosts(limit = 100, offset = 0)
                if (response.isSuccessful && response.body()?.success == true) {
                    val posts = response.body()?.data ?: emptyList()
                    offlineSyncService.cachePosts(posts)
                    Log.d(TAG, "Cached ${posts.size} posts from server")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching posts: ${e.message}")
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync error: ${e.message}", e)
            Result.retry()
        }
    }
}

