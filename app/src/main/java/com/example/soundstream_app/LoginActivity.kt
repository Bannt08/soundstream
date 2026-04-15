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
        SessionManager.restoreSession(this)
        if (SessionManager.hasActiveSession) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        
        startBackgroundMusic()

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
        }
    }

    private fun handleGuestLogin() {
        if (SessionManager.loginAsGuest(this)) {
            openMainScreen()
        }
    }

    private fun openMainScreen() {
        stopBackgroundMusic()
        startActivity(Intent(this, MainActivity::class.java))
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
        if (backgroundPlayer == null) {
            backgroundPlayer = MediaPlayer.create(this, R.raw.uth).apply {
                isLooping = true
                seekTo(4000)
            }
        }
        backgroundPlayer?.start()
    }

    private fun stopBackgroundMusic() {
        backgroundPlayer?.stop()
        backgroundPlayer?.release()
        backgroundPlayer = null
    }
}
