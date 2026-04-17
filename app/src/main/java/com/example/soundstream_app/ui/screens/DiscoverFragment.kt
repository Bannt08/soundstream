package com.example.soundstream_app.ui.screens

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundstream_app.R
import com.example.soundstream_app.data.AppDatabase
import com.example.soundstream_app.data.PlaybackManager
import com.example.soundstream_app.data.RawAudioProvider
import com.example.soundstream_app.data.SessionManager
import com.example.soundstream_app.databinding.DialogAlbumSongsBinding
import com.example.soundstream_app.databinding.FragmentDiscoverBinding
import com.example.soundstream_app.model.PlaylistEntity
import com.example.soundstream_app.model.Song
import com.example.soundstream_app.model.SongEntity
import com.example.soundstream_app.model.PlaylistWithSongs
import com.example.soundstream_app.ui.components.AlbumAdapter
import com.example.soundstream_app.ui.components.AlbumSongAdapter
import com.example.soundstream_app.ui.components.ArtistAdapter
import com.example.soundstream_app.ui.components.SongListAdapter
import com.example.soundstream_app.ui.components.MadeForYouAdapter
import kotlinx.coroutines.launch

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!
    private lateinit var songAdapter: SongListAdapter
    private lateinit var trendingAdapter: AlbumAdapter
    private lateinit var madeForYouAdapter: MadeForYouAdapter
    private lateinit var trendingPlaylist: PlaylistWithSongs
    private var allSongs: List<Song> = emptyList()
    private var favoriteSongIds: Set<String> = emptySet()
    private val playbackStateUpdateListener = {
        refreshMiniPlayer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLists()
        setupGreeting()
        setupSearch()
        setupMiniPlayer()
        loadContent()
    }

    override fun onResume() {
        super.onResume()
        PlaybackManager.addPlaybackStateChangedListener(playbackStateUpdateListener)
        setupGreeting()
        refreshMiniPlayer()
    }

    override fun onPause() {
        super.onPause()
        PlaybackManager.removePlaybackStateChangedListener(playbackStateUpdateListener)
    }

    private fun setupLists() {
        songAdapter = SongListAdapter(
            emptyList(),
            isFavorite = { favoriteSongIds.contains(it.id) },
            onSongClick = { playSong(it) },
            onFavoriteToggle = { toggleFavorite(it) },
            onAddToPlaylist = { showPlaylistPicker(it) }
        )

        trendingAdapter = AlbumAdapter(emptyList(), onAlbumClick = { playlist ->
            showTrendingAlbumSongs(playlist)
        }, onAlbumOptions = {})

        binding.rvTrendingSongs.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTrendingSongs.adapter = trendingAdapter

        madeForYouAdapter = MadeForYouAdapter(emptyList()) { playlist ->
            Toast.makeText(requireContext(), "Mở playlist ${playlist.title}", Toast.LENGTH_SHORT).show()
        }
        binding.rvMadeForYou.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvMadeForYou.adapter = madeForYouAdapter

        binding.rvPopularSingers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPopularSingers.adapter = ArtistAdapter(RawAudioProvider.popularSingers)
    }

    private fun setupSearch() {
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            filterSongs(text?.toString().orEmpty())
        }
    }

    private fun setupGreeting() {
        val user = SessionManager.currentUser
        binding.tvGreeting.text = if (user == null || SessionManager.isGuest) {
            "Xin chào !"
        } else {
            "Xin chào ${user.displayName} !"
        }
    }

    private fun loadContent() {
        viewLifecycleOwner.lifecycleScope.launch {
            AppDatabase.waitForInitialization(requireContext())
            val db = AppDatabase.getInstance(requireContext())
            val songs = db.songDao().getAllSongs().map { entity ->
                Song(
                    id = entity.id,
                    title = entity.title,
                    artistName = entity.artistName,
                    imageResId = entity.imageResId,
                    duration = entity.duration,
                    rawResId = entity.rawResId,
                    sourceUri = entity.sourceUri
                )
            }

            allSongs = if (songs.isEmpty()) {
                RawAudioProvider.getSongs(requireContext())
            } else {
                songs
            }

            updateFavoriteIds(db)
            setupTrendingPlaylist()
            songAdapter.updateItems(allSongs)
            madeForYouAdapter.updateItems(RawAudioProvider.getRandomMadeForYouPlaylists(allSongs))
        }
    }

    private fun setupTrendingPlaylist() {
        if (allSongs.isEmpty()) return

        val playlistEntity = PlaylistEntity(
            id = "trending_album",
            title = "Trending",
            description = "Những bản hit đáng nghe nhất",
            imageResId = R.drawable.img1
        )
        val songEntities = allSongs.map { song ->
            SongEntity(
                id = song.id,
                title = song.title,
                artistName = song.artistName,
                imageResId = song.imageResId,
                duration = song.duration,
                rawResId = song.rawResId,
                sourceUri = song.sourceUri,
                isUploaded = false,
                ownerUsername = null
            )
        }

        trendingPlaylist = PlaylistWithSongs(
            playlist = playlistEntity,
            songs = songEntities
        )
        trendingAdapter.updateItems(listOf(trendingPlaylist))
    }

    private suspend fun updateFavoriteIds(db: AppDatabase) {
        val user = SessionManager.currentUser
        favoriteSongIds = if (user != null && !SessionManager.isGuest) {
            db.favoriteDao().getFavoriteSongIds(user.username).toSet()
        } else emptySet()
    }

    private fun filterSongs(query: String) {
        val filtered = if (query.isBlank()) {
            allSongs
        } else {
            allSongs.filter {
                it.title.contains(query, ignoreCase = true) ||
                    it.artistName.contains(query, ignoreCase = true)
            }
        }
        if (query.isBlank()) {
            binding.tvTrendingTitle.text = "Trending"
            binding.rvTrendingSongs.adapter = trendingAdapter
            if (::trendingPlaylist.isInitialized) {
                trendingAdapter.updateItems(listOf(trendingPlaylist))
            }
        } else {
            binding.tvTrendingTitle.text = "Kết quả tìm kiếm"
            binding.rvTrendingSongs.adapter = songAdapter
            songAdapter.updateItems(filtered)
        }
    }

    private fun playSong(song: Song, queue: List<Song> = allSongs) {
        PlaybackManager.play(song, queue)
        findNavController().navigate(R.id.action_discoverFragment_to_playerDetailFragment)
    }

    private fun showTrendingAlbumSongs(trendingSongsPlaylist: PlaylistWithSongs) {
        val dialogBinding = DialogAlbumSongsBinding.inflate(layoutInflater)
        dialogBinding.tvDialogAlbumTitle.text = trendingSongsPlaylist.playlist.title

        val songs = trendingSongsPlaylist.songs.map { entity ->
            Song(
                id = entity.id,
                title = entity.title,
                artistName = entity.artistName,
                imageResId = entity.imageResId,
                duration = entity.duration,
                rawResId = entity.rawResId,
                sourceUri = entity.sourceUri
            )
        }

        lateinit var dialog: AlertDialog
        val dialogAdapter = AlbumSongAdapter(songs) { selectedSong ->
            playSong(selectedSong, songs)
            dialog.dismiss()
        }

        dialogBinding.rvAlbumSongs.layoutManager = LinearLayoutManager(requireContext())
        dialogBinding.rvAlbumSongs.adapter = dialogAdapter

        dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setPositiveButton("Đóng", null)
            .show()
    }

    private fun toggleFavorite(song: Song) {
        val user = SessionManager.currentUser
        if (user == null || SessionManager.isGuest) {
            Toast.makeText(requireContext(), "Đăng nhập để yêu thích bài hát", Toast.LENGTH_SHORT).show()
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
            updateFavoriteIds(db)
            songAdapter.notifyDataSetChanged()
        }
    }

    private fun showPlaylistPicker(song: Song) {
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
                        db.playlistDao().addSongToPlaylist(
                            com.example.soundstream_app.model.PlaylistSongCrossRef(
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
                .show()
        }
    }

    private fun setupMiniPlayer() {
        binding.miniPlayer.btnMiniVolume.setOnClickListener {
            Toast.makeText(requireContext(), "Chức năng tắt tiếng đang phát triển", Toast.LENGTH_SHORT).show()
        }
        binding.miniPlayer.progressMini.progress = PlaybackManager.progressPercent
        refreshMiniPlayer()
    }

    private fun refreshMiniPlayer() {
        val current = PlaybackManager.currentSong
        if (current != null) {
            binding.miniPlayer.imgMiniSong.setImageResource(current.imageResId)
            binding.miniPlayer.tvMiniTitle.text = current.title
            binding.miniPlayer.tvMiniArtist.text = current.artistName
            binding.miniPlayer.btnMiniPlayPause.setImageResource(
                if (PlaybackManager.isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            )
            binding.miniPlayer.progressMini.progress = PlaybackManager.progressPercent
            binding.miniPlayer.miniPlayerContainer.setOnClickListener { playCurrentMiniSong() }
            binding.miniPlayer.btnMiniPlayPause.setOnClickListener {
                if (PlaybackManager.isPlaying) PlaybackManager.pause() else PlaybackManager.resume()
                refreshMiniPlayer()
            }
        } else {
            binding.miniPlayer.imgMiniSong.setImageResource(R.drawable.uth)
            binding.miniPlayer.tvMiniTitle.text = "Chọn bài hát"
            binding.miniPlayer.tvMiniArtist.text = getString(R.string.app_name)
            binding.miniPlayer.btnMiniPlayPause.setImageResource(android.R.drawable.ic_media_play)
            binding.miniPlayer.progressMini.progress = 0
            binding.miniPlayer.miniPlayerContainer.setOnClickListener(null)
            binding.miniPlayer.btnMiniPlayPause.setOnClickListener(null)
        }
    }

    private fun playCurrentMiniSong() {
        val current = PlaybackManager.currentSong ?: return
        findNavController().navigate(R.id.action_discoverFragment_to_playerDetailFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
