package com.ishant.musicify.exoplayer

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.ishant.musicify.exoplayer.callbacks.MusicNotificationListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject

private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
// This is our Music Service class that will play the music and display the notification even when our app is minimized or screen is turned off
class MusicService: MediaBrowserServiceCompat() {

    @Inject // This annotation is used to call or inject something from modules that we created (AppModule or ServiceModule)
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private val serviceJob = Job() // It is a simple coroutine job to perform some task asynchronously

    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob) // It is our own serviceScope in a custom created context

    private lateinit var mediaSession: MediaSessionCompat // It is a media session that will be activated when media is played

    private lateinit var mediaSessionConnector: MediaSessionConnector // It will connect our media session to exoplayer

    private lateinit var musicNotificationManager: MusicNotificationManager // Notification Manager declared in our service

    var isForegroundService = false // Set service running to false initially

    override fun onCreate() {
        super.onCreate()

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

        // Our media session is finally connected to exoplayer
        mediaSessionConnector.setPlayer(exoPlayer)

        // Create the Notification Manager in our service class
        musicNotificationManager = MusicNotificationManager(
            this,mediaSession.sessionToken,MusicNotificationListener(this)
        ) { }

    }

    override fun onDestroy() {
        super.onDestroy()
        // We need to manually destroy or cancel our coroutine jobs when our service ends in order to prevent any memory leaks
        serviceScope.cancel()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }
}