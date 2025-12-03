package com.hamzatariq.lost_and_found_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey
    val post_id: Int,
    val item_name: String,
    val item_description: String,
    val location: String,
    val item_type: String, // "lost" or "found"
    val image_base64: String?,
    val status: String,
    val created_at: String,
    val user_id: Int,
    val username: String,
    val full_name: String,
    val mobile_number: String,
    val is_synced: Boolean = true,
    val last_updated: Long = System.currentTimeMillis()
)

