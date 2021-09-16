package com.ishant.musicify.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.ishant.musicify.data.remote.MusicDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/*
It takes some time to get music from firebase to our app. And in a service it would be difficult to implement waiting functionality
as some songs will load faster while others will load slow depending upon the size. Therefore we implemented a mechanism using this enum
class. It will have 4 states of our song request.
*/
enum class State {
    STATE_CREATED, // Our song request is created
    STATE_INITIALIZING, // Song has started to load from firebase
    STATE_INITIALIZED, // Song is loaded into our app and is ready to be played
    STATE_ERROR // An error occurred
}

// This class will help us get songs from firebase. It will take our MusicDatabase instance in its constructor using Dagger Hilt
class FirebaseMusicSource @Inject constructor(private val musicDatabase: MusicDatabase) {

    // It is an empty list of MediaMetadataCompat that consists of meta data about a song such as title, subtitle, mediaUri, iconUri etc
    var songs = emptyList<MediaMetadataCompat>()

    // A function to fetch media data from firebase. This function will run in a coroutine scope with IO (Input Output) context
    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = State.STATE_INITIALIZING // Since we are starting to initialize our song data, we will set the current state of our song to initializing
        val allSongs = musicDatabase.getAllSongs() // This variable will get all song metadata from our firebase firestore collection using our MusicDatabase instance that we passed in the constructor of this class

        // The map function will assign or map values from allSongs (Song) class to songs (MediaMetadataCompat) class
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST,song.subtitle)
                .putString(METADATA_KEY_MEDIA_ID,song.mediaId)
                .putString(METADATA_KEY_TITLE,song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE,song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI,song.imageUrl)
                .putString(METADATA_KEY_MEDIA_URI,song.songUrl)
                .putString(METADATA_KEY_ALBUM_ART_URI,song.imageUrl)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE,song.subtitle)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION,song.subtitle)
                .build()
        }
        state = State.STATE_INITIALIZED // Once initializion is done, we will set the state to state initialized
    }

    // This function will get one single media source object from the songs list that has MediaMetadataCompat class objects stored
    // A DefaultDataSourceFactory will help us get the song file through the url using file:// scheme
    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        // This is an instance of ConcatenatingMediaSource class that will concatenate or combine all the media sources (songs) together so that we can play them
        val concatenatingMediaSource = ConcatenatingMediaSource()

        // For each song, we will get the meta data detail and create a media source object out of it and add that media source in our concatenatingMediaSource list
        songs.forEach { song ->
            // ProgressiveMediaSource class will convert the meta data to the actual playable song media source object
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    // This function will get the song as a playable media item
    fun asMediaItem() = songs.map { song ->
        // This is a description variable that consists of song meta data that will be displayed on screen
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()

        // This is a playable media item from song object
        MediaBrowserCompat.MediaItem(desc,FLAG_PLAYABLE)
    }

    // This is kind of lambda expression. It will take a list of boolean variables and return a unit or nothing
    private val onReadyListener = mutableListOf<(Boolean)->Unit>()

    // It is the state variale that will tell us the state of our song request. At starting, we assign it as State Created that means that our song request has been created
    private var state: State = State.STATE_CREATED

    set(value) { // A setter function with State enum class value
        if(value == State.STATE_INITIALIZED || value == State.STATE_ERROR) {
            synchronized(onReadyListener) { // No other request can access onReadyListener when code in this block is running
                field = value // Our state variable will be assigned the value
                onReadyListener.forEach { listener ->
                    listener(state == State.STATE_INITIALIZED) // Check if state variable is initialized or not, then its results will be added to our onReadyListener list
                }
            }
        } else {
            field = value // Do nothing, simply the state variable will be assigned the value of setter set() function
        }
    }

    fun whenReady(action: (Boolean)->Unit): Boolean { // A function to check if our song is ready to be played or not
        if(state == State.STATE_CREATED || state == State.STATE_INITIALIZING) { // When created or initializing, add action to onReadyListener and return false
            onReadyListener += action
            return false
        } else { // Else set action's state to initialized and return true
            action(state == State.STATE_INITIALIZED)
            return true
        }
    }

}