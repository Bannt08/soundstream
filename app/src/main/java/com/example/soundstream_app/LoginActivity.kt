package com.example.soundstream_app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

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
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
