package com.example.soundstream_app.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.soundstream_app.data.MockDataProvider
import com.example.soundstream_app.databinding.FragmentPlayerDetailBinding

class PlayerDetailFragment : Fragment() {

    private var _binding: FragmentPlayerDetailBinding? = null
    private val binding get() = _binding!!

    private val playbackQueue = MockDataProvider.playbackQueue
    private var currentIndex = 0
    private var isPlaying = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateSongUI()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPlayPauseDetail.setOnClickListener {
            togglePlayPause()
        }

        binding.btnPrevious.setOnClickListener {
            playPrevious()
        }

        binding.btnNext.setOnClickListener {
            playNext()
        }
    }

    private fun updateSongUI() {
        val song = playbackQueue[currentIndex]
        binding.imgBackground.setImageResource(song.imageResId)
        binding.tvDetailTitle.text = song.title
        binding.tvDetailArtist.text = song.artistName
        binding.seekDetail.progress = 0
        binding.btnPlayPauseDetail.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
    }

    private fun togglePlayPause() {
        isPlaying = !isPlaying
        binding.btnPlayPauseDetail.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
        Toast.makeText(
            requireContext(),
            if (isPlaying) "Tiếp tục phát" else "Đã tạm dừng",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun playPrevious() {
        currentIndex = if (currentIndex - 1 < 0) playbackQueue.lastIndex else currentIndex - 1
        isPlaying = true
        updateSongUI()
        Toast.makeText(requireContext(), "Bài trước", Toast.LENGTH_SHORT).show()
    }

    private fun playNext() {
        currentIndex = if (currentIndex + 1 > playbackQueue.lastIndex) 0 else currentIndex + 1
        isPlaying = true
        updateSongUI()
        Toast.makeText(requireContext(), "Bài tiếp theo", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
