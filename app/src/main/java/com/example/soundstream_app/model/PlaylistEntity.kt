package com.example.soundstream_app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playlists",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["username"],
            childColumns = ["ownerUsername"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerUsername")]
)
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val imageResId: Int,
    val ownerUsername: String? = null
)
