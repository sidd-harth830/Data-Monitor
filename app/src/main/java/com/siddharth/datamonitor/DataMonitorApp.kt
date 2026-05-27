package com.siddharth.datamonitor

import android.app.Application
import androidx.work.Configuration
import com.google.firebase.FirebaseApp

class DataMonitorApp : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
