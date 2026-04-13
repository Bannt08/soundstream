package com.example.soundstream_app.ui.screens

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.soundstream_app.R
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.FragmentUploadBinding
import com.example.soundstream_app.model.Song
import java.util.UUID

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val currentUser get() = SessionManager.currentUser

    private val pickAudio = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            onAudioSelected(uri)
        } else {
            Toast.makeText(requireContext(), getString(R.string.upload_cancelled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = currentUser
        if (user == null) {
            binding.tvUploadHint.text = getString(R.string.upload_not_logged_in)
            binding.btnChooseAudio.text = getString(R.string.choose_audio_file)
            binding.btnChooseAudio.isEnabled = false
            binding.tvStatus.text = getString(R.string.upload_not_logged_in)
            binding.tvArtistStatus.text = getString(R.string.not_artist_yet)
            return
        }

        binding.tvUploadHint.text = if (user.isPremium) {
            getString(R.string.upload_premium_hint)
        } else {
            getString(R.string.upload_not_premium)
        }

        binding.btnChooseAudio.text = getString(R.string.choose_audio_file)
        binding.btnChooseAudio.isEnabled = user.isPremium
        binding.btnChooseAudio.setOnClickListener {
            if (user.isPremium) {
                pickAudio.launch("audio/*")
            }
        }

        updateArtistStatus()
        if (!user.isPremium) {
            binding.tvStatus.text = getString(R.string.premium_required_message)
        }
    }

    private fun onAudioSelected(uri: Uri) {
        val user = currentUser ?: return
        val trackName = uri.lastPathSegment?.substringAfterLast('/') ?: getString(R.string.uploaded_song)
        val uploadedSong = Song(
            id = UUID.randomUUID().toString(),
            title = trackName,
            artistName = user.username,
            imageResId = R.drawable.demo_artist_1,
            duration = "03:30"
        )

        SessionManager.addUploadedSong(uploadedSong)
        binding.tvStatus.text = getString(R.string.upload_success_message, trackName)
        updateArtistStatus()
    }

    private fun updateArtistStatus() {
        binding.tvArtistStatus.text = if (currentUser?.isArtist == true) {
            getString(R.string.now_artist)
        } else {
            getString(R.string.not_artist_yet)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
