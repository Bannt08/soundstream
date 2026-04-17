package com.example.soundstream_app.util

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PasswordUtils {
    private const val SALT_LENGTH = 16
    private const val HASH_ALGORITHM = "SHA-256"
    private const val HASH_ITERATIONS = 1000

    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPassword(password: String, salt: String): String {
        var data = password.toByteArray(Charsets.UTF_8) + Base64.decode(salt, Base64.NO_WRAP)
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
        repeat(HASH_ITERATIONS) {
            data = digest.digest(data)
        }
        return Base64.encodeToString(data, Base64.NO_WRAP)
    }

    fun verifyPassword(password: String, salt: String, hash: String): Boolean {
        return hashPassword(password, salt) == hash
    }
}
