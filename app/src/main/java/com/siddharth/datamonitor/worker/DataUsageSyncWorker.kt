package com.siddharth.datamonitor.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.siddharth.datamonitor.data.AppDatabase
import com.siddharth.datamonitor.data.DataUsageRecord
import com.siddharth.datamonitor.utils.NetworkUsageTracker
import com.siddharth.datamonitor.ui.theme.ThemeManager
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DataUsageSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val tracker = NetworkUsageTracker(applicationContext)
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endTime = calendar.timeInMillis

        val mobileBytes = tracker.getUsageForDay(android.net.NetworkCapabilities.TRANSPORT_CELLULAR, startTime, endTime)
        val wifiBytes = tracker.getUsageForDay(android.net.NetworkCapabilities.TRANSPORT_WIFI, startTime, endTime)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateStr = dateFormat.format(Date(startTime))

        val record = DataUsageRecord(
            dateStr = dateStr,
            mobileBytes = mobileBytes,
            wifiBytes = wifiBytes,
            timestamp = System.currentTimeMillis()
        )

        val db = AppDatabase.getDatabase(applicationContext)
        db.dataUsageDao().insertRecord(record)

        try {
            val prefs = db.userPreferencesDao().getPreferences().first() ?: com.siddharth.datamonitor.data.UserPreferences()
            
            if (prefs.alertsEnabled) {
                // Check daily limits
                val dailyLimitBytes = prefs.dailyLimitMb * 1024L * 1024L
                val totalDailyBytes = mobileBytes + wifiBytes
                
                if (totalDailyBytes >= dailyLimitBytes) {
                    sendHighUsageNotification("Daily", totalDailyBytes, dailyLimitBytes, "100%")
                } else if (totalDailyBytes >= dailyLimitBytes * 0.8) {
                    sendHighUsageNotification("Daily", totalDailyBytes, dailyLimitBytes, "80%")
                }
                
                // For monthly logic, we need total usage for the month
                val monthStart = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val currentEnd = System.currentTimeMillis()
                
                val monthlyMobile = tracker.getUsageForDay(android.net.NetworkCapabilities.TRANSPORT_CELLULAR, monthStart, currentEnd)
                val monthlyWifi = tracker.getUsageForDay(android.net.NetworkCapabilities.TRANSPORT_WIFI, monthStart, currentEnd)
                val totalMonthlyBytes = monthlyMobile + monthlyWifi
                val monthlyLimitBytes = prefs.monthlyLimitMb * 1024L * 1024L
                
                if (totalMonthlyBytes >= monthlyLimitBytes) {
                    sendHighUsageNotification("Monthly", totalMonthlyBytes, monthlyLimitBytes, "100%")
                } else if (totalMonthlyBytes >= monthlyLimitBytes * 0.8) {
                    sendHighUsageNotification("Monthly", totalMonthlyBytes, monthlyLimitBytes, "80%")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Result.success()
    }

    private fun sendHighUsageNotification(period: String, usedBytes: Long, limitBytes: Long, percentStr: String) {
        val context = applicationContext
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "data_limit_alerts_"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Data Limit Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warns when data limits are approaching thresholds"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val usedMB = usedBytes / (1024 * 1024)
        val limitMB = limitBytes / (1024 * 1024)
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("⚠️ $period Data Usage Alert!")
            .setContentText("You have used $usedMB MB of $limitMB MB limit. That is at/above $percentStr of your $period limit.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
