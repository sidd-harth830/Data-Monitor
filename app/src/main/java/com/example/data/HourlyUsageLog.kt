package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hourly_usage_log")
data class HourlyUsageLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // format: yyyy-MM-dd
    val hour: Int, // 0..23
    val mobileBytes: Long,
    val wifiBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
)
