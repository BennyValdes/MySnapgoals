package com.mysnapgoals.app

import android.app.Application
import com.airbnb.mvrx.Mavericks

class SnapGoalsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Mavericks.initialize(this)
        SnapGoalsGraph.init(this)
    }
}