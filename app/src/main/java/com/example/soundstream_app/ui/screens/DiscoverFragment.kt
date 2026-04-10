package com.example.soundstream_app.ui.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.soundstream_app.R
import com.example.soundstream_app.data.MockDataProvider
import com.example.soundstream_app.databinding.FragmentDiscoverBinding
import com.example.soundstream_app.ui.components.ArtistAdapter
import com.example.soundstream_app.ui.components.FavoriteSongAdapter
import com.example.soundstream_app.ui.components.MadeForYouAdapter

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

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
        setupMiniPlayer()
    }

    private fun setupLists() {
        binding.rvMadeForYou.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvMadeForYou.adapter = MadeForYouAdapter(MockDataProvider.madeForYouPlaylists) {
            openDetail()
        }

        binding.rvPopularSingers.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvPopularSingers.adapter = ArtistAdapter(MockDataProvider.popularSingers)

        binding.rvFavoriteSongs.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFavoriteSongs.adapter = FavoriteSongAdapter(MockDataProvider.favoriteSongs) {
            openDetail()
        }
    }

    private fun setupMiniPlayer() {
        val current = MockDataProvider.nowPlaying
        binding.miniPlayer.imgMiniSong.setImageResource(current.imageResId)
        binding.miniPlayer.tvMiniTitle.text = current.title
        binding.miniPlayer.tvMiniArtist.text = current.artistName
        binding.miniPlayer.miniPlayerContainer.setOnClickListener { openDetail() }
    }

    private fun openDetail() {
        findNavController().navigate(R.id.action_discoverFragment_to_playerDetailFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
