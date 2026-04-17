package com.example.soundstream_app.data

import android.content.Context
import androidx.core.content.edit
import com.example.soundstream_app.R
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.model.User
import com.example.soundstream_app.model.UserEntity
import com.example.soundstream_app.util.PasswordUtils
import java.util.UUID

@Suppress("unused")
object SessionManager {
    private const val PREFS_NAME = "soundstream_session"
    private const val KEY_SESSION_TOKEN = "session_token"
    private const val KEY_IS_GUEST = "session_is_guest"

    var currentUser: User? = null
    private val uploadedSongs = mutableListOf<Song>()

    val authToken: String?
        get() = currentUser?.token

    val isLoggedIn: Boolean
        get() = currentUser != null && currentUser?.isGuest == false

    val isGuest: Boolean
        get() = currentUser?.isGuest == true

    val hasActiveSession: Boolean
        get() = currentUser != null

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun login(context: Context, username: String, password: String): Boolean {
        AppDatabase.waitForInitialization(context)
        val userEntity = AppDatabase.getInstance(context).userDao().getUser(username)
        return if (userEntity != null && PasswordUtils.verifyPassword(password, userEntity.passwordSalt, userEntity.passwordHash)) {
            currentUser = userEntity.toUser()
            saveSession(context)
            uploadedSongs.clear()
            true
        } else {
            false
        }
    }

    suspend fun loginWithToken(context: Context, token: String): Boolean {
        AppDatabase.waitForInitialization(context)
        val userEntity = AppDatabase.getInstance(context).userDao().getUserByToken(token)
        return if (userEntity != null) {
            currentUser = userEntity.toUser()
            saveSession(context)
            uploadedSongs.clear()
            true
        } else {
            false
        }
    }

    suspend fun restoreSession(context: Context): Boolean {
        val storedPrefs = prefs(context)
        val isGuestSession = storedPrefs.getBoolean(KEY_IS_GUEST, false)
        if (isGuestSession) {
            currentUser = User(
                username = "guest",
                displayName = context.getString(R.string.guest_name),
                isPremium = false,
                isArtist = false,
                token = null,
                isGuest = true,
                avatarResId = R.drawable.uth
            )
            uploadedSongs.clear()
            return true
        }

        val token = storedPrefs.getString(KEY_SESSION_TOKEN, null)
        return if (!token.isNullOrBlank()) {
            try {
                AppDatabase.waitForInitialization(context)
                loginWithToken(context, token)
            } catch (ex: Exception) {
                ex.printStackTrace()
                false
            }
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
            isGuest = true,
            avatarResId = R.drawable.uth
        )
        saveGuestSession(context)
        uploadedSongs.clear()
        return true
    }

    suspend fun register(
        context: Context,
        username: String,
        password: String,
        displayName: String,
        avatarResId: Int = R.drawable.uth
    ): Boolean {
        AppDatabase.waitForInitialization(context)
        val userDao = AppDatabase.getInstance(context).userDao()
        if (userDao.getUser(username) != null) {
            return false
        }

        val salt = PasswordUtils.generateSalt()
        val hash = PasswordUtils.hashPassword(password, salt)
        val token = UUID.randomUUID().toString()

        val userEntity = UserEntity(
            username = username,
            passwordHash = hash,
            passwordSalt = salt,
            displayName = displayName.ifBlank { username },
            isPremium = false,
            isArtist = false,
            isAdmin = false,
            token = token,
            avatarResId = avatarResId
        )

        userDao.insertUser(userEntity)
        currentUser = userEntity.toUser()
        saveSession(context)
        uploadedSongs.clear()
        return true
    }

    suspend fun updateProfile(context: Context, displayName: String, avatarResId: Int): Boolean {
        val user = currentUser ?: return false
        if (user.isGuest) return false

        AppDatabase.waitForInitialization(context)
        val userDao = AppDatabase.getInstance(context).userDao()
        val userEntity = userDao.getUser(user.username) ?: return false

        userDao.updateUser(
            userEntity.copy(
                displayName = displayName.ifBlank { user.username },
                avatarResId = avatarResId
            )
        )

        currentUser = currentUser!!.copy(
            displayName = displayName.ifBlank { user.username },
            avatarResId = avatarResId
        )
        saveSession(context)
        return true
    }

    suspend fun changePassword(context: Context, currentPassword: String, newPassword: String): Boolean {
        val user = currentUser ?: return false
        if (user.isGuest) return false

        AppDatabase.waitForInitialization(context)
        val userDao = AppDatabase.getInstance(context).userDao()
        val userEntity = userDao.getUser(user.username) ?: return false

        if (!PasswordUtils.verifyPassword(currentPassword, userEntity.passwordSalt, userEntity.passwordHash)) {
            return false
        }

        val newSalt = PasswordUtils.generateSalt()
        val newHash = PasswordUtils.hashPassword(newPassword, newSalt)
        userDao.updateUser(userEntity.copy(passwordHash = newHash, passwordSalt = newSalt))
        return true
    }

    suspend fun upgradeToPremium(context: Context): Boolean {
        val user = currentUser ?: return false
        if (user.isGuest || user.isPremium) return false

        AppDatabase.waitForInitialization(context)
        val userDao = AppDatabase.getInstance(context).userDao()
        val userEntity = userDao.getUser(user.username) ?: return false
        userDao.updateUser(userEntity.copy(isPremium = true))

        currentUser = currentUser!!.copy(isPremium = true)
        saveSession(context)
        return true
    }

    private fun saveSession(context: Context) {
        prefs(context).edit {
            putString(KEY_SESSION_TOKEN, currentUser?.token)
            putBoolean(KEY_IS_GUEST, false)
        }
    }

    private fun saveGuestSession(context: Context) {
        prefs(context).edit {
            remove(KEY_SESSION_TOKEN)
            putBoolean(KEY_IS_GUEST, true)
        }
    }

    fun logout(context: Context) {
        currentUser = null
        prefs(context).edit {
            remove(KEY_SESSION_TOKEN)
            remove(KEY_IS_GUEST)
        }
    }

    fun addUploadedSong(song: Song) {
        uploadedSongs.add(song)
        currentUser?.isArtist = true
    }

    fun removeUploadedSong(songId: String) {
        uploadedSongs.removeAll { it.id == songId }
    }

    fun getUploadedSongs(): List<Song> = uploadedSongs.toList()

    private fun UserEntity.toUser(): User {
        return User(
            username = username,
            displayName = displayName ?: username,
            isPremium = isPremium,
            isArtist = isArtist,
            token = token,
            isGuest = false,
            avatarResId = avatarResId.takeIf { it != 0 } ?: R.drawable.uth
        )
    }
}
