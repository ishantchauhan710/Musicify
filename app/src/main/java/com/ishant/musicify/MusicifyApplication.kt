package com.ishant.musicify

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicifyApplication: Application()
// This is the application class of dagger hilt.
// It will provide us application context that can be used while creating modules
// A module is kind of a function or dependency that we can inject in our code (Dependency Injection)
// You also need to add this class under "android:name" under AndroidManifest <application----> tag