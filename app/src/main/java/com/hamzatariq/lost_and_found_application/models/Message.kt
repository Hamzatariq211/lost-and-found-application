package com.hamzatariq.lost_and_found_application.models

data class Message(
    val message_id: Int,
    val room_id: Int,
    val sender_id: Int,
    val sender_name: String,
    val message_text: String,
    val message_type: String = "text",
    val is_read: Boolean = false,
    val created_at: String
)

