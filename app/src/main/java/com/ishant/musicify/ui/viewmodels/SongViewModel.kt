package com.ishant.musicify.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ishant.musicify.exoplayer.MusicService
import com.ishant.musicify.exoplayer.MusicServiceConnection
import com.ishant.musicify.exoplayer.currentPlaybackPosition
import com.ishant.musicify.other.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel @ViewModelInject constructor(val musicServiceConnection: MusicServiceConnection): ViewModel() {
    private val playbackState = musicServiceConnection.playbackState

    private var _curSongDuration = MutableLiveData<Long>()
    val curSongDuration = _curSongDuration

    private var _curPlayerPosition = MutableLiveData<Long>()
    val curPlayerPosition = _curPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    // Function to get current position of song
    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while(true) {
                val pos = playbackState.value?.currentPlaybackPosition // Extension variable we created in PlaybackStateCompatExt.kt
                if(curPlayerPosition.value!=pos) {
                    _curPlayerPosition.postValue(pos)
                    _curSongDuration.postValue(MusicService.curSongDuration)
                    delay(UPDATE_PLAYER_POSITION_INTERVAL)
                }
            }
        }
    }


}