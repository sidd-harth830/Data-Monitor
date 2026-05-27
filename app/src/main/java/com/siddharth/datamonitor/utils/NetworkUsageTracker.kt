package com.siddharth.datamonitor.utils

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
                val rawUid = bucket.uid
                val uid = if (rawUid < 0) -5 else rawUid
                val bytes = bucket.rxBytes + bucket.txBytes
                appUsageMap[uid] = appUsageMap.getOrDefault(uid, 0L) + bytes
            }
            stats.close()
            
            val mobileStats = networkStatsManager.querySummary(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
            while (mobileStats.hasNextBucket()) {
                mobileStats.getNextBucket(bucket)
                val rawUid = bucket.uid
                val uid = if (rawUid < 0) -5 else rawUid
                val bytes = bucket.rxBytes + bucket.txBytes
                appUsageMap[uid] = appUsageMap.getOrDefault(uid, 0L) + bytes
            }
            mobileStats.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        val pm = context.packageManager
        
        // Fetch all installed applications using MATCH_ALL to bypass visibility issues
        val installedApps = try {
            pm.getInstalledApplications(PackageManager.MATCH_ALL)
        } catch (e: Exception) {
            emptyList()
        }
        val uidToAppMap = installedApps.associateBy { it.uid }
        
        return appUsageMap.entries
            .filter { it.value > 0 }
            .mapNotNull { entry ->
                val uid = entry.key
                if (uid == -5) {
                    AppUsageInfo("system.tethering", "Mobile Hotspot / System", entry.value)
                } else {
                    val appInfo = uidToAppMap[uid]
                    if (appInfo != null) {
                        val packageName = appInfo.packageName
                        val appName = pm.getApplicationLabel(appInfo).toString()
                        AppUsageInfo(packageName, appName, entry.value)
                    } else {
                        val packages = pm.getPackagesForUid(uid)
                        if (!packages.isNullOrEmpty()) {
                            val packageName = packages[0]
                            try {
                                val appInfoFallback = pm.getApplicationInfo(packageName, 0)
                                val appName = pm.getApplicationLabel(appInfoFallback).toString()
                                AppUsageInfo(packageName, appName, entry.value)
                            } catch (e: PackageManager.NameNotFoundException) {
                                AppUsageInfo(packageName, packageName, entry.value)
                            }
                        } else {
                            null
                        }
                    }
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
        val networkType = if (transportType == NetworkCapabilities.TRANSPORT_WIFI) {
            ConnectivityManager.TYPE_WIFI
        } else {
            ConnectivityManager.TYPE_MOBILE
        }
        
        var totalBytes = 0L
        try {
            val stats = networkStatsManager.querySummary(networkType, null, startTime, endTime)
            val bucket = NetworkStats.Bucket()
            while (stats.hasNextBucket()) {
                stats.getNextBucket(bucket)
                totalBytes += bucket.rxBytes + bucket.txBytes
            }
            stats.close()
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                val bucketDevice = networkStatsManager.querySummaryForDevice(networkType, null, startTime, endTime)
                totalBytes = (bucketDevice?.rxBytes ?: 0L) + (bucketDevice?.txBytes ?: 0L)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return totalBytes
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

    fun getHourlyUsageForLast7Days(): List<com.siddharth.datamonitor.data.HourlyUsageLog> {
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val calendar = Calendar.getInstance()
        val endTime = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        val hourlyLogs = mutableMapOf<Pair<String, Int>, Pair<Long, Long>>() 
        val df = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())

        try {
            val wifiStats = networkStatsManager.queryDetails(ConnectivityManager.TYPE_WIFI, null, startTime, endTime)
            val bucket = NetworkStats.Bucket()
            while (wifiStats.hasNextBucket()) {
                wifiStats.getNextBucket(bucket)
                val time = bucket.startTimeStamp
                val bucketCal = Calendar.getInstance()
                bucketCal.timeInMillis = time
                val dateStr = df.format(bucketCal.time)
                val hour = bucketCal.get(Calendar.HOUR_OF_DAY)
                val bytes = bucket.rxBytes + bucket.txBytes
                
                val current = hourlyLogs.getOrDefault(Pair(dateStr, hour), Pair(0L, 0L))
                hourlyLogs[Pair(dateStr, hour)] = Pair(current.first, current.second + bytes)
            }
            wifiStats.close()

            val mobileStats = networkStatsManager.queryDetails(ConnectivityManager.TYPE_MOBILE, null, startTime, endTime)
            while (mobileStats.hasNextBucket()) {
                mobileStats.getNextBucket(bucket)
                val time = bucket.startTimeStamp
                val bucketCal = Calendar.getInstance()
                bucketCal.timeInMillis = time
                val dateStr = df.format(bucketCal.time)
                val hour = bucketCal.get(Calendar.HOUR_OF_DAY)
                val bytes = bucket.rxBytes + bucket.txBytes
                
                val current = hourlyLogs.getOrDefault(Pair(dateStr, hour), Pair(0L, 0L))
                hourlyLogs[Pair(dateStr, hour)] = Pair(current.first + bytes, current.second)
            }
            mobileStats.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val result = mutableListOf<com.siddharth.datamonitor.data.HourlyUsageLog>()
        val currentCal = Calendar.getInstance()
        currentCal.timeInMillis = startTime
        
        for (day in 0..6) {
            val dateStr = df.format(currentCal.time)
            for (hour in 0..23) {
                val bytes = hourlyLogs[Pair(dateStr, hour)]
                result.add(
                    com.siddharth.datamonitor.data.HourlyUsageLog(
                        dateStr = dateStr,
                        hour = hour,
                        mobileBytes = bytes?.first ?: 0L,
                        wifiBytes = bytes?.second ?: 0L
                    )
                )
            }
            currentCal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return result
    }
}
