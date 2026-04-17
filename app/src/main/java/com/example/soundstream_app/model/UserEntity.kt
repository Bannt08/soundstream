package com.example.soundstream_app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.soundstream_app.R

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val passwordSalt: String,
    val displayName: String? = null,
    val isPremium: Boolean = false,
    val isArtist: Boolean = false,
    val isAdmin: Boolean = false,
    val token: String? = null,
    val avatarResId: Int = R.drawable.uth
)
