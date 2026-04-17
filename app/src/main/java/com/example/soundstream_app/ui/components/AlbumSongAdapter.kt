package com.example.soundstream_app.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundstream_app.databinding.ItemAlbumSongBinding
import com.example.soundstream_app.model.Song

class AlbumSongAdapter(
    private var items: List<Song> = emptyList(),
    private val onSongClick: (Song) -> Unit
) : RecyclerView.Adapter<AlbumSongAdapter.AlbumSongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumSongViewHolder {
        val binding = ItemAlbumSongBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumSongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumSongViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class AlbumSongViewHolder(
        private val binding: ItemAlbumSongBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Song) {
            binding.tvSongTitle.text = item.title
            binding.tvSongArtist.text = item.artistName
            binding.root.setOnClickListener {
                onSongClick(item)
            }
        }
    }
}
