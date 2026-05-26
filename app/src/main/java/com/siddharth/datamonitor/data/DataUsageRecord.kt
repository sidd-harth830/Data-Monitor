package com.siddharth.datamonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_usage")
data class DataUsageRecord(
    @PrimaryKey val dateStr: String, // format: yyyy-MM-dd
    val mobileBytes: Long,
    val wifiBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
)
