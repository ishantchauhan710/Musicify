package com.ishant.musicify.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.ishant.musicify.R
import com.ishant.musicify.adapters.SongAdapter
import com.ishant.musicify.databinding.FragmentHomeBinding
import com.ishant.musicify.other.Status
import com.ishant.musicify.ui.MusicifyActivity
import com.ishant.musicify.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    lateinit var mainViewModel: MainViewModel
    private lateinit var songAdapter: SongAdapter
    private lateinit var binding: FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        binding = FragmentHomeBinding.bind(view)

        songAdapter = SongAdapter((activity as MusicifyActivity).glide)

        setupRecyclerView()
        subscribeToObservers()

        // When recyclerview's adapter's any of the song items is clicked, play or pause that song
        songAdapter.setOnItemClickListener {
            mainViewModel.playOrToggleSong(it)
        }

    }

    private fun subscribeToObservers() {

        // Get list of all songs and send them to recyclerview adapter
        mainViewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when(result.status) {
                Status.LOADING -> binding.allSongsProgressBar.visibility = View.VISIBLE
                Status.ERROR -> Unit
                Status.SUCCESS -> {
                    binding.allSongsProgressBar.visibility = View.GONE
                    result.data?.let { songs ->
                        songAdapter.songs = songs
                    }
                }
            }
        }
    }

    // A function to set up our recyclerview
    private fun setupRecyclerView() = binding.rvSongs.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

}