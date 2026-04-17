package com.example.soundstream_app.ui.screens

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundstream_app.R
import com.example.soundstream_app.data.AppDatabase
import com.example.soundstream_app.data.PlaybackManager
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.DialogAlbumSongsBinding
import com.example.soundstream_app.databinding.FragmentUploadBinding
import com.example.soundstream_app.model.PlaylistEntity
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.model.SongEntity
import com.example.soundstream_app.ui.components.AlbumAdapter
import com.example.soundstream_app.ui.components.AlbumSongAdapter
import com.example.soundstream_app.ui.components.UploadedSongAdapter
import kotlinx.coroutines.launch
import java.util.UUID

class UploadFragment : Fragment() {

    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!
    private val currentUser get() = SessionManager.currentUser
    private lateinit var uploadedSongAdapter: UploadedSongAdapter
    private lateinit var albumAdapter: AlbumAdapter

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
        setupUploadedList()
        setupAlbumList()

        val user = currentUser
        if (user == null || user.isGuest) {
            binding.tvUploadHint.text = getString(R.string.upload_not_logged_in)
            binding.btnAddActions.isEnabled = false
            binding.btnAddActions.alpha = 0.5f
            binding.tvStatus.text = getString(R.string.upload_not_logged_in)
            binding.tvArtistStatus.text = getString(R.string.not_artist_yet)
            binding.tvAlbumsTitle.visibility = View.GONE
            binding.tvUploadedListTitle.visibility = View.GONE
            return
        }

        binding.tvAccountName.text = getString(R.string.profile_account_name, user.username)
        binding.tvArtistName.text = getString(R.string.profile_artist_name, user.displayName)

        if (user.isPremium) {
            binding.tvUploadHint.text = getString(R.string.upload_premium_hint)
            binding.tvStatus.text = getString(R.string.upload_ready_message)
            binding.btnAddActions.isEnabled = true
            binding.btnAddActions.alpha = 1f
        } else {
            binding.tvUploadHint.text = getString(R.string.upload_not_premium)
            binding.tvStatus.text = getString(R.string.premium_required_message)
            binding.btnAddActions.isEnabled = false
            binding.btnAddActions.alpha = 0.5f
        }

        binding.btnAddActions.setOnClickListener {
            showActionMenu()
        }

        updateArtistStatus()
        loadAlbums()
        loadUploadedSongs()
    }

    private fun setupUploadedList() {
        uploadedSongAdapter = UploadedSongAdapter(
            emptyList(),
            onSongClick = { song -> playSong(song) },
            onAddToAlbum = { song -> showAddToAlbumDialog(song) },
            onDelete = { song -> showDeleteSongDialog(song) }
        )
        binding.rvUploadedSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvUploadedSongs.adapter = uploadedSongAdapter
        binding.tvUploadedListTitle.visibility = View.GONE
    }

    private fun setupAlbumList() {
        albumAdapter = AlbumAdapter(
            emptyList(),
            onAlbumClick = { playlistWithSongs -> showAlbumSongsDialog(playlistWithSongs) },
            onAlbumOptions = { playlistWithSongs -> showAlbumOptions(playlistWithSongs) }
        )
        binding.rvAlbums.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvAlbums.adapter = albumAdapter
        binding.tvAlbumsTitle.visibility = View.GONE
        binding.rvAlbums.visibility = View.GONE
    }

    private fun showActionMenu() {
        if (currentUser?.isPremium != true) {
            Toast.makeText(requireContext(), getString(R.string.premium_required_message), Toast.LENGTH_SHORT).show()
            return
        }

        val options = arrayOf(
            getString(R.string.create_album),
            getString(R.string.choose_audio_file)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.action_menu_title))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showCreateAlbumDialog()
                    1 -> pickAudio.launch("audio/*")
                }
            }
            .show()
    }

    private fun showAlbumOptions(playlistWithSongs: com.example.soundstream_app.model.PlaylistWithSongs) {
        val options = arrayOf(
            getString(R.string.album_edit),
            getString(R.string.album_delete)
        )

        AlertDialog.Builder(requireContext())
            .setTitle(playlistWithSongs.playlist.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditAlbumDialog(playlistWithSongs)
                    1 -> showDeleteAlbumDialog(playlistWithSongs)
                }
            }
            .show()
    }

    private fun showEditAlbumDialog(playlistWithSongs: com.example.soundstream_app.model.PlaylistWithSongs) {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.create_album_name_hint)
            setText(playlistWithSongs.playlist.title)
            setSelection(text?.length ?: 0)
            setPadding(24, 24, 24, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.album_edit_title))
            .setView(editText)
            .setPositiveButton(getString(R.string.album_confirm)) { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    editAlbum(playlistWithSongs.playlist.id, newName)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.album_name_required), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.album_cancel), null)
            .show()
    }

    private fun showDeleteAlbumDialog(playlistWithSongs: com.example.soundstream_app.model.PlaylistWithSongs) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.album_delete))
            .setMessage(getString(R.string.album_delete_confirmation, playlistWithSongs.playlist.title))
            .setPositiveButton(getString(R.string.album_confirm)) { _, _ ->
                deleteAlbum(playlistWithSongs.playlist.id)
            }
            .setNegativeButton(getString(R.string.album_cancel), null)
            .show()
    }

    private fun editAlbum(playlistId: String, newTitle: String) {
        lifecycleScope.launch {
            val playlistDao = AppDatabase.getInstance(requireContext()).playlistDao()
            val current = playlistDao.getPlaylistWithSongs(playlistId)?.playlist ?: return@launch
            playlistDao.updatePlaylist(current.copy(title = newTitle))
            loadAlbums()
            Toast.makeText(requireContext(), getString(R.string.album_edit) + " thành công", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteAlbum(playlistId: String) {
        lifecycleScope.launch {
            val playlistDao = AppDatabase.getInstance(requireContext()).playlistDao()
            playlistDao.deletePlaylistCrossRefs(playlistId)
            playlistDao.deletePlaylist(playlistId)
            loadAlbums()
            Toast.makeText(requireContext(), getString(R.string.album_delete) + " thành công", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCreateAlbumDialog() {
        val editText = EditText(requireContext()).apply {
            hint = getString(R.string.create_album_name_hint)
            setPadding(24, 24, 24, 24)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.create_album_title))
            .setView(editText)
            .setPositiveButton(getString(R.string.create_album)) { _, _ ->
                val albumName = editText.text.toString().trim()
                if (albumName.isNotEmpty()) {
                    createAlbum(albumName)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.album_name_required), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun createAlbum(albumName: String) {
        val user = currentUser ?: return
        val playlist = PlaylistEntity(
            id = UUID.randomUUID().toString(),
            title = albumName,
            description = getString(R.string.album_description, user.displayName),
            imageResId = if (Math.random() < 0.5) R.drawable.img1 else R.drawable.img2,
            ownerUsername = user.username
        )

        lifecycleScope.launch {
            AppDatabase.getInstance(requireContext()).playlistDao().insertPlaylist(playlist)
            Toast.makeText(requireContext(), getString(R.string.album_created_message, albumName), Toast.LENGTH_SHORT).show()
            loadAlbums()
        }
    }

    private fun loadAlbums() {
        val user = currentUser ?: return
        lifecycleScope.launch {
            val playlists = AppDatabase.getInstance(requireContext()).playlistDao().getPlaylistsForUser(user.username)
            binding.tvAlbumsTitle.visibility = if (playlists.isEmpty()) View.GONE else View.VISIBLE
            binding.rvAlbums.visibility = if (playlists.isEmpty()) View.GONE else View.VISIBLE
            albumAdapter.updateItems(playlists)
        }
    }

    private fun showAlbumSongsDialog(playlistWithSongs: com.example.soundstream_app.model.PlaylistWithSongs) {
        val dialogBinding = DialogAlbumSongsBinding.inflate(layoutInflater)
        dialogBinding.tvDialogAlbumTitle.text = getString(
            R.string.album_songs_title,
            playlistWithSongs.playlist.title
        )

        val playlistSongs = playlistWithSongs.songs.map {
            Song(
                id = it.id,
                title = it.title,
                artistName = it.artistName,
                imageResId = it.imageResId,
                duration = it.duration
            )
        }

        val adapter = AlbumSongAdapter(playlistSongs) { song ->
            playSong(song, playlistSongs)
        }
        dialogBinding.rvAlbumSongs.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvAlbumSongs.adapter = adapter

        AlertDialog.Builder(requireContext())
            .setTitle(playlistWithSongs.playlist.title)
            .setView(dialogBinding.root)
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun showAddToAlbumDialog(song: Song) {
        val user = currentUser ?: return
        lifecycleScope.launch {
            val playlists = AppDatabase.getInstance(requireContext()).playlistDao().getPlaylistsForUser(user.username)
            if (playlists.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.no_albums_message), Toast.LENGTH_SHORT).show()
                return@launch
            }

            val titles = playlists.map { it.playlist.title }.toTypedArray()
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.add_to_album))
                .setItems(titles) { _, index ->
                    addSongToAlbum(song, playlists[index].playlist.id)
                }
                .show()
        }
    }

    private fun addSongToAlbum(song: Song, albumId: String) {
        lifecycleScope.launch {
            AppDatabase.getInstance(requireContext()).playlistDao()
                .addSongToPlaylist(com.example.soundstream_app.model.PlaylistSongCrossRef(
                    playlistId = albumId,
                    songId = song.id
                ))
            Toast.makeText(requireContext(), getString(R.string.added_song_to_album), Toast.LENGTH_SHORT).show()
            loadAlbums()
        }
    }

    private fun playSong(song: Song, queue: List<Song> = listOf(song)) {
        PlaybackManager.play(song, queue)
        findNavController().navigate(R.id.playerDetailFragment)
    }

    private fun loadUploadedSongs() {
        val user = currentUser ?: return
        lifecycleScope.launch {
            val songEntities = AppDatabase.getInstance(requireContext()).songDao().getSongsByOwner(user.username)
            val songs = songEntities.map {
                Song(
                    id = it.id,
                    title = it.title,
                    artistName = it.artistName,
                    imageResId = it.imageResId,
                    duration = it.duration
                )
            }
            binding.tvUploadedListTitle.visibility = if (songs.isEmpty()) View.GONE else View.VISIBLE
            uploadedSongAdapter.updateItems(songs)
        }
    }

    private fun showDeleteSongDialog(song: Song) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_song))
            .setMessage(getString(R.string.delete_song_confirmation, song.title))
            .setPositiveButton(getString(R.string.album_confirm)) { _, _ ->
                deleteUploadedSong(song)
            }
            .setNegativeButton(getString(R.string.album_cancel), null)
            .show()
    }

    private fun deleteUploadedSong(song: Song) {
        lifecycleScope.launch {
            AppDatabase.getInstance(requireContext()).songDao().deleteSong(song.id)
            loadUploadedSongs()
            Toast.makeText(requireContext(), getString(R.string.delete_song_success), Toast.LENGTH_SHORT).show()
        }
    }

    private fun onAudioSelected(uri: Uri) {
        showUploadSongDialog(uri)
    }

    private fun showUploadSongDialog(uri: Uri) {
        val user = currentUser ?: return
        val defaultTrackName = uri.lastPathSegment?.substringAfterLast('/') ?: getString(R.string.uploaded_song)
        val titleInput = EditText(requireContext()).apply {
            hint = getString(R.string.upload_song_title_hint)
            setText(defaultTrackName)
            setPadding(24, 24, 24, 24)
        }
        val artistInput = EditText(requireContext()).apply {
            hint = getString(R.string.upload_song_artist_hint)
            setText(user.displayName)
            setPadding(24, 24, 24, 24)
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            addView(titleInput)
            addView(artistInput)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.upload_song_info_title))
            .setView(container)
            .setPositiveButton(getString(R.string.album_confirm)) { _, _ ->
                val titleText = titleInput.text.toString().trim()
                val artistText = artistInput.text.toString().trim().ifEmpty { user.displayName }
                if (titleText.isEmpty()) {
                    Toast.makeText(requireContext(), getString(R.string.upload_song_save_error), Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                saveUploadedSong(uri, titleText, artistText)
            }
            .setNegativeButton(getString(R.string.album_cancel), null)
            .show()
    }

    private fun saveUploadedSong(uri: Uri, title: String, artistName: String) {
        val user = currentUser ?: return
        val uploadedSong = Song(
            id = UUID.randomUUID().toString(),
            title = title,
            artistName = artistName,
            imageResId = R.drawable.img1,
            duration = "03:30",
            sourceUri = uri.toString()
        )

        binding.tvStatus.text = getString(R.string.upload_success_message, title)
        lifecycleScope.launch {
            AppDatabase.getInstance(requireContext()).songDao().insertSong(
                SongEntity(
                    id = uploadedSong.id,
                    title = uploadedSong.title,
                    artistName = uploadedSong.artistName,
                    imageResId = uploadedSong.imageResId,
                    duration = uploadedSong.duration,
                    sourceUri = uploadedSong.sourceUri,
                    isUploaded = true,
                    ownerUsername = user.username
                )
            )

            if (!user.isArtist) {
                val userDao = AppDatabase.getInstance(requireContext()).userDao()
                val userEntity = userDao.getUser(user.username)
                if (userEntity != null && !userEntity.isArtist) {
                    userDao.updateUser(userEntity.copy(isArtist = true))
                    user.isArtist = true
                }
            }

            updateArtistStatus()
            loadUploadedSongs()
            loadAlbums()
        }
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
