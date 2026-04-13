package com.example.soundstream_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.soundstream_app.databinding.ActivityMainBinding
import com.example.soundstream_app.LoginActivity
import com.example.soundstream_app.data.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SessionManager.restoreSession(this)
        if (!SessionManager.hasActiveSession) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHost.navController
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
