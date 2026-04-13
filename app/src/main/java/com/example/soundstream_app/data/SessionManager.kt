package com.example.soundstream_app.data

import android.content.Context
import com.example.soundstream_app.R
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.model.User
import kotlinx.coroutines.runBlocking

object SessionManager {
    private const val PREFS_NAME = "soundstream_session"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_IS_GUEST = "session_is_guest"

    var currentUser: User? = null
    private val uploadedSongs = mutableListOf<Song>()

    val authToken: String?
        get() = currentUser?.token

    val isLoggedIn: Boolean
        get() = currentUser != null && !currentUser!!.isGuest

    val isGuest: Boolean
        get() = currentUser?.isGuest == true

    val hasActiveSession: Boolean
        get() = currentUser != null

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun login(context: Context, username: String, password: String): Boolean {
        val userEntity = AppDatabase.getInstance(context).userDao().authenticate(username, password)
        return if (userEntity != null) {
            currentUser = User(
                username = userEntity.username,
                displayName = userEntity.displayName ?: userEntity.username,
                isPremium = userEntity.isPremium,
                isArtist = userEntity.isArtist,
                token = userEntity.token,
                isGuest = false
            )
            saveSession(context)
            uploadedSongs.clear()
            true
        } else {
            false
        }
    }

    suspend fun loginWithToken(context: Context, token: String): Boolean {
        val userEntity = AppDatabase.getInstance(context).userDao().getUserByToken(token)
        return if (userEntity != null) {
            currentUser = User(
                username = userEntity.username,
                displayName = userEntity.displayName ?: userEntity.username,
                isPremium = userEntity.isPremium,
                isArtist = userEntity.isArtist,
                token = userEntity.token,
                isGuest = false
            )
            saveSession(context)
            uploadedSongs.clear()
            true
        } else {
            false
        }
    }

    fun loginAsGuest(context: Context): Boolean {
        currentUser = User(
            username = "guest",
            displayName = context.getString(R.string.guest_name),
            isPremium = false,
            isArtist = false,
            token = null,
            isGuest = true
        )
        saveGuestSession(context)
        uploadedSongs.clear()
        return true
    }

    fun restoreSession(context: Context): Boolean {
        val storedPrefs = prefs(context)
        val isGuestSession = storedPrefs.getBoolean(KEY_IS_GUEST, false)
        if (isGuestSession) {
            currentUser = User(
                username = "guest",
                displayName = context.getString(R.string.guest_name),
                isPremium = false,
                isArtist = false,
                token = null,
                isGuest = true
            )
            uploadedSongs.clear()
            return true
        }

        val token = storedPrefs.getString(KEY_SESSION_TOKEN, null)
        return if (!token.isNullOrBlank()) {
            runBlocking {
                loginWithToken(context, token)
            }
        } else {
            false
        }
    }

    fun logout(context: Context) {
        currentUser = null
        uploadedSongs.clear()
        prefs(context).edit().clear().apply()
    }

    private fun saveSession(context: Context) {
        prefs(context).edit()
            .putString(KEY_SESSION_TOKEN, currentUser?.token)
            .putBoolean(KEY_IS_GUEST, false)
            .apply()
    }

    private fun saveGuestSession(context: Context) {
        prefs(context).edit()
            .remove(KEY_SESSION_TOKEN)
            .putBoolean(KEY_IS_GUEST, true)
            .apply()
    }

    fun addUploadedSong(song: Song) {
        uploadedSongs.add(song)
        currentUser?.isArtist = true
    }

    fun getUploadedSongs(): List<Song> = uploadedSongs.toList()
}
