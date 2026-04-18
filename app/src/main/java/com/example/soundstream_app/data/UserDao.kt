package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.soundstream_app.model.UserEntity

@Dao
interface UserDao {
    // Sử dụng REPLACE để khi đăng ký lại hoặc cập nhật dữ liệu mẫu (Data Seeding), thông tin cũ sẽ được ghi đè
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Dùng cho logic Login (Xác thực) [cite: 5, 8]
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUser(username: String): UserEntity?

    // Dùng cho SessionManager để kiểm tra phiên làm việc qua Token [cite: 4, 8]
    @Query("SELECT * FROM users WHERE token = :token LIMIT 1")
    suspend fun getUserByToken(token: String): UserEntity?

    // Kiểm tra xem database đã có dữ liệu mẫu chưa (Người 4)
    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Query("SELECT COUNT(*) FROM users WHERE username != :username")
    suspend fun countOtherUsers(username: String): Int

    @Query("DELETE FROM users")
    suspend fun clearAll()
}