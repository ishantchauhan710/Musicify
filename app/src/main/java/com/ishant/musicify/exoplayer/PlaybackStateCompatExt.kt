package com.ishant.musicify.exoplayer

import android.support.v4.media.session.PlaybackStateCompat

// We will use this class to define states for playOrToggle() function in our MainViewModel.kt
inline val PlaybackStateCompat.isPrepared
        get() = state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_PLAYING ||
                state == PlaybackStateCompat.STATE_PAUSED

inline val PlaybackStateCompat.isPlaying
        get() = state == PlaybackStateCompat.STATE_BUFFERING ||
                state == PlaybackStateCompat.STATE_PAUSED

inline val PlaybackStateCompat.isPlayEnabled
    get() = actions and PlaybackStateCompat.ACTION_PLAY != 0L ||
            (actions and PlaybackStateCompat.ACTION_PLAY_PAUSE != 0L && state == PlaybackStateCompat.STATE_PAUSED)