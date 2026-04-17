package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.soundstream_app.model.PlaylistEntity
import com.example.soundstream_app.model.PlaylistSongCrossRef
import com.example.soundstream_app.model.PlaylistWithSongs

@Dao
interface PlaylistDao {
    // Tạo playlist mới [cite: 1]
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    // Thêm bài hát vào một playlist cụ thể (Bảng trung gian) [cite: 1]
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(ref: PlaylistSongCrossRef)

    // Xóa tất cả liên kết bài hát của một playlist trước khi xóa playlist đó [cite: 1]
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun deletePlaylistCrossRefs(playlistId: String)

    // Xóa playlist [cite: 1]
    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    // Cập nhật tên hoặc mô tả playlist [cite: 1]
    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    // Lấy chi tiết một playlist kèm theo danh sách các bài hát bên trong [cite: 1]
    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistWithSongs(playlistId: String): PlaylistWithSongs?

    // Lấy tất cả playlist của một người dùng cụ thể [cite: 1]
    @Transaction
    @Query("SELECT * FROM playlists WHERE ownerUsername = :username")
    suspend fun getPlaylistsForUser(username: String): List<PlaylistWithSongs>

    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylists(): List<PlaylistEntity>
}