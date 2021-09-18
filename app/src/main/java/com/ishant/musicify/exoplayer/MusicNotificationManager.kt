package com.ishant.musicify.exoplayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.ishant.musicify.R
import com.ishant.musicify.other.Constants.NOTIFICATION_CHANNEL_ID
import com.ishant.musicify.other.Constants.NOTIFICATION_ID

// This class will create the music notification for us
class MusicNotificationManager(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token,
    notificationListener: PlayerNotificationManager.NotificationListener,
    private val newSongCallback: ()->Unit
) {
    // A variable to start media control system through the session token we provided in constructor
    val mediaController = MediaControllerCompat(context,sessionToken)

    // Here we create the notification manager
    private val notificationManager: PlayerNotificationManager
    init {
        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context,
            NOTIFICATION_CHANNEL_ID,
            R.string.notification_channel,
            R.string.notification_channel_description,
            NOTIFICATION_ID,
            DescriptionAdapter(mediaController),
            notificationListener).apply {
                setSmallIcon(R.drawable.ic_image)
                setMediaSessionToken(sessionToken)
        }
    }

    // We will call this function whenever we need to show the notification
    fun showNotification(player: Player) {
        notificationManager.setPlayer(player)
    }

    // This adapter will manage the data of song such as title, icon, intent etc
    private inner class DescriptionAdapter(private val mediaController: MediaControllerCompat): PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            newSongCallback()
            return mediaController.metadata.description.title.toString()
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return mediaController.metadata.description.subtitle.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            Glide.with(context).asBitmap().load(mediaController.metadata.description.iconUri).into(object: CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    // When the resource is ready or when our bitmap is fully loaded, then we will send it to our notification using this adapter
                    callback.onBitmap(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) = Unit
            })
            return null
        }
    }

}