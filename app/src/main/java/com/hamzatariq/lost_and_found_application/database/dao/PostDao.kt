package com.hamzatariq.lost_and_found_application.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hamzatariq.lost_and_found_application.database.entities.PostEntity

@Dao
interface PostDao {

    @Query("SELECT * FROM posts ORDER BY created_at DESC")
    fun getAllPosts(): LiveData<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE item_type = :itemType ORDER BY created_at DESC")
    fun getPostsByType(itemType: String): LiveData<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE post_id = :postId")
    suspend fun getPostById(postId: Int): PostEntity?

    @Query("SELECT * FROM posts WHERE item_name LIKE '%' || :query || '%' OR item_description LIKE '%' || :query || '%'")
    fun searchPosts(query: String): LiveData<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<PostEntity>)

    @Update
    suspend fun updatePost(post: PostEntity)

    @Delete
    suspend fun deletePost(post: PostEntity)

    @Query("DELETE FROM posts")
    suspend fun deleteAllPosts()

    @Query("SELECT * FROM posts WHERE is_synced = 0")
    suspend fun getUnsyncedPosts(): List<PostEntity>

    @Query("UPDATE posts SET is_synced = 1 WHERE post_id = :postId")
    suspend fun markAsSynced(postId: Int)

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun getPostCount(): Int
}

