package com.example.soundstream_app.ui.screens

import android.app.AlertDialog
import android.animation.ObjectAnimator
import com.example.soundstream_app.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        currentIndex = PlaybackManager.currentIndex
        isPlaying = PlaybackManager.isPlaying
        updateSongUI()
        loadFavoriteState()

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

        setupSeekBar()

        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        binding.btnAddToPlaylist.setOnClickListener {
            showPlaylistPicker()
        }
    }

    private fun updateSongUI() {
        val song = PlaybackManager.currentSong ?: playbackQueue.getOrNull(currentIndex) ?: run {
            Toast.makeText(requireContext(), "Không có bài hát để phát", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }

        currentIndex = PlaybackManager.currentIndex
        isPlaying = PlaybackManager.isPlaying
        binding.imgBackground.setImageResource(song.imageResId)
        binding.imgLogo.setImageResource(song.imageResId)
        binding.tvDetailTitle.text = song.title
        binding.tvDetailArtist.text = song.artistName
        binding.seekDetail.max = 100
        binding.seekDetail.progress = PlaybackManager.progressPercent
        binding.btnPlayPauseDetail.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
        binding.tvCurrentTime.text = formatTime(PlaybackManager.currentPosition)
        binding.tvRemainingTime.text = formatTime((PlaybackManager.duration - PlaybackManager.currentPosition).coerceAtLeast(0))
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
                if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )
        }
    }

    private fun togglePlayPause() {
        if (PlaybackManager.isPlaying) {
            PlaybackManager.pause()
            isPlaying = false
        } else {
            PlaybackManager.resume()
            isPlaying = true
        }
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
        PlaybackManager.playPrevious()
        currentIndex = PlaybackManager.currentIndex
        isPlaying = PlaybackManager.isPlaying
        updateSongUI()
        loadFavoriteState()
        Toast.makeText(requireContext(), "Bài trước", Toast.LENGTH_SHORT).show()
    }

    private fun playNext() {
        PlaybackManager.playNext()
        currentIndex = PlaybackManager.currentIndex
        isPlaying = PlaybackManager.isPlaying
        updateSongUI()
        loadFavoriteState()
        Toast.makeText(requireContext(), "Bài tiếp theo", Toast.LENGTH_SHORT).show()
    }

    private fun setupSeekBar() {
        binding.seekDetail.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val seekPosition = ((PlaybackManager.duration * progress) / 100).coerceIn(0, PlaybackManager.duration)
                    binding.tvCurrentTime.text = formatTime(seekPosition)
                    binding.tvRemainingTime.text = formatTime((PlaybackManager.duration - seekPosition).coerceAtLeast(0))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    val seekPosition = ((PlaybackManager.duration * it.progress) / 100).coerceIn(0, PlaybackManager.duration)
                    PlaybackManager.seekTo(seekPosition)
                    updateSongUI()
                }
            }
        })
    }

    private fun formatTime(milliseconds: Int): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun toggleFavorite() {
        val user = SessionManager.currentUser
        if (user == null || SessionManager.isGuest) {
            Toast.makeText(requireContext(), "Đăng nhập để quản lý yêu thích", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val song = playbackQueue.getOrNull(currentIndex) ?: return@launch
            if (isFavorite) {
                db.favoriteDao().removeFavorite(user.username, song.id)
                Toast.makeText(requireContext(), "Đã xóa khỏi yêu thích", Toast.LENGTH_SHORT).show()
            } else {
                db.favoriteDao().addFavorite(
                    com.example.soundstream_app.model.FavoriteSongEntity(
                        username = user.username,
                        songId = song.id
                    )
                )
                Toast.makeText(requireContext(), "Đã thêm vào yêu thích", Toast.LENGTH_SHORT).show()
            }
            isFavorite = !isFavorite
            binding.btnFavorite.setImageResource(
                if (isFavorite) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            )
        }
    }

    private fun showPlaylistPicker() {
        val user = SessionManager.currentUser
        if (user == null || SessionManager.isGuest) {
            Toast.makeText(requireContext(), "Đăng nhập để thêm vào playlist", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            val playlists = db.playlistDao().getPlaylistsForUser(user.username)
            if (playlists.isEmpty()) {
                Toast.makeText(requireContext(), "Bạn chưa có playlist nào", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val titles = playlists.map { it.playlist.title }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Thêm vào playlist")
                .setItems(titles) { _, index ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        playbackQueue.getOrNull(currentIndex)?.let { song ->
                            db.playlistDao().addSongToPlaylist(
                                PlaylistSongCrossRef(
                                    playlistId = playlists[index].playlist.id,
                                    songId = song.id
                                )
                            )
                            Toast.makeText(
                                requireContext(),
                                "Đã thêm vào ${playlists[index].playlist.title}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                .setNegativeButton("Hủy", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        PlaybackManager.addPlaybackStateChangedListener(playbackStateListener)
        startLogoRotation()
        playbackStateListener.invoke()
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
