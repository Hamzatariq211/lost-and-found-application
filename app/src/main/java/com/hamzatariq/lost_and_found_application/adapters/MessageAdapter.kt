package com.hamzatariq.lost_and_found_application.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.hamzatariq.lost_and_found_application.R
import com.hamzatariq.lost_and_found_application.models.Message
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(
    private val messages: List<Message>,
    private val currentUserId: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.sentMessageText)
        val timestamp: TextView = itemView.findViewById(R.id.sentTimestamp)

        fun bind(message: Message) {
            messageText.text = message.message_text
            timestamp.text = formatTime(message.created_at)
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.receivedMessageText)
        val timestamp: TextView = itemView.findViewById(R.id.receivedTimestamp)
        val senderName: TextView = itemView.findViewById(R.id.receivedSenderName)

        fun bind(message: Message) {
            messageText.text = message.message_text
            timestamp.text = formatTime(message.created_at)
            senderName.text = message.sender_name
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].sender_id == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SentMessageViewHolder) {
            holder.bind(messages[position])
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(messages[position])
        }
    }

    override fun getItemCount() = messages.size

    private fun formatTime(timestamp: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = sdf.parse(timestamp)
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            ""
        }
    }
}

