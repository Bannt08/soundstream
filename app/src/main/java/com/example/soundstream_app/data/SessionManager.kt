package com.example.soundstream_app.data

import android.content.Context
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.model.User
import com.example.soundstream_app.data.AppDatabase

object SessionManager {
    var currentUser: User = User("", false, false)
    private val uploadedSongs = mutableListOf<Song>()

    suspend fun login(context: Context, username: String, password: String): Boolean {
        val userEntity = AppDatabase.getInstance(context).userDao().authenticate(username, password)
        return if (userEntity != null) {
            currentUser = User(
                username = userEntity.username,
                isPremium = userEntity.isPremium,
                isArtist = userEntity.isArtist
            )
            uploadedSongs.clear()
            true
        } else {
            false
        }
    }

    fun logout() {
        currentUser = User("", false, false)
        uploadedSongs.clear()
    }

    fun addUploadedSong(song: Song) {
        uploadedSongs.add(song)
        currentUser.isArtist = true
    }

    fun getUploadedSongs(): List<Song> = uploadedSongs
}
