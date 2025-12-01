package com.hamzatariq.lost_and_found_application

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.hamzatariq.lost_and_found_application.adapters.ChatRoomAdapter
import com.hamzatariq.lost_and_found_application.api.ApiConfig
import com.hamzatariq.lost_and_found_application.models.ChatRoom
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ChatsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var emptyView: LinearLayout
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private val chatRooms = mutableListOf<ChatRoom>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chats)

        setupViews()
        loadChatRooms()
    }

    override fun onResume() {
        super.onResume()
        // Refresh chats when returning to this activity
        loadChatRooms()
    }

    private fun setupViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Messages"
        toolbar.setNavigationOnClickListener { finish() }

        recyclerView = findViewById(R.id.chatsRecyclerView)
        progressBar = findViewById(R.id.progressBar)
        emptyView = findViewById(R.id.emptyView)

        chatRoomAdapter = ChatRoomAdapter(chatRooms) { chatRoom ->
            openChatScreen(chatRoom)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatsActivity)
            adapter = chatRoomAdapter
        }
    }

    private fun loadChatRooms() {
        progressBar.visibility = View.VISIBLE
        emptyView.visibility = View.GONE

        Thread {
            try {
                val sharedPref = getSharedPreferences("lost_and_found_prefs", MODE_PRIVATE)
                val userId = sharedPref.getInt("user_id", 0)

                android.util.Log.d("ChatDebug", "Loading chat rooms for user ID: $userId")

                if (userId == 0) {
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        emptyView.visibility = View.VISIBLE
                        Toast.makeText(this, "Please log in again", Toast.LENGTH_LONG).show()
                    }
                    return@Thread
                }

                val url = URL(ApiConfig.getUrl(ApiConfig.Endpoints.CHAT_API) + "?action=get_rooms&user_id=$userId")
                android.util.Log.d("ChatDebug", "URL: ${url.toString()}")

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                val responseCode = connection.responseCode
                android.util.Log.d("ChatDebug", "Response Code: $responseCode")

                val response = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream?.bufferedReader()?.readText() ?: "No error message"
                }

                android.util.Log.d("ChatDebug", "Response: $response")

                val jsonResponse = JSONObject(response)

                runOnUiThread {
                    progressBar.visibility = View.GONE

                    if (jsonResponse.getBoolean("success")) {
                        chatRooms.clear()
                        val roomsArray = jsonResponse.getJSONArray("rooms")

                        android.util.Log.d("ChatDebug", "Found ${roomsArray.length()} chat rooms")

                        for (i in 0 until roomsArray.length()) {
                            val room = roomsArray.getJSONObject(i)
                            chatRooms.add(
                                ChatRoom(
                                    room_id = room.getInt("room_id"),
                                    post_id = room.getInt("post_id"),
                                    sender_id = room.getInt("sender_id"),
                                    receiver_id = room.getInt("receiver_id"),
                                    item_name = room.getString("item_name"),
                                    item_image = room.optString("item_image"),
                                    other_user_name = room.getString("other_user_name"),
                                    other_user_id = room.getInt("other_user_id"),
                                    last_message = room.optString("last_message"),
                                    last_message_time = room.optString("last_message_time"),
                                    unread_count = room.getInt("unread_count"),
                                    created_at = room.getString("created_at")
                                )
                            )
                        }

                        if (chatRooms.isEmpty()) {
                            emptyView.visibility = View.VISIBLE
                            android.util.Log.d("ChatDebug", "No chat rooms found")
                        }

                        chatRoomAdapter.notifyDataSetChanged()
                    } else {
                        val errorMsg = jsonResponse.optString("message", "Unknown error")
                        android.util.Log.e("ChatDebug", "API Error: $errorMsg")
                        Toast.makeText(this, "Failed to load chats: $errorMsg", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatDebug", "Exception in loadChatRooms", e)
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    e.printStackTrace()
                }
            }
        }.start()
    }

    private fun openChatScreen(chatRoom: ChatRoom) {
        val intent = Intent(this, ChatScreenActivity::class.java).apply {
            putExtra("room_id", chatRoom.room_id)
            putExtra("other_user_name", chatRoom.other_user_name)
            putExtra("item_name", chatRoom.item_name)
        }
        startActivity(intent)
    }
}
