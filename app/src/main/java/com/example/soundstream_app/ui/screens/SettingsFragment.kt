package com.example.soundstream_app.ui.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
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
        val user = SessionManager.currentUser

        binding.tvUserName.text = getString(R.string.settings_user_name, user.username)
        binding.tvMembership.text = if (user.isPremium) {
            getString(R.string.membership_premium)
        } else {
            getString(R.string.membership_free)
        }
        binding.tvArtistStatus.text = if (user.isArtist) {
            getString(R.string.now_artist)
        } else {
            getString(R.string.not_artist_yet)
        }
        binding.tvPremiumHint.text = getString(R.string.settings_upload_hint)
        binding.btnUploadMusic.isEnabled = user.isPremium
        binding.btnUploadMusic.alpha = if (user.isPremium) 1f else 0.5f

        binding.btnUploadMusic.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_uploadFragment)
        }

        binding.btnLogout.setOnClickListener {
            SessionManager.logout()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
