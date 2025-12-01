package com.hamzatariq.lost_and_found_application

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.hamzatariq.lost_and_found_application.adapters.MessageAdapter
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import com.hamzatariq.lost_and_found_application.models.Message
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class ChatScreenActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var messageAdapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private var roomId = 0
    private var userId = 0
    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 3000L // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_screen)

        roomId = intent.getIntExtra("room_id", 0)
        val otherUserName = intent.getStringExtra("other_user_name") ?: ""
        val itemName = intent.getStringExtra("item_name") ?: ""

        val sharedPref = getSharedPreferences("lost_and_found_prefs", MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", 0)

        android.util.Log.d("ChatDebug", "ChatScreen - User ID: $userId, Room ID: $roomId")

        if (userId == 0) {
            Toast.makeText(this, "Please log in again", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setupViews(otherUserName, itemName)
        loadMessages()
        startAutoRefresh()
    }

    private fun setupViews(otherUserName: String, itemName: String) {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.title = otherUserName
        toolbar.subtitle = itemName
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        messageAdapter = MessageAdapter(messages, userId)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatScreenActivity).apply {
                stackFromEnd = true // Start from bottom
            }
            adapter = messageAdapter
            // Smooth scroll when keyboard opens
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom && messages.isNotEmpty()) {
                    recyclerView.postDelayed({
                        recyclerView.smoothScrollToPosition(messages.size - 1)
                    }, 100)
                }
            }
        }

        sendButton.setOnClickListener {
            sendMessage()
        }
    }

    private fun loadMessages() {
        Thread {
            try {
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.CHAT_API) + "?action=get_messages&room_id=$roomId")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    if (jsonResponse.getBoolean("success")) {
                        val oldSize = messages.size
                        messages.clear()
                        val messagesArray = jsonResponse.getJSONArray("messages")

                        for (i in 0 until messagesArray.length()) {
                            val msg = messagesArray.getJSONObject(i)
                            messages.add(
                                Message(
                                    message_id = msg.getInt("message_id"),
                                    room_id = msg.getInt("room_id"),
                                    sender_id = msg.getInt("sender_id"),
                                    sender_name = msg.getString("sender_name"),
                                    message_text = msg.getString("message_text"),
                                    is_read = msg.getInt("is_read") == 1,
                                    created_at = msg.getString("created_at")
                                )
                            )
                        }

                        messageAdapter.notifyDataSetChanged()
                        if (messages.size > oldSize) {
                            recyclerView.scrollToPosition(messages.size - 1)
                        }

                        // Mark messages as read
                        markMessagesAsRead()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        if (messageText.isEmpty()) return

        messageInput.text.clear()
        sendButton.isEnabled = false

        Thread {
            try {
                val url = URL("http://10.0.2.2/lost_and_found_api/chat/chat_api.php")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val postData = "action=send_message&room_id=$roomId&sender_id=$userId&message_text=${URLEncoder.encode(messageText, "UTF-8")}"
                connection.outputStream.write(postData.toByteArray())

                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    sendButton.isEnabled = true
                    if (jsonResponse.getBoolean("success")) {
                        loadMessages()
                    } else {
                        Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    sendButton.isEnabled = true
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun markMessagesAsRead() {
        Thread {
            try {
                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.CHAT_API))
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val postData = "action=mark_read&room_id=$roomId&user_id=$userId"
                connection.outputStream.write(postData.toByteArray())
                connection.inputStream.bufferedReader().readText()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun startAutoRefresh() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                loadMessages()
                handler.postDelayed(this, refreshInterval)
            }
        }, refreshInterval)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}
