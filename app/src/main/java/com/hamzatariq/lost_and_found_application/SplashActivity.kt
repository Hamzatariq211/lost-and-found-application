package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import com.hamzatariq.lost_and_found_application.api.ApiConfig

class SplashActivity : AppCompatActivity() {

    private val splashTimeOut: Long = 2000 // 2 seconds

    companion object {
        private const val TAG = "SplashActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Auto-detect environment (emulator vs physical device)
        ApiConfig.autoDetectEnvironment()
        Log.d(TAG, "API Base URL: ${ApiConfig.getBaseUrl()}")

        // Hide the action bar for splash screen
        supportActionBar?.hide()

        // Check if user is already logged in
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, splashTimeOut)
    }

    private fun checkLoginStatus() {
        val sharedPref = getSharedPreferences("lost_and_found_prefs", MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 0)
        val isLoggedIn = sharedPref.getBoolean("is_logged_in", false)

        if (isLoggedIn && userId > 0) {
            // User is logged in, get FCM token and go to home
            Log.d(TAG, "User already logged in, userId: $userId")

            // Get FCM token and update on server
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(TAG, "FCM Token: $token")

                    // Save token locally
                    sharedPref.edit().putString("fcm_token", token).apply()

                    // Send to server
                    updateFCMToken(userId, token)
                }
            }

            // Navigate to Home
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            // User not logged in, go to login screen
            Log.d(TAG, "User not logged in, going to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun updateFCMToken(userId: Int, token: String) {
        Thread {
            try {
                val url = java.net.URL(ApiConfig.getUrl(ApiConfig.Endpoints.UPDATE_FCM_TOKEN))
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val postData = "user_id=$userId&fcm_token=$token"
                connection.outputStream.write(postData.toByteArray())

                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "FCM token updated on server: $response")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating FCM token: ${e.message}")
            }
        }.start()
    }
}
