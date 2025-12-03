package com.hamzatariq.lost_and_found_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_rooms")
data class ChatRoomEntity(
    @PrimaryKey
    val room_id: Int,
    val post_id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val item_name: String,
    val item_image: String?,
    val other_user_name: String,
    val other_user_id: Int,
    val last_message: String?,
    val last_message_time: String?,
    val unread_count: Int,
    val created_at: String,
    val is_synced: Boolean = true,
    val last_updated: Long = System.currentTimeMillis()
)

