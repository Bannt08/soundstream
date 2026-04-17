package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soundstream_app.model.SongEntity

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)

    @Query("DELETE FROM songs WHERE id = :id")
    suspend fun deleteSong(id: String)

    @Query("SELECT * FROM songs")
    suspend fun getAllSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE ownerUsername = :username")
    suspend fun getSongsByOwner(username: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE isUploaded = 1")
    suspend fun getUploadedSongs(): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: String): SongEntity?

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artistName LIKE '%' || :query || '%'")
    suspend fun searchSongs(query: String): List<SongEntity>

    @Query("SELECT * FROM songs WHERE id IN (:ids)")
    suspend fun getSongsByIds(ids: List<String>): List<SongEntity>
}
