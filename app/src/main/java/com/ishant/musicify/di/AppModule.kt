package com.ishant.musicify.di

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.ishant.musicify.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module // This annotation means that this file is a dagger hilt module that can be injected in our code when needed
@InstallIn(ApplicationComponent::class) // This module will live as long as our application does
// In this object we define all the modules or instances that we need in our application
object AppModule {
    @Singleton // It will have only one instance
    @Provides // Each module function needs to have @Provides annotation
    // This function will provide us an instance of Glide() which is an image loading library
    fun provideGlideInstance(@ApplicationContext context: Context) =
        Glide.with(context).setDefaultRequestOptions(RequestOptions()
             .placeholder(R.drawable.ic_image)
             .error(R.drawable.ic_image).diskCacheStrategy(DiskCacheStrategy.DATA))

}