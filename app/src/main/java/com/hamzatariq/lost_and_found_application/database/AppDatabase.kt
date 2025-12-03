package com.hamzatariq.lost_and_found_application.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hamzatariq.lost_and_found_application.database.dao.*
import com.hamzatariq.lost_and_found_application.database.entities.*

@Database(
    entities = [
        PostEntity::class,
        ChatRoomEntity::class,
        MessageEntity::class,
        UserEntity::class,
        PendingPostEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao
    abstract fun chatRoomDao(): ChatRoomDao
    abstract fun messageDao(): MessageDao
    abstract fun userDao(): UserDao
    abstract fun pendingPostDao(): PendingPostDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lost_and_found_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

