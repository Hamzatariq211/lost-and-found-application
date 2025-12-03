package com.hamzatariq.lost_and_found_application.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.hamzatariq.lost_and_found_application.database.AppDatabase
import com.hamzatariq.lost_and_found_application.models.ChatRoom
import com.hamzatariq.lost_and_found_application.models.Message
import com.hamzatariq.lost_and_found_application.sync.OfflineSyncService
import com.hamzatariq.lost_and_found_application.utils.NetworkUtils

class ChatRepository(private val context: Context) {

    private val offlineSyncService = OfflineSyncService(context)
    private val networkUtils = NetworkUtils(context)
    private val database = AppDatabase.getDatabase(context)

    companion object {
        private const val TAG = "ChatRepository"
    }

    // Get chat rooms as LiveData from cache
    fun getChatRoomsLiveData(): LiveData<List<ChatRoom>> {
        return database.chatRoomDao().getAllChatRooms().map { entities ->
            entities.map { entity ->
                ChatRoom(
                    room_id = entity.room_id,
                    post_id = entity.post_id,
                    sender_id = entity.sender_id,
                    receiver_id = entity.receiver_id,
                    item_name = entity.item_name,
                    item_image = entity.item_image,
                    other_user_name = entity.other_user_name,
                    other_user_id = entity.other_user_id,
                    last_message = entity.last_message,
                    last_message_time = entity.last_message_time,
                    unread_count = entity.unread_count,
                    created_at = entity.created_at
                )
            }
        }
    }

    // Get messages for a room as LiveData from cache
    fun getMessagesLiveData(roomId: Int): LiveData<List<Message>> {
        return database.messageDao().getMessagesByRoom(roomId).map { entities ->
            entities.map { entity ->
                Message(
                    message_id = entity.message_id,
                    room_id = entity.room_id,
                    sender_id = entity.sender_id,
                    sender_name = entity.sender_name,
                    message_text = entity.message_text,
                    message_type = entity.message_type,
                    is_read = entity.is_read,
                    created_at = entity.created_at
                )
            }
        }
    }

    suspend fun getChatRooms(): List<ChatRoom> {
        return try {
            // Always try to get from cache first for instant display
            val cachedRooms = offlineSyncService.getCachedChatRooms()

            if (networkUtils.isNetworkAvailable()) {
                // TODO: Fetch from API and update cache
                Log.d(TAG, "Network available, could fetch fresh data")
            }

            cachedRooms
        } catch (e: Exception) {
            Log.e(TAG, "Error getting chat rooms: ${e.message}")
            emptyList()
        }
    }

    suspend fun getMessages(roomId: Int): List<Message> {
        return try {
            // Always try to get from cache first for instant display
            val cachedMessages = offlineSyncService.getCachedMessages(roomId)

            if (networkUtils.isNetworkAvailable()) {
                // TODO: Fetch from API and update cache
                Log.d(TAG, "Network available, could fetch fresh messages")
            }

            cachedMessages
        } catch (e: Exception) {
            Log.e(TAG, "Error getting messages: ${e.message}")
            emptyList()
        }
    }

    suspend fun sendMessage(message: Message): Boolean {
        return try {
            if (!networkUtils.isNetworkAvailable()) {
                // Save message locally with pending sync flag
                offlineSyncService.savePendingMessage(message)
                Log.d(TAG, "Message saved offline, will sync when online")
                return true
            }

            // TODO: Send via API
            // For now, just cache it
            offlineSyncService.savePendingMessage(message)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message: ${e.message}")
            false
        }
    }

    suspend fun cacheChatRooms(chatRooms: List<ChatRoom>) {
        offlineSyncService.cacheChatRooms(chatRooms)
    }

    suspend fun cacheMessages(messages: List<Message>) {
        offlineSyncService.cacheMessages(messages)
    }
}

