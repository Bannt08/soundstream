package com.example.soundstream_app.ui.components

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundstream_app.databinding.ItemAlbumBinding
import com.example.soundstream_app.model.PlaylistWithSongs

class AlbumAdapter(
    private var items: List<PlaylistWithSongs> = emptyList(),
    private val onAlbumClick: (PlaylistWithSongs) -> Unit,
    private val onAlbumOptions: ((PlaylistWithSongs) -> Unit)? = null
) : RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<PlaylistWithSongs>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class AlbumViewHolder(
        private val binding: ItemAlbumBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlaylistWithSongs) {
            binding.tvAlbumTitle.text = item.playlist.title
            binding.tvAlbumSubtitle.text = binding.root.context.getString(
                com.example.soundstream_app.R.string.album_song_count,
                item.songs.size
            )
            binding.imgAlbum.setImageResource(item.playlist.imageResId)
            binding.root.setOnClickListener {
                onAlbumClick(item)
            }
            if (onAlbumOptions != null) {
                binding.btnAlbumOptions.visibility = View.VISIBLE
                binding.btnAlbumOptions.setOnClickListener {
                    onAlbumOptions.invoke(item)
                }
            } else {
                binding.btnAlbumOptions.visibility = View.GONE
            }
        }
    }
}
