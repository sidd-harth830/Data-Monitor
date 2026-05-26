package com.example.data

import kotlinx.coroutines.flow.Flow

class DataUsageRepository(private val dao: DataUsageDao) {
    val allRecords: Flow<List<DataUsageRecord>> = dao.getAllRecords()
    
    fun getRecentRecords(limit: Int): Flow<List<DataUsageRecord>> {
        return dao.getRecentRecords(limit)
    }

    suspend fun insertRecord(record: DataUsageRecord) {
        dao.insertRecord(record)
    }
    
    suspend fun getRecordByDate(dateStr: String): DataUsageRecord? {
        return dao.getRecordByDate(dateStr)
    }

    // Hourly usage logs
    fun getHourlyLogsForDate(dateStr: String): Flow<List<HourlyUsageLog>> {
        return dao.getHourlyLogsForDate(dateStr)
    }

    fun getAllHourlyLogs(): Flow<List<HourlyUsageLog>> {
        return dao.getAllHourlyLogs()
    }

    suspend fun insertHourlyLog(log: HourlyUsageLog) {
        dao.insertHourlyLog(log)
    }

    suspend fun insertHourlyLogs(logs: List<HourlyUsageLog>) {
        dao.insertHourlyLogs(logs)
    }

    fun getHourlyLogsForDates(dateStrings: List<String>): Flow<List<HourlyUsageLog>> {
        return dao.getHourlyLogsForDates(dateStrings)
    }

    // Aggregates for Profile/Analytics
    suspend fun getPeakUsageBytes(): Long? {
        return dao.getPeakUsageBytes()
    }

    suspend fun getAverageUsageBytes(): Double? {
        return dao.getAverageUsageBytes()
    }

    suspend fun getTotalUsageBytes(): Long? {
        return dao.getTotalUsageBytes()
    }
}
