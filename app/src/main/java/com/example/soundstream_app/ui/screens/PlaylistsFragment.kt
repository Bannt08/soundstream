package com.example.soundstream_app.ui.screens

import android.app.AlertDialog
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
import com.example.soundstream_app.databinding.FragmentPlaylistsBinding
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.ui.components.AlbumAdapter
import com.example.soundstream_app.ui.components.AlbumSongAdapter
import kotlinx.coroutines.launch

class PlaylistsFragment : Fragment() {

    private var _binding: FragmentPlaylistsBinding? = null
    private val binding get() = _binding!!
    private lateinit var albumAdapter: AlbumAdapter
    private var playlists = emptyList<com.example.soundstream_app.model.PlaylistWithSongs>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPlaylistList()
        loadPlaylists()
    }

    private fun setupPlaylistList() {
        albumAdapter = AlbumAdapter(emptyList(), onAlbumClick = { playlist ->
            showPlaylistSongs(playlist)
        }, onAlbumOptions = { playlist ->
            Toast.makeText(requireContext(), "Playlist: ${playlist.playlist.title}", Toast.LENGTH_SHORT).show()
        })
        binding.rvPlaylists.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPlaylists.adapter = albumAdapter
    }

    private fun loadPlaylists() {
        viewLifecycleOwner.lifecycleScope.launch {
            val db = AppDatabase.getInstance(requireContext())
            playlists = db.playlistDao().getAllPlaylistsWithSongs()
            albumAdapter.updateItems(playlists)
            if (playlists.isEmpty()) {
                binding.tvPlaylistsTitle.text = "Chưa có playlist"
            }
        }
    }

    private fun showPlaylistSongs(playlist: com.example.soundstream_app.model.PlaylistWithSongs) {
        val songs = playlist.songs.map { entity ->
            Song(
                id = entity.id,
                title = entity.title,
                artistName = entity.artistName,
                imageResId = entity.imageResId,
                duration = entity.duration,
                sourceUri = entity.sourceUri
            )
        }

        val songTitles = songs.map { it.title }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(playlist.playlist.title)
            .setItems(songTitles) { _, index ->
                val song = songs[index]
                PlaybackManager.play(song, songs)
                findNavController().navigate(R.id.playerDetailFragment)
            }
            .setPositiveButton("Đóng", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
