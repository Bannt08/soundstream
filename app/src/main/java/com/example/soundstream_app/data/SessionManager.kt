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

    // Sử dụng để quản lý tạm thời các bài hát vừa upload trong phiên làm việc (Người 3)
    private val uploadedSongs = mutableListOf<Song>()

    val authToken: String? get() = currentUser?.token
    val isLoggedIn: Boolean get() = currentUser != null && currentUser?.isGuest == false
    val isGuest: Boolean get() = currentUser?.isGuest == true
    val hasActiveSession: Boolean get() = currentUser != null

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
        } else false
    }

    suspend fun loginWithToken(context: Context, token: String): Boolean {
        AppDatabase.waitForInitialization(context)
        val userEntity = AppDatabase.getInstance(context).userDao().getUserByToken(token)
        return if (userEntity != null) {
            currentUser = userEntity.toUser()
            saveSession(context)
            uploadedSongs.clear()
            true
        } else false
    }

    suspend fun restoreSession(context: Context): Boolean {
        val storedPrefs = prefs(context)
        if (storedPrefs.getBoolean(KEY_IS_GUEST, false)) {
            return loginAsGuest(context)
        }
        val token = storedPrefs.getString(KEY_SESSION_TOKEN, null)
        return if (!token.isNullOrBlank()) {
            try {
                AppDatabase.waitForInitialization(context)
                loginWithToken(context, token)
            } catch (ex: Exception) { false }
        } else false
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

    suspend fun register(context: Context, u: String, p: String, d: String, avt: Int = R.drawable.uth): Boolean {
        AppDatabase.waitForInitialization(context)
        val userDao = AppDatabase.getInstance(context).userDao()
        if (userDao.getUser(u) != null) return false

        val salt = PasswordUtils.generateSalt()
        val token = UUID.randomUUID().toString()
        val userEntity = UserEntity(
            username = u, passwordHash = PasswordUtils.hashPassword(p, salt),
            passwordSalt = salt, displayName = d.ifBlank { u },
            isPremium = false, isArtist = false, isAdmin = false,
            token = token, avatarResId = avt
        )

        userDao.insertUser(userEntity)
        currentUser = userEntity.toUser()
        saveSession(context)
        return true
    }

    suspend fun updateProfile(context: Context, displayName: String, avatarResId: Int): Boolean {
        val user = currentUser ?: return false
        if (user.isGuest) return false

        AppDatabase.waitForInitialization(context)
        val userDao = AppDatabase.getInstance(context).userDao()
        val userEntity = userDao.getUser(user.username) ?: return false

        val updatedEntity = userEntity.copy(displayName = displayName.ifBlank { user.username }, avatarResId = avatarResId)
        userDao.updateUser(updatedEntity)
        currentUser = updatedEntity.toUser()
        saveSession(context)
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

    // Tự động cập nhật isArtist = true khi user upload bài hát đầu tiên (Người 3)
    suspend fun markAsArtist(context: Context) {
        val user = currentUser ?: return
        if (user.isGuest || user.isArtist) return

        AppDatabase.waitForInitialization(context)
        val userDao = AppDatabase.getInstance(context).userDao()
        val userEntity = userDao.getUser(user.username) ?: return

        userDao.updateUser(userEntity.copy(isArtist = true))
        currentUser = currentUser!!.copy(isArtist = true)
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
        uploadedSongs.clear()
        prefs(context).edit().clear().apply() // Xóa sạch cache khi logout (Người 1)
    }

    private fun UserEntity.toUser() = User(
        username = username, displayName = displayName ?: username,
        isPremium = isPremium, isArtist = isArtist,
        token = token, isGuest = false,
        avatarResId = if (avatarResId != 0) avatarResId else R.drawable.uth
    )
}