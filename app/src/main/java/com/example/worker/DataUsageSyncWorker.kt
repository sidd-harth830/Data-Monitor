package com.siddharth.datamonitor.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.AppDatabase
import com.example.data.DataUsageRecord
import com.example.utils.NetworkUsageTracker
import com.example.ui.theme.ThemeManager
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
            val themeManager = ThemeManager(applicationContext)
            val alertsEnabled = themeManager.alertsEnabledFlow.first()
            val limitMBStr = themeManager.dataLimitFlow.first()
            val limitMB = limitMBStr.toLongOrNull() ?: 2000L
            val limitBytes = limitMB * 1024L * 1024L
            val totalBytes = mobileBytes + wifiBytes
            
            if (alertsEnabled && totalBytes > (limitBytes * 0.9)) {
                sendHighUsageNotification(totalBytes, limitBytes)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Result.success()
    }

    private fun sendHighUsageNotification(usedBytes: Long, limitBytes: Long) {
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
            .setContentTitle("⚠️ Data Usage Alert!")
            .setContentText("You have used $usedMB MB of $limitMB MB limit. That is above 90% of your plan.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(1001, notification)
    }
}
