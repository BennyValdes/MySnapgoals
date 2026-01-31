package com.mysnapgoals.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SnapGoalsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
