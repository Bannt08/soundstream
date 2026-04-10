package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soundstream_app.model.PlayHistoryEntity

@Dao
interface PlayHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayHistory(history: PlayHistoryEntity)

    @Query("SELECT * FROM play_history WHERE username = :username ORDER BY playedAt DESC")
    suspend fun getHistoryForUser(username: String): List<PlayHistoryEntity>

    @Query("DELETE FROM play_history WHERE username = :username")
    suspend fun deleteHistoryForUser(username: String)
}
