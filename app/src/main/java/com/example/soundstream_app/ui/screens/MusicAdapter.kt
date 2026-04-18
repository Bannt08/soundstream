package com.example.soundstream_app.ui.screens

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.soundstream_app.databinding.ItemMusicBinding
import com.example.soundstream_app.model.Song

class MusicAdapter(
    private var items: List<Song> = emptyList(),
    private val onClick: (Song) -> Unit = {}
) : RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemMusicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMusicBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = items[position]

        with(holder.binding) {
            tvTitle.text = song.title
            tvArtist.text = song.artistName
            imgCover.setImageResource(song.imageResId)
        }

        holder.itemView.setOnClickListener {
            onClick(song)
        }
    }

    fun submitList(newItems: List<Song>) {
        items = newItems
        notifyDataSetChanged()
    }
}