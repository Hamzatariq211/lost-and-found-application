package com.hamzatariq.lost_and_found_application.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val user_id: Int,
    val username: String,
    val email: String,
    val full_name: String,
    val mobile_number: String?,
    val token: String?,
    val is_synced: Boolean = true,
    val last_updated: Long = System.currentTimeMillis()
)

