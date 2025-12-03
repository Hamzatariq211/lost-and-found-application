package com.hamzatariq.lost_and_found_application.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.hamzatariq.lost_and_found_application.database.entities.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE user_id = :userId")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("SELECT * FROM users WHERE user_id = :userId")
    fun getUserByIdLiveData(userId: Int): LiveData<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("DELETE FROM users WHERE user_id = :userId")
    suspend fun deleteUserById(userId: Int)
}

