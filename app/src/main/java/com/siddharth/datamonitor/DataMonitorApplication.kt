package com.siddharth.datamonitor

import android.app.Application
import androidx.work.Configuration

class DataMonitorApplication : Application(), Configuration.Provider {
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}

