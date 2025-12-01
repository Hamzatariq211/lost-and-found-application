package com.hamzatariq.lost_and_found_application.adapters

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hamzatariq.lost_and_found_application.R
import com.hamzatariq.lost_and_found_application.models.ChatRoom
import java.text.SimpleDateFormat
import java.util.*

class ChatRoomAdapter(
    private val chatRooms: List<ChatRoom>,
    private val onChatClick: (ChatRoom) -> Unit
) : RecyclerView.Adapter<ChatRoomAdapter.ChatRoomViewHolder>() {

    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemImage: ImageView = itemView.findViewById(R.id.chatItemImage)
        val userName: TextView = itemView.findViewById(R.id.chatUserName)
        val itemName: TextView = itemView.findViewById(R.id.chatItemName)
        val lastMessage: TextView = itemView.findViewById(R.id.chatLastMessage)
        val timestamp: TextView = itemView.findViewById(R.id.chatTimestamp)
        val unreadBadge: TextView = itemView.findViewById(R.id.unreadBadge)

        fun bind(chatRoom: ChatRoom) {
            userName.text = chatRoom.other_user_name
            itemName.text = "Re: ${chatRoom.item_name}"
            lastMessage.text = chatRoom.last_message ?: "No messages yet"

            // Format timestamp
            if (chatRoom.last_message_time != null) {
                timestamp.text = formatTimestamp(chatRoom.last_message_time)
            } else {
                timestamp.text = ""
            }

            // Show unread badge
            if (chatRoom.unread_count > 0) {
                unreadBadge.visibility = View.VISIBLE
                unreadBadge.text = if (chatRoom.unread_count > 99) "99+" else chatRoom.unread_count.toString()
            } else {
                unreadBadge.visibility = View.GONE
            }

            // Load item image (base64 or placeholder)
            if (!chatRoom.item_image.isNullOrEmpty()) {
                try {
                    val base64String = if (chatRoom.item_image.contains("base64,")) {
                        chatRoom.item_image.substring(chatRoom.item_image.indexOf("base64,") + 7)
                    } else {
                        chatRoom.item_image
                    }
                    val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    itemImage.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    itemImage.setImageResource(R.drawable.ic_profile)
                }
            } else {
                itemImage.setImageResource(R.drawable.ic_profile)
            }

            itemView.setOnClickListener {
                onChatClick(chatRoom)
            }
        }

        private fun formatTimestamp(timestamp: String): String {
            return try {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = sdf.parse(timestamp)
                val now = Date()
                val diff = now.time - (date?.time ?: 0)

                when {
                    diff < 60000 -> "Just now"
                    diff < 3600000 -> "${diff / 60000}m ago"
                    diff < 86400000 -> "${diff / 3600000}h ago"
                    else -> SimpleDateFormat("MMM dd", Locale.getDefault()).format(date)
                }
            } catch (e: Exception) {
                ""
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_room, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        holder.bind(chatRooms[position])
    }

    override fun getItemCount() = chatRooms.size
}

