package com.example.soundstream_app.ui.components

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundstream_app.databinding.ItemMadeForYouBinding
import com.example.soundstream_app.model.Playlist

class MadeForYouAdapter(
    private var items: List<Playlist>,
    private val onClick: (Playlist) -> Unit
) : RecyclerView.Adapter<MadeForYouAdapter.MadeForYouViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MadeForYouViewHolder {
        val binding = ItemMadeForYouBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MadeForYouViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MadeForYouViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<Playlist>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class MadeForYouViewHolder(
        private val binding: ItemMadeForYouBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Playlist) {
            binding.imgPlaylist.setImageResource(item.imageResId)
            binding.tvPlaylistTitle.text = item.title
            binding.tvPlaylistDescription.text = item.description
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
