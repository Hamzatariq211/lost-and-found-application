package com.hamzatariq.lost_and_found_application.database.dao

import androidx.room.*
import com.hamzatariq.lost_and_found_application.database.entities.PendingPostEntity

@Dao
interface PendingPostDao {

    @Query("SELECT * FROM pending_posts ORDER BY created_at ASC")
    suspend fun getAllPendingPosts(): List<PendingPostEntity>

    @Insert
    suspend fun insertPendingPost(post: PendingPostEntity): Long

    @Delete
    suspend fun deletePendingPost(post: PendingPostEntity)

    @Update
    suspend fun updatePendingPost(post: PendingPostEntity)

    @Query("DELETE FROM pending_posts WHERE id = :id")
    suspend fun deletePendingPostById(id: Int)

    @Query("SELECT COUNT(*) FROM pending_posts")
    suspend fun getPendingPostCount(): Int
}

