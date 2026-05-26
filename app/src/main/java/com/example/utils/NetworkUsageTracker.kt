package com.example.utils

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.RemoteException
import java.util.Calendar

data class AppUsageInfo(
    val packageName: String,
    val appName: String,
    val bytes: Long
)

class NetworkUsageTracker(private val context: Context) {

    fun getTopAppsUsage(): List<AppUsageInfo> {
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val appUsageMap = mutableMapOf<Int, Long>()

        try {
            val stats = networkStatsManager.querySummary(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                val uid = bucket.uid
                val bytes = bucket.rxBytes + bucket.txBytes
                appUsageMap[uid] = appUsageMap.getOrDefault(uid, 0L) + bytes
            }
            stats.close()
            
            val mobileStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
            while (mobileStats.hasNextBucket()) {
                mobileStats.getNextBucket(bucket)
                val uid = bucket.uid
                val bytes = bucket.rxBytes + bucket.txBytes
                appUsageMap[uid] = appUsageMap.getOrDefault(uid, 0L) + bytes
            }
            mobileStats.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val pm = context.packageManager
        
        return appUsageMap.entries
            .filter { it.value > 0 }
            .mapNotNull { entry ->
                val uid = entry.key
                val packages = pm.getPackagesForUid(uid)
                if (!packages.isNullOrEmpty()) {
                    val packageName = packages[0]
                    try {
                        val appInfo = pm.getApplicationInfo(packageName, 0)
                        val appName = pm.getApplicationLabel(appInfo).toString()
                        AppUsageInfo(packageName, appName, entry.value)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                } else {
                    null
                }
            }
            .sortedByDescending { it.bytes }
            .take(5)
    }

    fun getTodayMobileUsage(): Long {
        return getUsage(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    fun getTodayWifiUsage(): Long {
        return getUsage(NetworkCapabilities.TRANSPORT_WIFI)
    }
    
    fun getUsageForDay(transportType: Int, startTime: Long, endTime: Long): Long {
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val subscriberId: String? = null // For API 28+, subscriberId is no longer needed/allowed for general queries if null is used properly, though it might throw SecurityException if not matched. Actually, for querying mobile data stats without subId, we can use null. Wait, API 28 requires it if we don't have READ_PHONE_STATE, but READ_PHONE_STATE is requested. However, since API 31 `null` corresponds to all.
        // Let's use ConnectivityManager.TYPE_WIFI or TYPE_MOBILE instead of NetworkCapabilities transports. No, querySummaryForDevice uses ConnectivityManager types.
        val networkType = if (transportType == NetworkCapabilities.TRANSPORT_WIFI) {
            ConnectivityManager.TYPE_WIFI
        } else {
            ConnectivityManager.TYPE_MOBILE
        }
        
        var bucket: NetworkStats.Bucket? = null
        try {
            bucket = networkStatsManager.querySummaryForDevice(networkType, null, startTime, endTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return (bucket?.rxBytes ?: 0L) + (bucket?.txBytes ?: 0L)
    }

    private fun getUsage(transportType: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        
        return getUsageForDay(transportType, startTime, endTime)
    }
}
