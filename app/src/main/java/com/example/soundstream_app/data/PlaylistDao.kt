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
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addSongToPlaylist(ref: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun deletePlaylistCrossRefs(playlistId: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: String)

    @Update
    suspend fun updatePlaylist(playlist: PlaylistEntity)

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistWithSongs(playlistId: String): PlaylistWithSongs?

    @Transaction
    @Query("SELECT * FROM playlists WHERE ownerUsername = :username")
    suspend fun getPlaylistsForUser(username: String): List<PlaylistWithSongs>

    @Transaction
    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylistsWithSongs(): List<PlaylistWithSongs>

    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylists(): List<PlaylistEntity>
}
