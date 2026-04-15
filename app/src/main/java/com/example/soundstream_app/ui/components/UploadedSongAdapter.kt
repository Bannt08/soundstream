package com.example.soundstream_app.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundstream_app.databinding.ItemFavoriteSongBinding
import com.example.soundstream_app.model.Song

class UploadedSongAdapter(
    private var items: List<Song>
) : RecyclerView.Adapter<UploadedSongAdapter.UploadedSongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadedSongViewHolder {
        val binding = ItemFavoriteSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UploadedSongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UploadedSongViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class UploadedSongViewHolder(
        private val binding: ItemFavoriteSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Song) {
            binding.imgSong.setImageResource(item.imageResId)
            binding.tvSongTitle.text = item.title
            binding.tvSongArtist.text = item.artistName
        }
    }
}
