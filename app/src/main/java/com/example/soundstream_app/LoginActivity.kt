package com.example.soundstream_app

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        binding.btnRegister.setOnClickListener { showRegisterDialog() }
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
                    Toast.makeText(this@LoginActivity, getString(R.string.login_error), Toast.LENGTH_SHORT).show()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                Toast.makeText(this@LoginActivity, getString(R.string.login_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGuestLogin() {
        if (SessionManager.loginAsGuest(this)) {
            openMainScreen()
        }
    }

    private fun showRegisterDialog() {
        val usernameInput = EditText(this).apply {
            hint = getString(R.string.username_hint)
            setSingleLine()
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setPadding(18, 18, 18, 18)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 12
            }
        }

        val displayNameInput = EditText(this).apply {
            hint = getString(R.string.display_name_hint)
            setSingleLine()
            inputType = android.text.InputType.TYPE_CLASS_TEXT
            setPadding(18, 18, 18, 18)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 12
            }
        }

        val passwordInput = EditText(this).apply {
            hint = getString(R.string.password_hint)
            setSingleLine()
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(18, 18, 18, 18)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 12
            }
        }

        val confirmPasswordInput = EditText(this).apply {
            hint = getString(R.string.confirm_password_hint)
            setSingleLine()
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setPadding(18, 18, 18, 18)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                bottomMargin = 12
            }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            addView(usernameInput)
            addView(displayNameInput)
            addView(passwordInput)
            addView(confirmPasswordInput)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.register_title))
            .setView(container)
            .setPositiveButton(getString(R.string.register_button)) { _, _ ->
                val username = usernameInput.text?.toString()?.trim().orEmpty()
                val displayName = displayNameInput.text?.toString()?.trim().orEmpty()
                val password = passwordInput.text?.toString().orEmpty()
                val confirmPassword = confirmPasswordInput.text?.toString().orEmpty()

                when {
                    username.isEmpty() || displayName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ->
                        Toast.makeText(this, getString(R.string.register_error_fill_all), Toast.LENGTH_SHORT).show()
                    password != confirmPassword ->
                        Toast.makeText(this, getString(R.string.register_error_passwords_mismatch), Toast.LENGTH_SHORT).show()
                    else -> lifecycleScope.launch {
                        val success = SessionManager.register(this@LoginActivity, username, password, displayName)
                        if (success) {
                            openMainScreen()
                        } else {
                            Toast.makeText(this@LoginActivity, getString(R.string.register_failed_username_exists), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
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
