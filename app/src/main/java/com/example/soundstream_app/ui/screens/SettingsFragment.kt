package com.example.soundstream_app.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.soundstream_app.LoginActivity
import com.example.soundstream_app.R
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUserView()

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(requireContext(), LoginActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            SessionManager.logout(requireContext())
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onResume() {
        super.onResume()
        updateUserView()
    }

    private fun updateUserView() {
        val user = SessionManager.currentUser
        val username = user?.username ?: getString(R.string.guest_name)
        val isLoggedIn = user != null

        binding.tvUserName.text = getString(R.string.settings_user_name, username)
        binding.tvMembership.text = when {
            user?.isPremium == true -> getString(R.string.membership_premium)
            user != null -> getString(R.string.membership_free)
            else -> getString(R.string.membership_not_logged_in)
        }
        binding.tvArtistStatus.text = if (user?.isArtist == true) {
            getString(R.string.now_artist)
        } else {
            getString(R.string.not_artist_yet)
        }
        binding.tvPremiumHint.text = if (isLoggedIn) {
            getString(R.string.settings_upload_hint)
        } else {
            getString(R.string.settings_upload_hint_guest)
        }

        binding.btnLogin.visibility = if (isLoggedIn) View.GONE else View.VISIBLE
        binding.btnLogout.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
