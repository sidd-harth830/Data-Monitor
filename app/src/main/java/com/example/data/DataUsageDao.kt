package com.siddharth.datamonitor.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DataUsageDao {
    @Query("SELECT * FROM data_usage ORDER BY dateStr DESC")
    fun getAllRecords(): Flow<List<DataUsageRecord>>
    
    @Query("SELECT * FROM data_usage ORDER BY dateStr DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<DataUsageRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DataUsageRecord)
    
    @Query("SELECT * FROM data_usage WHERE dateStr = :dateStr LIMIT 1")
    suspend fun getRecordByDate(dateStr: String): DataUsageRecord?

    // Hourly usage logs
    @Query("SELECT * FROM hourly_usage_log WHERE dateStr = :dateStr ORDER BY hour ASC")
    fun getHourlyLogsForDate(dateStr: String): Flow<List<HourlyUsageLog>>

    @Query("SELECT * FROM hourly_usage_log ORDER BY dateStr DESC, hour DESC")
    fun getAllHourlyLogs(): Flow<List<HourlyUsageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyLog(log: HourlyUsageLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyLogs(logs: List<HourlyUsageLog>)

    @Query("SELECT * FROM hourly_usage_log WHERE dateStr IN (:dateStrings) ORDER BY dateStr ASC, hour ASC")
    fun getHourlyLogsForDates(dateStrings: List<String>): Flow<List<HourlyUsageLog>>

    // Aggregates for Profile/Analytics
    @Query("SELECT MAX(mobileBytes + wifiBytes) FROM data_usage")
    suspend fun getPeakUsageBytes(): Long?

    @Query("SELECT AVG(mobileBytes + wifiBytes) FROM data_usage")
    suspend fun getAverageUsageBytes(): Double?

    @Query("SELECT SUM(mobileBytes + wifiBytes) FROM data_usage")
    suspend fun getTotalUsageBytes(): Long?
}
