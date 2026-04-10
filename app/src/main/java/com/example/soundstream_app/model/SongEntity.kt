package com.example.soundstream_app.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
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
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistName: String,
    val imageResId: Int,
    val duration: String,
    val isUploaded: Boolean = false,
    val ownerUsername: String? = null
)
