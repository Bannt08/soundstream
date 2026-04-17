package com.example.soundstream_app.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundstream_app.R
import com.example.soundstream_app.data.AppDatabase
import com.example.soundstream_app.data.PlaybackManager
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.FragmentLikedBinding
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.ui.components.SongListAdapter
import kotlinx.coroutines.launch

class LikedFragment : Fragment() {

    private var _binding: FragmentLikedBinding? = null
    private val binding get() = _binding!!
    private lateinit var songAdapter: SongListAdapter
    private var favoriteSongIds = emptySet<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLikedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        loadFavorites()
    }

    private fun setupList() {
        songAdapter = SongListAdapter(
            emptyList(),
            isFavorite = { favoriteSongIds.contains(it.id) },
            onSongClick = { playSong(it) },
            onFavoriteToggle = { toggleFavorite(it) },
            onAddToPlaylist = { addToPlaylist(it) }
        )

        binding.rvLikedSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLikedSongs.adapter = songAdapter
    }

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = SessionManager.currentUser
            if (user == null || SessionManager.isGuest) {
                Toast.makeText(requireContext(), "Đăng nhập để xem yêu thích", Toast.LENGTH_SHORT).show()
                return@launch
            }
            val db = AppDatabase.getInstance(requireContext())
            favoriteSongIds = db.favoriteDao().getFavoriteSongIds(user.username).toSet()
            val songs = db.songDao().getSongsByIds(favoriteSongIds.toList()).map { entity ->
                Song(
                    id = entity.id,
                    title = entity.title,
                    artistName = entity.artistName,
                    imageResId = entity.imageResId,
                    duration = entity.duration,
                    sourceUri = entity.sourceUri
                )
            }
            songAdapter.updateItems(songs)
        }
    }

    private fun playSong(song: Song) {
        PlaybackManager.play(song, listOf(song))
        findNavController().navigate(R.id.playerDetailFragment)
    }

    private fun toggleFavorite(song: Song) {
        val user = SessionManager.currentUser
        if (user == null || SessionManager.isGuest) {
            Toast.makeText(requireContext(), "Đăng nhập để quản lý yêu thích", Toast.LENGTH_SHORT).show()
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            if (favoriteSongIds.contains(song.id)) {
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
            loadFavorites()
        }
    }

    private fun addToPlaylist(song: Song) {
        Toast.makeText(requireContext(), "Sử dụng Discover hoặc Player để thêm vào playlist", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
