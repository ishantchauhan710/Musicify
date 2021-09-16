package com.ishant.musicify.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.ishant.musicify.exoplayer.MusicService

class MusicPlayerEventListener(private val musicService: MusicService): Player.EventListener {
    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicService,"Error: No Internet Connection Available", Toast.LENGTH_LONG).show()
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(playWhenReady, playbackState)
        if(playbackState == Player.STATE_READY && !playWhenReady) {
            musicService.stopForeground(false)
        }
    }

}