package com.example.soundstream_app.model

import com.example.soundstream_app.R

data class User(
    val username: String,
    val displayName: String = username,
    val isPremium: Boolean = false,
    var isArtist: Boolean = false,
    val token: String? = null,
    val isGuest: Boolean = false,
    val avatarResId: Int = R.drawable.uth
)
