package com.hamzatariq.lost_and_found_application.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hamzatariq.lost_and_found_application.ChatsActivity
import com.hamzatariq.lost_and_found_application.HomeActivity
import com.hamzatariq.lost_and_found_application.MyPostsActivity
import com.hamzatariq.lost_and_found_application.R
import com.hamzatariq.lost_and_found_application.api.ApiConfig

class FCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID_CHAT = "chat_notifications"
        private const val CHANNEL_ID_MATCH = "match_notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Save token to SharedPreferences
        val sharedPref = getSharedPreferences("lost_and_found_prefs", Context.MODE_PRIVATE)
        sharedPref.edit().putString("fcm_token", token).apply()

        // Send token to server if user is logged in
        val userId = sharedPref.getInt("user_id", 0)
        if (userId > 0) {
            sendTokenToServer(userId, token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Check if message contains data payload
        message.data.isNotEmpty().let {
            Log.d(TAG, "Message data: ${message.data}")
            handleDataMessage(message.data)
        }

        // Check if message contains notification payload
        message.notification?.let {
            Log.d(TAG, "Message notification: ${it.title} - ${it.body}")
            sendNotification(it.title ?: "", it.body ?: "", "general")
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val type = data["type"] ?: return
        val title = data["title"] ?: "Lost & Found"
        val body = data["body"] ?: ""

        when (type) {
            "chat_message" -> {
                val chatId = data["chat_id"]
                val senderId = data["sender_id"]
                sendNotification(title, body, "chat", chatId, senderId)
            }
            "item_match" -> {
                val lostItemId = data["lost_item_id"]
                val postId = data["post_id"]
                sendNotification(title, body, "match", lostItemId, postId)
            }
            else -> {
                sendNotification(title, body, "general")
            }
        }
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        type: String,
        extraId1: String? = null,
        extraId2: String? = null
    ) {
        val intent = when (type) {
            "chat" -> Intent(this, ChatsActivity::class.java).apply {
                extraId1?.let { putExtra("chat_id", it) }
                extraId2?.let { putExtra("sender_id", it) }
            }
            "match" -> Intent(this, MyPostsActivity::class.java).apply {
                putExtra("tab", "lost_items")
                extraId1?.let { putExtra("lost_item_id", it) }
            }
            else -> Intent(this, HomeActivity::class.java)
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (type == "chat") CHANNEL_ID_CHAT else CHANNEL_ID_MATCH
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chatChannel = NotificationChannel(
                CHANNEL_ID_CHAT,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
            }

            val matchChannel = NotificationChannel(
                CHANNEL_ID_MATCH,
                "Item Matches",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for matching found items"
            }

            notificationManager.createNotificationChannel(chatChannel)
            notificationManager.createNotificationChannel(matchChannel)
        }

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun sendTokenToServer(userId: Int, token: String) {
        Thread {
            try {
                val url = java.net.URL(ApiConfig.getUrl(ApiConfig.Endpoints.UPDATE_FCM_TOKEN))
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                val postData = "user_id=$userId&fcm_token=$token"
                connection.outputStream.write(postData.toByteArray())

                val response = connection.inputStream.bufferedReader().readText()
                Log.d(TAG, "Token sent to server: $response")
            } catch (e: Exception) {
                Log.e(TAG, "Error sending token to server: ${e.message}")
            }
        }.start()
    }
}
