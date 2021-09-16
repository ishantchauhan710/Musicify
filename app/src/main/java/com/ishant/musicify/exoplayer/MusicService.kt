package com.ishant.musicify.exoplayer

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.ishant.musicify.exoplayer.callbacks.MusicNotificationListener
import com.ishant.musicify.exoplayer.callbacks.MusicPlaybackPreparer
import com.ishant.musicify.exoplayer.callbacks.MusicPlayerEventListener
import com.ishant.musicify.other.Constants.MEDIA_ROOT_ID
import com.ishant.musicify.other.Constants.NETWORK_ERROR
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
// This is our Music Service class that will play the music and display the notification even when our app is minimized or screen is turned off
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    @Inject // This annotation is used to call or inject something from modules that we created (AppModule or ServiceModule)
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject // This is the instance of our exoplayer that will play songs
    lateinit var exoPlayer: SimpleExoPlayer

    private val serviceJob = Job() // It is a simple coroutine job to perform some task asynchronously

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob) // It is our own serviceScope in a custom created context

    private lateinit var mediaSession: MediaSessionCompat // It is a media session that will be activated when media is played

    private var isPlayerInitialized = false // Check if exoplayer is initialized or not, initially we set it to false

    private lateinit var mediaSessionConnector: MediaSessionConnector // It will connect our media session to exoplayer

    private lateinit var musicNotificationManager: MusicNotificationManager // Notification Manager declared in our service

    private var curPlayingSong: MediaMetadataCompat? = null // This is current playing song object that will contain the meta data about currently playing song. Initially we set it to null

    var isForegroundService = false // Set service running to false initially

    private lateinit var musicPlayerEventListener: MusicPlayerEventListener // This is the listener we created that will handle player events such as when there is no internet or no song is being played when app is opened up
    companion object {
        var curSongDuration = 0L // We set the starting duration of our app to 00:00
            private set // This specifies that we can only change or set this curSongDuration variable's value through this service class only. From other classes we can see or read its value but we cannot change or set it.
    }

    override fun onCreate() {
        super.onCreate()

        // When onCreate() is called, first we load all the music metadata from our firebase database in our custom defined coroutine scope
        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        // Pending Intent is used to specify operations on any activity intent even when the activity is not created
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivities(this,0, arrayOf(it),0)
        }

        // Media Session Created
        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        // Our service has a session token that will hold value of media session token. Which means our session is created and initialized and started
        sessionToken = mediaSession.sessionToken

        // This is an instance of media session connector that is created using the mediaSession variable
        mediaSessionConnector = MediaSessionConnector(mediaSession)

        // Create the Notification Manager in our service class
        musicNotificationManager = MusicNotificationManager(
            this,mediaSession.sessionToken,MusicNotificationListener(this)
        ) { }

        // This is the callback we created. It will take the firebase object and return the song user wants to play and set curPlayingSong to the song that is returned
        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource) {
            // We got the song user wants to play through the lambda function call in our MusicPlaybackPreparer() class. The function is called when the exoplayer is prepared using mediaId
            curPlayingSong = it
            // Prepare the exoplayer
            preparePlayer(firebaseMusicSource.songs,it,true)
        }

        // Our mediaSessionConnector is assigned a musicPlaybackPreparer that will prepare the exoplayer using the song we want to play's mediaId
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        // We set the mediaSessionConnector's player to exoPlayer that we have received using DI Service Module
        mediaSessionConnector.setPlayer(exoPlayer)

        // Exoplayer will have an event listener that we created. It will check for events such as no internet or when no song is being played
        musicPlayerEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayerEventListener)

        // Through the Music Player Notification Manager that we created, we will display the notification
        musicNotificationManager.showNotification(exoPlayer)
        // We will play music in queue... One after another
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())

    }

    // This function will prepare the exoplayer
    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        // If current song is null, we set it to first song in our list (0 position) else we set it to the index of item that user wants to play music
        val curSongIndex = if(curPlayingSong==null) 0 else songs.indexOf(itemToPlay)
        // We set the media source of our exoplayer that is our firebaseMusicSource using the dataSourceFactory we injected using DI's ServiceModule
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        // Play the song and start from 00:00
        exoPlayer.seekTo(curSongIndex,0L)
        // When we want to play the music, we will set the function's playNow to true during function call
        exoPlayer.playWhenReady = playNow
    }

    override fun onDestroy() {
        super.onDestroy()
        // We need to manually destroy or cancel our coroutine jobs and remove all listeners and tasks of exoplayer when our service ends in order to prevent any memory leaks
        serviceScope.cancel()
        exoPlayer.removeListener(musicPlayerEventListener)
        exoPlayer.release()
    }

    // It is the function that will get the root of music source. You can have multiple roots as well such as in case you have multiple playlists or albums
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID,null)
    }

    // This function will load all songs in the root
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = firebaseMusicSource.whenReady { isInitialized ->
                    // When firebaseMusicSource has successfully loaded all data and the data is not empty, we prepare the exoPlayer with first song and initially we don't play it. We want user to play it when the song is clicked.
                    if(isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()) {
                        preparePlayer(firebaseMusicSource.songs,firebaseMusicSource.songs[0],false)
                        isPlayerInitialized = true
                    } else {
                        // Tell our media session that there is some network error
                        mediaSession.sendSessionEvent(NETWORK_ERROR,null)
                        // Else we send data as null
                        result.sendResult(null)
                    }
                }
                if(!resultsSent) {
                    // If no results are sent, that means no data is there so end this function
                    result.detach()
                }
            }
        }
    }

    // We get the description of currently playing song
    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

}