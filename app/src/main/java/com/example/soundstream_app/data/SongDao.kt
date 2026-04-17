package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soundstream_app.model.SongEntity

@Dao
interface SongDao {
    // Thêm hoặc cập nhật bài hát (Dùng cho cả Seeding dữ liệu và Upload nhạc mới)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    // Xóa bài hát theo ID (Người 3 dùng để quản lý nhạc đã upload)
    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    // Lấy toàn bộ danh sách nhạc hiện có trong hệ thống, sắp xếp theo tên (Người 2)
    @Query("SELECT * FROM songs ORDER BY title ASC")
    suspend fun getAllSongs(): List<SongEntity>

    // Lấy nhạc do một User cụ thể upload (Người 3)
    @Query("SELECT * FROM songs WHERE ownerUsername = :username ORDER BY id DESC")
    suspend fun getSongsByOwner(username: String): List<SongEntity>

    // Lấy toàn bộ danh sách nhạc được cộng đồng upload (Người 3/Người 2)
    @Query("SELECT * FROM songs WHERE isUploaded = 1")
    suspend fun getUploadedSongs(): List<SongEntity>

    // Lấy thông tin chi tiết một bài hát (Người 5 dùng cho History/Playlist)
    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: String): SongEntity?

    // Tìm kiếm bài hát theo tên hoặc nghệ sĩ (Người 2)
    // Tối ưu hóa việc tìm kiếm không phân biệt chữ hoa chữ thường (nếu SQLite hỗ trợ)
    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artistName LIKE '%' || :query || '%'")
    suspend fun searchSongs(query: String): List<SongEntity>

    // Lấy danh sách nhiều bài hát từ tập hợp ID (Dùng cho Playlist và Recent History - Người 5)
    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIds(ids: List<String>): List<SongEntity>

    // Đếm số lượng bài hát hiện có để kiểm tra việc khởi tạo dữ liệu mẫu (Người 4)
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun countSongs(): Int
}