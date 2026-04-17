package com.example.soundstream_app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var backgroundPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            try {
                if (SessionManager.restoreSession(this@LoginActivity)) {
                    openMainScreen()
                } else {
                    startBackgroundMusic()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                startBackgroundMusic()
            }
        }

        binding.btnLogin.setOnClickListener { handleLogin() }
        binding.btnGuest.setOnClickListener { handleGuestLogin() }
    }

    private fun handleLogin() {
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString()?.trim().orEmpty()

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_error), Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val success = SessionManager.login(this@LoginActivity, username, password)
                if (success) {
                    openMainScreen()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.login_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun handleGuestLogin() {
        if (SessionManager.loginAsGuest(this)) {
            openMainScreen()
        }
    }

    private fun openMainScreen() {
        stopBackgroundMusic()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            stopBackgroundMusic()
        } else {
            backgroundPlayer?.pause()
        }
    }

    override fun onStart() {
        super.onStart()
        backgroundPlayer?.start()
    }

    override fun onDestroy() {
        stopBackgroundMusic()
        super.onDestroy()
    }

    private fun startBackgroundMusic() {
        try {
            if (backgroundPlayer == null) {
                backgroundPlayer = MediaPlayer.create(this, R.raw.uth).apply {
                    isLooping = true
                    seekTo(4000)
                }
            }
            backgroundPlayer?.start()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun stopBackgroundMusic() {
        backgroundPlayer?.stop()
        backgroundPlayer?.release()
        backgroundPlayer = null
    }
}
