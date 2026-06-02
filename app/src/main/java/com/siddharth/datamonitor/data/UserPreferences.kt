package com.siddharth.datamonitor.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val id: Int = 1,
    val dailyLimitMb: Long = 1000L,
    val monthlyLimitMb: Long = 20000L,
    val alertsEnabled: Boolean = true
)
