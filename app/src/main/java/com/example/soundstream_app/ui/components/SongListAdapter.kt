package com.example.soundstream_app.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundstream_app.databinding.ItemSongListBinding
import com.example.soundstream_app.model.Song

class SongListAdapter(
    private var items: List<Song> = emptyList(),
    private val isFavorite: (Song) -> Boolean,
    private val onSongClick: (Song) -> Unit,
    private val onFavoriteToggle: (Song) -> Unit,
    private val onAddToPlaylist: (Song) -> Unit
) : RecyclerView.Adapter<SongListAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = ItemSongListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class SongViewHolder(
        private val binding: ItemSongListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Song) {
            binding.imgSong.setImageResource(item.imageResId)
            binding.tvSongTitle.text = item.title
            binding.tvSongArtist.text = item.artistName
            binding.root.setOnClickListener { onSongClick(item) }
            binding.btnFavorite.setImageResource(
                if (isFavorite(item)) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            binding.btnFavorite.setOnClickListener {
                onFavoriteToggle(item)
            }
            binding.btnAddToPlaylist.setOnClickListener {
                onAddToPlaylist(item)
            }
        }
    }
}
