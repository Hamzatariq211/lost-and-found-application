package com.hamzatariq.lost_and_found_application.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hamzatariq.lost_and_found_application.database.entities.MessageEntity

@Dao
interface MessageDao {

    @Query("SELECT * FROM messages WHERE room_id = :roomId ORDER BY created_at ASC")
    fun getMessagesByRoom(roomId: Int): LiveData<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE room_id = :roomId ORDER BY created_at ASC")
    suspend fun getMessagesByRoomSync(roomId: Int): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("DELETE FROM messages WHERE room_id = :roomId")
    suspend fun deleteMessagesByRoom(roomId: Int)

    @Query("SELECT * FROM messages WHERE pending_sync = 1")
    suspend fun getPendingMessages(): List<MessageEntity>

    @Query("UPDATE messages SET is_synced = 1, pending_sync = 0 WHERE message_id = :messageId")
    suspend fun markMessageAsSynced(messageId: Int)

    @Query("SELECT COUNT(*) FROM messages WHERE room_id = :roomId AND is_read = 0")
    suspend fun getUnreadCount(roomId: Int): Int
}

