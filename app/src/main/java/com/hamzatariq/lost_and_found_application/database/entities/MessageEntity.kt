package com.hamzatariq.lost_and_found_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val message_id: Int,
    val room_id: Int,
    val sender_id: Int,
    val sender_name: String,
    val message_text: String,
    val message_type: String = "text",
    val is_read: Boolean = false,
    val created_at: String,
    val is_synced: Boolean = true,
    val pending_sync: Boolean = false,
    val last_updated: Long = System.currentTimeMillis()
)

