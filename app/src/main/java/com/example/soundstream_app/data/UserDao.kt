package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.soundstream_app.model.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE token = :token LIMIT 1")
    suspend fun getUserByToken(token: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE username != :username")
    suspend fun countOtherUsers(username: String): Int

    @Query("DELETE FROM users")
    suspend fun clearAll()
}
