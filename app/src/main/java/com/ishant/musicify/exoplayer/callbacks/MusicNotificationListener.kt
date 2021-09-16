package com.ishant.musicify.exoplayer.callbacks

import android.app.Notification
import android.content.Intent
import androidx.core.content.ContextCompat
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.ishant.musicify.exoplayer.MusicService
import com.ishant.musicify.other.Constants.NOTIFICATION_ID

// This class will handle the actions of notification such as when notification appears or is cancelled or removed
class MusicNotificationListener(
    private val musicService: MusicService
): PlayerNotificationManager.NotificationListener {

    override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
        super.onNotificationCancelled(notificationId, dismissedByUser)
        musicService.apply {
            // When notification is removed, stop the service
            stopForeground(true)
            isForegroundService = false
            stopSelf()
        }
    }

    override fun onNotificationPosted(
        notificationId: Int,
        notification: Notification,
        ongoing: Boolean
    ) {
        super.onNotificationPosted(notificationId, notification, ongoing)
        musicService.apply {
            // Uf notification is displaying and service is disabled, then start our music service
            if(ongoing && !isForegroundService) {
                ContextCompat.startForegroundService(this, Intent(applicationContext,this::class.java))
                startForeground(NOTIFICATION_ID,notification)
                isForegroundService = true
            }
        }
    }
}