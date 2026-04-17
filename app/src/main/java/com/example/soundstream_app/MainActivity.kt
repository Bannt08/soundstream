package com.example.soundstream_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.soundstream_app.data.PlaybackManager
import com.example.soundstream_app.databinding.ActivityMainBinding
import com.example.soundstream_app.LoginActivity
import com.example.soundstream_app.data.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (SessionManager.hasActiveSession) {
            setupNavigation()
        } else {
            lifecycleScope.launch {
                try {
                    val restored = SessionManager.restoreSession(this@MainActivity)
                    if (!restored) {
                        val loginIntent = Intent(this@MainActivity, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(loginIntent)
                        finish()
                    } else {
                        setupNavigation()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    val loginIntent = Intent(this@MainActivity, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(loginIntent)
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PlaybackManager.stop()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as? NavHostFragment
        val navController: NavController = navHostFragment?.navController
            ?: throw IllegalStateException("NavHostFragment not found in MainActivity layout")

        binding.bottomNavigation.setupWithNavController(navController)

        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_discover -> {
                    navController.navigate(R.id.discoverFragment)
                    true
                }
                R.id.menu_liked -> {
                    navController.navigate(R.id.likedFragment)
                    true
                }
                R.id.menu_playlists -> {
                    navController.navigate(R.id.playlistsFragment)
                    true
                }
                R.id.menu_settings -> {
                    navController.navigate(R.id.settingsFragment)
                    true
                }
                R.id.menu_logout -> {
                    SessionManager.logout(this)
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavigation.visibility =
                if (destination.id == R.id.playerDetailFragment) View.GONE else View.VISIBLE
        }
    }
}
