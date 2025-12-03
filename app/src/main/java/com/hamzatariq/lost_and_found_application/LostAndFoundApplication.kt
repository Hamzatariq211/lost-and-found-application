package com.hamzatariq.lost_and_found_application

import android.app.Application
import android.util.Log
import com.hamzatariq.lost_and_found_application.sync.DataSyncWorker

class LostAndFoundApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Schedule periodic background sync
        DataSyncWorker.scheduleSync(this)

        Log.d("LostAndFoundApp", "Application started, sync scheduled")
    }
}

