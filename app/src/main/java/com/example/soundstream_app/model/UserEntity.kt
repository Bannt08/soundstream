package com.example.soundstream_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val password: String,
    val displayName: String? = null,
    val isPremium: Boolean = false,
    val isArtist: Boolean = false,
    val isAdmin: Boolean = false,
    val token: String? = null
)
