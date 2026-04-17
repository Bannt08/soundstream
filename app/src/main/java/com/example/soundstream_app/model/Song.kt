package com.example.soundstream_app.model

data class Song(
    val id: String,
    val title: String,
    val artistName: String,
    val imageResId: Int,
    val duration: String,
    val rawResId: Int? = null,
    val sourceUri: String? = null
)
