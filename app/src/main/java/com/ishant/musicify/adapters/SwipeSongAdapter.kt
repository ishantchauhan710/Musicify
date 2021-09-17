package com.ishant.musicify.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.ishant.musicify.data.entities.Song
import com.ishant.musicify.databinding.SongBinding
import com.ishant.musicify.databinding.SwippableSongBinding
import javax.inject.Inject


// This is the adapter for the viewpager we will see at the bottom of home fragment. It belongs to our activity
class SwipeSongAdapter @Inject constructor(private val glide: RequestManager): RecyclerView.Adapter<SwipeSongAdapter.SwipeSongViewHolder>() {

    inner class SwipeSongViewHolder(val binding: SwippableSongBinding): RecyclerView.ViewHolder(binding.root)

    // It will help in detecting the changed song items
    private val differCallback = object: DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this,differCallback)

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeSongViewHolder {
        val view = SwippableSongBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SwipeSongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SwipeSongViewHolder, position: Int) {
        val song = songs[position]
        val songText = "${song.title} - ${song.subtitle}"
        holder.binding.tvPrimary.text = songText

        holder.binding.root.setOnClickListener {
            onItemClickListener?.let { click ->
                click(song)
            }
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    // It will help us manage click events on a particular song item
    private var onItemClickListener: ((Song)->Unit) ?= null

    fun setOnItemClickListener(listener: (Song)->Unit) {
        onItemClickListener = listener
    }


}