package com.hamzatariq.lost_and_found_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_posts")
data class PendingPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val item_name: String,
    val item_description: String,
    val location: String,
    val item_type: String,
    val image_base64: String?,
    val user_id: Int,
    val created_at: Long = System.currentTimeMillis(),
    val retry_count: Int = 0
)

