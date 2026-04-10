package com.example.soundstream_app.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundstream_app.databinding.ItemFavoriteSongBinding
import com.example.soundstream_app.model.Song

class FavoriteSongAdapter(
    private val items: List<Song>,
    private val onClick: () -> Unit
) : RecyclerView.Adapter<FavoriteSongAdapter.FavoriteSongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteSongViewHolder {
        val binding = ItemFavoriteSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavoriteSongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteSongViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FavoriteSongViewHolder(
        private val binding: ItemFavoriteSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Song) {
            binding.imgSong.setImageResource(item.imageResId)
            binding.tvSongTitle.text = item.title
            binding.tvSongArtist.text = item.artistName
            binding.root.setOnClickListener { onClick() }
        }
    }
}
