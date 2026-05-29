package com.siddharth.datamonitor.utils

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

object PermissionsUtils {
    fun hasUsageStatsPermission(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpRaw(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            } else {
                @Suppress("DEPRECATION")
                appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    Process.myUid(),
                    context.packageName
                )
            }
            
            if (mode == AppOpsManager.MODE_ALLOWED) {
                true
            } else {
                // Secondary fallback check: Querying usage stats inside a small time window.
                // On some custom versions of Android or ROMs, AppOps returns MODE_DEFAULT,
                // but if we query some stats, it returns a non-empty list indicating it's allowed.
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
                val currentTime = System.currentTimeMillis()
                val stats = usageStatsManager.queryUsageStats(
                    android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                    currentTime - 60_000,
                    currentTime
                )
                !stats.isNullOrEmpty()
            }
        } catch (e: Throwable) {
            // Ultimate fallback check
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as android.app.usage.UsageStatsManager
                val currentTime = System.currentTimeMillis()
                val stats = usageStatsManager.queryUsageStats(
                    android.app.usage.UsageStatsManager.INTERVAL_DAILY,
                    currentTime - 60_000,
                    currentTime
                )
                !stats.isNullOrEmpty()
            } catch (ex: Throwable) {
                false
            }
        }
    }

    fun getUsageStatsIntent(): Intent {
        return Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
