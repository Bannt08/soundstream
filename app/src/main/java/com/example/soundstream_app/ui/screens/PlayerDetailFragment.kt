package com.example.soundstream_app.ui.screens

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.animation.LinearInterpolator
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.soundstream_app.data.AppDatabase
import com.example.soundstream_app.data.PlaybackManager
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.FragmentPlayerDetailBinding
import com.example.soundstream_app.model.PlaylistSongCrossRef
import kotlinx.coroutines.launch

class PlayerDetailFragment : Fragment() {

    private var _binding: FragmentPlayerDetailBinding? = null
    private val binding get() = _binding!!

    private val playbackQueue get() = PlaybackManager.playbackQueue
    private var currentIndex = PlaybackManager.currentIndex
    private var isPlaying = PlaybackManager.isPlaying
    private var isFavorite = false
    private var logoAnimator: ObjectAnimator? = null

    private val playbackStateListener = {
        currentIndex = PlaybackManager.currentIndex
        isPlaying = PlaybackManager.isPlaying
        updateSongUI()
        loadFavoriteState()
    }

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

        if (playbackQueue.isEmpty()) {
            Toast.makeText(requireContext(), "Không có bài hát để phát", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        updateSongUI()
        loadFavoriteState()

        binding.btnBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnPlayPauseDetail.setOnClickListener { togglePlayPause() }
        binding.btnPrevious.setOnClickListener { playPrevious() }
        binding.btnNext.setOnClickListener { playNext() }
        binding.btnFavorite.setOnClickListener { toggleFavorite() }
        binding.btnAddToPlaylist.setOnClickListener { showPlaylistPicker() }

        setupSeekBar()
    }

    private fun updateSongUI() {
        val song = PlaybackManager.currentSong ?: playbackQueue.getOrNull(currentIndex) ?: return

        binding.imgBackground.setImageResource(song.imageResId)
        binding.imgLogo.setImageResource(song.imageResId)
        binding.tvDetailTitle.text = song.title
        binding.tvDetailArtist.text = song.artistName

        binding.seekDetail.max = 100
        binding.seekDetail.progress = PlaybackManager.progressPercent

        binding.btnPlayPauseDetail.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )

        binding.tvCurrentTime.text = formatTime(PlaybackManager.currentPosition)
        binding.tvRemainingTime.text =
            formatTime((PlaybackManager.duration - PlaybackManager.currentPosition).coerceAtLeast(0))
    }

    private fun loadFavoriteState() {
        val user = SessionManager.currentUser
        if (user == null || SessionManager.isGuest) {
            binding.btnFavorite.visibility = View.GONE
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val favorites = db.favoriteDao().getFavoriteSongIds(user.username)
            isFavorite = playbackQueue.getOrNull(currentIndex)?.id in favorites

            binding.btnFavorite.setImageResource(
                if (isFavorite) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
        }
    }

    private fun togglePlayPause() {
        if (PlaybackManager.isPlaying) {
            PlaybackManager.pause()
        } else {
            PlaybackManager.resume()
        }

        isPlaying = PlaybackManager.isPlaying
        updateSongUI()

        if (isPlaying) startLogoRotation() else stopLogoRotation()
    }

    private fun playPrevious() {
        PlaybackManager.playPrevious()
        updateSongUI()
        loadFavoriteState()
    }

    private fun playNext() {
        PlaybackManager.playNext()
        updateSongUI()
        loadFavoriteState()
    }

    private fun setupSeekBar() {
        binding.seekDetail.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val pos = (PlaybackManager.duration * progress) / 100
                    binding.tvCurrentTime.text = formatTime(pos)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    val pos = (PlaybackManager.duration * it.progress) / 100
                    PlaybackManager.seekTo(pos)
                }
            }
        })
    }

    private fun formatTime(ms: Int): String {
        val total = ms / 1000
        val m = total / 60
        val s = total % 60
        return String.format("%d:%02d", m, s)
    }

    private fun toggleFavorite() {
        val user = SessionManager.currentUser ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val song = playbackQueue.getOrNull(currentIndex) ?: return@launch

            if (isFavorite) {
                db.favoriteDao().removeFavorite(user.username, song.id)
            } else {
                db.favoriteDao().addFavorite(
                    com.example.soundstream_app.model.FavoriteSongEntity(
                        username = user.username,
                        songId = song.id
                    )
                )
            }

            isFavorite = !isFavorite
            loadFavoriteState()
        }
    }

    private fun showPlaylistPicker() {
        val user = SessionManager.currentUser ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val playlists = db.playlistDao().getPlaylistsForUser(user.username)

            val titles = playlists.map { it.playlist.title }.toTypedArray()

            AlertDialog.Builder(requireContext())
                .setTitle("Chọn playlist")
                .setItems(titles) { _, index ->
                    lifecycleScope.launch {
                        val song = playbackQueue.getOrNull(currentIndex) ?: return@launch
                        db.playlistDao().addSongToPlaylist(
                            PlaylistSongCrossRef(
                                playlists[index].playlist.id,
                                song.id
                            )
                        )
                        Toast.makeText(requireContext(), "Đã thêm", Toast.LENGTH_SHORT).show()
                    }
                }
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        PlaybackManager.addPlaybackStateChangedListener(playbackStateListener)

        if (PlaybackManager.isPlaying) startLogoRotation()
    }

    override fun onPause() {
        super.onPause()
        PlaybackManager.removePlaybackStateChangedListener(playbackStateListener)
        stopLogoRotation()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLogoRotation()
        _binding = null
    }

    private fun startLogoRotation() {
        logoAnimator?.cancel()
        logoAnimator = ObjectAnimator.ofFloat(binding.imgLogo, View.ROTATION, 0f, 360f).apply {
            duration = 8000L
            repeatCount = ObjectAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopLogoRotation() {
        logoAnimator?.cancel()
        logoAnimator = null
    }
}