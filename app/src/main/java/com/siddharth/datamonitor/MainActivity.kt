package com.siddharth.datamonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.siddharth.datamonitor.ui.DataUsageViewModel
import com.siddharth.datamonitor.ui.MainScreen
import com.siddharth.datamonitor.ui.theme.DataMonitorTheme
import com.siddharth.datamonitor.ui.theme.ThemeManager
import com.siddharth.datamonitor.ui.theme.DynamicThemeProvider
import com.siddharth.datamonitor.worker.DataUsageSyncWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    private val viewModel: DataUsageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Cleanly initialize Firebase if not already initialized
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            // Aggressive logging enabling
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Isolate work manager or early synchronization startup failures
        try {
            setupDailyWorker()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (inner: Exception) {
                inner.printStackTrace()
            }
        }
        setContent {
            val themeManager = remember { ThemeManager(this) }
            DynamicThemeProvider(themeManager = themeManager) {
                MainScreen(viewModel = viewModel, themeManager = themeManager)
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Save current data right away so we don't start empty, 
        // will only save if permitted.
        try {
            viewModel.checkPermission()
            
            // Post-delayed secondary check (500ms) to ensure settings updates propagate correctly
            window.decorView.postDelayed({
                try {
                    viewModel.checkPermission()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, 500)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (inner: Exception) {
                inner.printStackTrace()
            }
        }
    }

    private fun setupDailyWorker() {
        try {
            // Enqueue the daily worker
            val workRequest = PeriodicWorkRequestBuilder<DataUsageSyncWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                "daily_data_sync_worker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (inner: Exception) {
                inner.printStackTrace()
            }
        }
    }
}
