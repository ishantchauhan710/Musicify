package com.ishant.musicify.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import com.bumptech.glide.RequestManager
import com.ishant.musicify.R
import com.ishant.musicify.adapters.SwipeSongAdapter
import com.ishant.musicify.data.entities.Song
import com.ishant.musicify.databinding.ActivityMusicifyBinding
import com.ishant.musicify.exoplayer.toSong
import com.ishant.musicify.other.Status
import com.ishant.musicify.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MusicifyActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject // For Image Loading
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private lateinit var binding: ActivityMusicifyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMusicifyBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        swipeSongAdapter = SwipeSongAdapter(glide)

        subscribeToObservers()
        binding.vpSong.adapter = swipeSongAdapter

    }

    // It will set viewpager to current playing song
    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex!=-1) {
            binding.vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers() {

        // List of all songs
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songList ->
                            swipeSongAdapter.songs = songList
                            if(songList.isNotEmpty()) {
                                glide.load((curPlayingSong?:songList[0]).imageUrl).into(binding.ivCurSongImage)
                                switchViewPagerToCurrentSong(curPlayingSong?:return@observe)
                            }
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        // Information about currently playing song in MediaMetadataCompat format
        mainViewModel.curPlayingSong.observe(this) {
            if(it==null) return@observe

            curPlayingSong = it.toSong() // MediaMetadataCompat converted to our Song.kt class format using the extension function we created in MediaMetadataCompat.kt
            glide.load(curPlayingSong?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerToCurrentSong(curPlayingSong?:return@observe)

        }

    }

}