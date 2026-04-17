package com.example.soundstream_app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.soundstream_app.model.PlayHistoryEntity

@Dao
interface PlayHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: PlayHistoryEntity)

    @Query("SELECT * FROM play_history WHERE username = :username ORDER BY timestamp DESC LIMIT 30")
    suspend fun getRecentHistory(username: String): List<PlayHistoryEntity>

    @Query("DELETE FROM play_history WHERE username = :username")
    suspend fun clearHistory(username: String)

    @Query("DELETE FROM play_history WHERE id NOT IN (SELECT id FROM play_history WHERE username = :username ORDER BY timestamp DESC LIMIT 100)")
    suspend fun trimHistory(username: String)
}