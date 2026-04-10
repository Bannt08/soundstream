package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soundstream_app.model.FavoriteSongEntity

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavorite(favorite: FavoriteSongEntity)

    @Query("DELETE FROM favorite_songs WHERE username = :username AND songId = :songId")
    suspend fun removeFavorite(username: String, songId: String)

    @Query("SELECT songId FROM favorite_songs WHERE username = :username")
    suspend fun getFavoriteSongIds(username: String): List<String>

    @Query("DELETE FROM favorite_songs WHERE username = :username")
    suspend fun clearFavoritesForUser(username: String)
}
