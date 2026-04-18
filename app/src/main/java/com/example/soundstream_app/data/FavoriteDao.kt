package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soundstream_app.model.FavoriteSongEntity

@Dao
interface FavoriteDao {
    // Thêm bài hát vào danh sách yêu thích [cite: 1]
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteSongEntity)

    // Xóa bài hát khỏi danh sách yêu thích của một người dùng cụ thể [cite: 1]
    @Query("DELETE FROM favorite_songs WHERE username = :username AND songId = :songId")
    suspend fun removeFavorite(username: String, songId: String)

    // Lấy danh sách ID các bài hát đã thích để hiển thị icon trái tim [cite: 1]
    @Query("SELECT songId FROM favorite_songs WHERE username = :username")
    suspend fun getFavoriteSongIds(username: String): List<String>

    // Xóa toàn bộ yêu thích khi người dùng yêu cầu xóa tài khoản hoặc cache [cite: 1]
    @Query("DELETE FROM favorite_songs WHERE username = :username")
    suspend fun clearFavoritesForUser(username: String)
}