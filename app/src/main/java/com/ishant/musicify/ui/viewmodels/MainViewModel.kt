package com.ishant.musicify.ui.viewmodels

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ishant.musicify.data.entities.Song
import com.ishant.musicify.exoplayer.MusicServiceConnection
import com.ishant.musicify.exoplayer.isPlayEnabled
import com.ishant.musicify.exoplayer.isPlaying
import com.ishant.musicify.exoplayer.isPrepared
import com.ishant.musicify.other.Constants.MEDIA_ROOT_ID
import com.ishant.musicify.other.Resource

// In this class, we will implement UI related functions such as when to play, pause, skip etc
class MainViewModel @ViewModelInject constructor(
    val musicServiceConnection: MusicServiceConnection
): ViewModel() {
    // Currently it is an empty list of songs (Live Data)
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems:LiveData<Resource<List<Song>>> = _mediaItems

    // States of our songs that we created in service
    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.isNetworkError
    val curPlayingSong = musicServiceConnection.curPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        // At start, items will start loading. This will help us in displaying the loading progress bar
        _mediaItems.postValue(Resource.loading(null))

        // We subscribe or start the service that we passed in constructor of this class
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() {

            override fun onError(parentId: String) {
                super.onError(parentId)
                Log.e("IshantChauhan","Subscription Error: Parent ID is $parentId")
            }

            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                Log.e("IshantChauhan","OnChildrenLoaded called")
                super.onChildrenLoaded(parentId, children)
               // When the songs in MediaBrowserCompat are loaded, we get each of the song and map it to a Song.kt class's object.
                val items = children.map {
                    Song(it.mediaId!!,it.description.title.toString(),it.description.subtitle.toString(),it.description.mediaUri.toString(),it.description.iconUri.toString())
                }

                // The above "items" variable will return a list of songs (List<Song>)
                // Once we get all the songs in our item variable, we post it to _mediaItems live data variable along with success state. We can use success state to hide the progress bar
                _mediaItems.postValue(Resource.success(items))
            }
        })
    }

    // We can use the transportControls function in our musicServiceConnection to skip or seek songs
    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    // When the viewmodel is cleared, we will unsubscribe to our music service to prevent any memory leaks
    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() { })
    }

    // This function will play a song when no song is playing and will pause/play a song when in respective opposite states. Also, when the same song is clicked in recyclerview list that is already playing, we won't pause it or play it again from start
    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {
        val isPrepared = playbackState.value?.isPrepared ?: false
        if(isPrepared && mediaItem.mediaId==curPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            // When clicked first time, play the song
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId,null)
        }
    }




}