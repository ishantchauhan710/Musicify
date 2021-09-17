package com.ishant.musicify.di

import android.content.Context
import androidx.core.app.ServiceCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.ishant.musicify.data.remote.MusicDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class) // This module will live as long as our service does
// In this object we define all the modules or instances that we need in our service
object ServiceModule {
    @ServiceScoped // You cannot use @Singleton in a service so you need to use @ServiceScoped which will create only one instance of our function
    @Provides
    // This function will provide us Exoplayer Audio Attributes that include Music Content Type and Media as Usage
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @ServiceScoped
    @Provides
    // This function will provide us an instance of Exoplayer
    fun provideExoPlayer(@ApplicationContext context: Context, audioAttributes: AudioAttributes) =
        SimpleExoPlayer.Builder(context).build().apply {
            setAudioAttributes(audioAttributes,true)
            setHandleAudioBecomingNoisy(true) // It will pause the audio whenever earphones are either plugged in or plugged out
        }

    @ServiceScoped
    @Provides
    // This function will provide us an Exoplayer Default Data Source Factory that consists of a user agent which will be used for requesting remote data
    fun provideDataSourceFactory(@ApplicationContext context: Context) = DefaultDataSourceFactory(context,
        Util.getUserAgent(context,"Musicify"))

    @ServiceScoped
    @Provides
    // This function will provide us an instance of our MusicDatabase class
    fun provideMusicDatabase() = MusicDatabase()
}