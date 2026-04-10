package com.example.soundstream_app.model

data class User(
    val username: String,
    val isPremium: Boolean = false,
    var isArtist: Boolean = false
)
