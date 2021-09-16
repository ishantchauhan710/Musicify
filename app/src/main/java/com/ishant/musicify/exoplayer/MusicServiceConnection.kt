package com.ishant.musicify.exoplayer

import android.content.ComponentName
import android.content.Context
import android.media.browse.MediaBrowser
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ishant.musicify.other.Constants.NETWORK_ERROR
import com.ishant.musicify.other.Event
import com.ishant.musicify.other.Resource

// This class will connect our service with ViewModel
class MusicServiceConnection(context: Context) {
    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _isNetworkError = MutableLiveData<Event<Resource<Boolean>>>()
    val isNetworkError: LiveData<Event<Resource<Boolean>>> = _isNetworkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _curPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val curPlayingSong: LiveData<MediaMetadataCompat?> = _curPlayingSong

    // A MediaController will allow us to control the song such as play, pause, skip, forward, backward etc
    lateinit var mediaController: MediaControllerCompat

    // This is the instance of the callback we created below to handle internet network related stuff
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)

    // This will let us play the music through our service by establishing a network connection with songs and will work according to the network callback we have assigned to it
    private val mediaBrowser = MediaBrowserCompat(context, ComponentName(context, MusicService::class.java),
        mediaBrowserConnectionCallback, null).apply { connect() }

    // This will help us transport or change controls from one song to another
    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    // Function to start our service connection through media browser
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId,callback)
    }

    // Function to end our service connection through media browser
    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId,callback)
    }


    // This callback will help us call functions when network related things change such as when connection takes place, when connection is suspended or when connection fails
    private inner class MediaBrowserConnectionCallback(private val context: Context): MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            mediaController = MediaControllerCompat(context,mediaBrowser.sessionToken).apply {
               // Run the MediaControllerCallback() we created below
                registerCallback(MediaControllerCallback())
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
            _isConnected.postValue(Event(Resource.error("Connection Suspended",false)))
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
            _isConnected.postValue(Event(Resource.error("Connection Failed",null)))
        }
    }

    // This callback will let us handle the states of our song such as when playback is changed (play/pause), when metadata changes or when any session related event happens such as session fails due to network error
    private inner class MediaControllerCallback: MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _curPlayingSong.postValue(metadata)
        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            when(event) {
                NETWORK_ERROR -> _isNetworkError.postValue(Event(Resource.error("No Internet Available",null)))
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            // We will end the service's network connection of our media browser when a session is destroyed
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }

    }


}