package com.hamzatariq.lost_and_found_application.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hamzatariq.lost_and_found_application.database.entities.ChatRoomEntity

@Dao
interface ChatRoomDao {

    @Query("SELECT * FROM chat_rooms ORDER BY last_message_time DESC")
    fun getAllChatRooms(): LiveData<List<ChatRoomEntity>>

    @Query("SELECT * FROM chat_rooms WHERE room_id = :roomId")
    suspend fun getChatRoomById(roomId: Int): ChatRoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(chatRoom: ChatRoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRooms(chatRooms: List<ChatRoomEntity>)

    @Update
    suspend fun updateChatRoom(chatRoom: ChatRoomEntity)

    @Delete
    suspend fun deleteChatRoom(chatRoom: ChatRoomEntity)

    @Query("DELETE FROM chat_rooms")
    suspend fun deleteAllChatRooms()

    @Query("SELECT * FROM chat_rooms WHERE is_synced = 0")
    suspend fun getUnsyncedChatRooms(): List<ChatRoomEntity>

    @Query("UPDATE chat_rooms SET unread_count = 0 WHERE room_id = :roomId")
    suspend fun markRoomAsRead(roomId: Int)
}

