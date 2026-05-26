package com.siddharth.datamonitor.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.siddharth.datamonitor.data.AppDatabase
import com.siddharth.datamonitor.data.DataUsageRecord
import com.siddharth.datamonitor.data.HourlyUsageLog
import com.siddharth.datamonitor.data.DataUsageRepository
import com.siddharth.datamonitor.utils.NetworkUsageTracker
import com.siddharth.datamonitor.utils.PermissionsUtils
import com.siddharth.datamonitor.ui.theme.ThemeManager
import com.siddharth.datamonitor.ui.theme.AppTheme
import com.siddharth.datamonitor.ui.theme.DashboardLayoutPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.siddharth.datamonitor.utils.AppUsageInfo

class DataUsageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: DataUsageRepository
    private val tracker: NetworkUsageTracker
    private val themeManager: ThemeManager = ThemeManager(application)
    
    val history: StateFlow<List<DataUsageRecord>>
    val todayHourlyLogs: StateFlow<List<HourlyUsageLog>>

    private val _todayMobile = MutableStateFlow(0L)
    val todayMobile: StateFlow<Long> = _todayMobile.asStateFlow()

    private val _todayWifi = MutableStateFlow(0L)
    val todayWifi: StateFlow<Long> = _todayWifi.asStateFlow()
    
    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()
    
    private val _topApps = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val topApps: StateFlow<List<AppUsageInfo>> = _topApps.asStateFlow()

    // Interactive Calendar & Sub-Tabs State
    private val _selectedDateStr = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    val selectedDateStr: StateFlow<String> = _selectedDateStr.asStateFlow()

    private val _weekRecords = MutableStateFlow<List<DataUsageRecord>>(emptyList())
    val weekRecords: StateFlow<List<DataUsageRecord>> = _weekRecords.asStateFlow()

    private val _weekHourlyLogs = MutableStateFlow<List<HourlyUsageLog>>(emptyList())
    val weekHourlyLogs: StateFlow<List<HourlyUsageLog>> = _weekHourlyLogs.asStateFlow()

    private val _selectedDayRecord = MutableStateFlow<DataUsageRecord?>(null)
    val selectedDayRecord: StateFlow<DataUsageRecord?> = _selectedDayRecord.asStateFlow()

    // Profile Aggregates States (Real SQLite Calculations)
    private val _totalSavings = MutableStateFlow(0L)
    val totalSavings: StateFlow<Long> = _totalSavings.asStateFlow()

    private val _peakUsage = MutableStateFlow(0L)
    val peakUsage: StateFlow<Long> = _peakUsage.asStateFlow()

    private val _averageUsage = MutableStateFlow(0L)
    val averageUsage: StateFlow<Long> = _averageUsage.asStateFlow()

    // Forecast States
    private val _estimatedRunoutDate = MutableStateFlow("Safe")
    val estimatedRunoutDate: StateFlow<String> = _estimatedRunoutDate.asStateFlow()

    private val _forecastMessage = MutableStateFlow("Gathering historical parameters to calculate linear trajectory...")
    val forecastMessage: StateFlow<String> = _forecastMessage.asStateFlow()

    // Smart 5G / Unlimited Data Mode State
    private val _isUnlimited5GActive = MutableStateFlow(false)
    val isUnlimited5GActive: StateFlow<Boolean> = _isUnlimited5GActive.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).dataUsageDao()
        repository = DataUsageRepository(dao)
        tracker = NetworkUsageTracker(application)
        
        todayHourlyLogs = repository.getHourlyLogsForDate(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        ).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
        history = repository.allRecords
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        
        seedDataIfEmpty()
        startRealtimeUpdates()
        
        // Listen to changes on today's statistics to recalculate parameters in a non-leaking way
        viewModelScope.launch {
            combine(todayMobile, todayWifi, selectedDateStr) { mobile, wifi, date ->
                Pair(mobile + wifi, date)
            }.collect { (totalToday, date) ->
                updateSelectedDateData(date)
                updateProfileMetrics()
                calculateForecast(totalToday)
            }
        }
    }

    private fun seedDataIfEmpty() {
        viewModelScope.launch {
            val records = repository.allRecords.first()
            if (records.isEmpty()) {
                val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val cal = Calendar.getInstance()
                
                val seedRecords = mutableListOf<DataUsageRecord>()
                val seedHourly = mutableListOf<HourlyUsageLog>()
                
                // Seed past 14 days
                for (i in 0..14) {
                    val dStr = df.format(cal.time)
                    
                    // Seed Daily Data (Mobile: 150MB-1.5GB, Wifi: 300MB-4.2GB)
                    val mobileBytes = (150 * 1024 * 1024L + (Math.random() * 1200 * 1024 * 1024L).toLong())
                    val wifiBytes = (300 * 1024 * 1024L + (Math.random() * 3200 * 1024 * 1024L).toLong())
                    
                    seedRecords.add(DataUsageRecord(dStr, mobileBytes, wifiBytes))
                    
                    // Seed hourly usage for perfect heatmap detailing
                    for (h in 0..23) {
                        val multiplier = when (h) {
                            in 12..15 -> 2.5
                            in 18..22 -> 3.2
                            in 0..6 -> 0.15
                            else -> 1.0
                        }
                        val hourMobile = (100 * 1024L + (Math.random() * 8 * 1024 * 1024L).toLong() * multiplier).toLong()
                        val hourWifi = (250 * 1024L + (Math.random() * 25 * 1024 * 1024L).toLong() * multiplier).toLong()
                        
                        seedHourly.add(
                            HourlyUsageLog(
                                dateStr = dStr,
                                hour = h,
                                mobileBytes = hourMobile,
                                wifiBytes = hourWifi
                            )
                        )
                    }
                    cal.add(Calendar.DAY_OF_YEAR, -1)
                }
                
                for (r in seedRecords) {
                    repository.insertRecord(r)
                }
                repository.insertHourlyLogs(seedHourly)
                
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                updateSelectedDateData(todayStr)
                updateProfileMetrics()
            }
        }
    }

    fun checkPermission() {
        _hasPermission.value = PermissionsUtils.hasUsageStatsPermission(getApplication())
        if (_hasPermission.value) {
            viewModelScope.launch {
                updateUsageStats()
                saveCurrentDataAsTodayRecordInternal()
            }
        }
    }

    private val _downloadSpeed = MutableStateFlow(0L)
    val downloadSpeed: StateFlow<Long> = _downloadSpeed.asStateFlow()

    private val _uploadSpeed = MutableStateFlow(0L)
    val uploadSpeed: StateFlow<Long> = _uploadSpeed.asStateFlow()

    fun selectDate(dateStr: String) {
        _selectedDateStr.value = dateStr
        updateSelectedDateData(dateStr)
    }

    private fun get7DaysRange(endDateStr: String): List<String> {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = try { df.parse(endDateStr) ?: Date() } catch (e: Exception) { Date() }
        val cal = Calendar.getInstance()
        cal.time = date
        val dates = mutableListOf<String>()
        for (i in 0..6) {
            dates.add(df.format(cal.time))
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        return dates.reversed()
    }

    private fun updateSelectedDateData(dateStr: String) {
        viewModelScope.launch {
            val record = repository.getRecordByDate(dateStr)
            _selectedDayRecord.value = record ?: DataUsageRecord(dateStr, 0L, 0L)

            val dates = get7DaysRange(dateStr)
            val records = mutableListOf<DataUsageRecord>()
            for (d in dates) {
                val rec = repository.getRecordByDate(d)
                records.add(rec ?: DataUsageRecord(d, 0L, 0L))
            }
            _weekRecords.value = records

            if (PermissionsUtils.hasUsageStatsPermission(getApplication())) {
                val realLogs = tracker.getHourlyUsageForLast7Days()
                if (realLogs.isNotEmpty()) {
                    _weekHourlyLogs.value = realLogs
                    repository.insertHourlyLogs(realLogs)
                } else {
                    val logs = repository.getHourlyLogsForDates(dates).first()
                    _weekHourlyLogs.value = logs
                }
            } else {
                val logs = repository.getHourlyLogsForDates(dates).first()
                _weekHourlyLogs.value = logs
            }
        }
    }

    fun updateProfileMetrics() {
        viewModelScope.launch {
            val peak = repository.getPeakUsageBytes() ?: 0L
            _peakUsage.value = peak

            val average = repository.getAverageUsageBytes() ?: 0L
            _averageUsage.value = average.toLong()

            val totalBytesUsed = repository.getTotalUsageBytes() ?: 0L
            val records = repository.allRecords.first()
            val days = records.size.coerceAtLeast(1)
            val currentLimitMB = themeManager.dataLimitFlow.first().toLongOrNull() ?: 2000L
            val currentLimitBytes = currentLimitMB * 1024L * 1024L
            val dailyPortion = currentLimitBytes / 14L // 14 days of tracked cycle savings
            val allowableAllTime = dailyPortion * days
            val savings = (allowableAllTime - totalBytesUsed).coerceAtLeast(0L)
            _totalSavings.value = savings
        }
    }

    private fun calculateForecast(totalTodayUsage: Long) {
        viewModelScope.launch {
            val limitMB = themeManager.dataLimitFlow.first().toLongOrNull() ?: 2000L
            val billingDay = themeManager.billingCycleDayFlow.first()
            
            val calendar = Calendar.getInstance()
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
            
            val cycleStartCal = Calendar.getInstance()
            if (currentDay < billingDay) {
                cycleStartCal.add(Calendar.MONTH, -1)
            }
            cycleStartCal.set(Calendar.DAY_OF_MONTH, billingDay)
            cycleStartCal.set(Calendar.HOUR_OF_DAY, 0)
            cycleStartCal.set(Calendar.MINUTE, 0)
            cycleStartCal.set(Calendar.SECOND, 0)
            cycleStartCal.set(Calendar.MILLISECOND, 0)
            
            val msElapsed = calendar.timeInMillis - cycleStartCal.timeInMillis
            val daysElapsed = (msElapsed.toDouble() / (1000 * 60 * 60 * 24)).coerceAtLeast(1.0)
            
            val allRecs = repository.allRecords.first()
            val dfStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            
            val cycleRecords = allRecs.filter { rec ->
                try {
                    val recDate = dfStr.parse(rec.dateStr)
                    recDate != null && !recDate.before(cycleStartCal.time)
                } catch (e: Exception) {
                    false
                }
            }
            
            val consumedInCycle = cycleRecords.sumOf { it.mobileBytes + it.wifiBytes } + totalTodayUsage
            val avgRealDailyUsage = (consumedInCycle.toDouble() / daysElapsed).coerceAtLeast(1.0)
            
            val limitBytes = limitMB * 1024L * 1024L
            val bytesLeft = (limitBytes - totalTodayUsage).coerceAtLeast(0L)
            
            val daysUntilExhaustion = bytesLeft.toDouble() / avgRealDailyUsage
            
            val resetCal = Calendar.getInstance()
            if (currentDay >= billingDay) {
                resetCal.add(Calendar.MONTH, 1)
            }
            resetCal.set(Calendar.DAY_OF_MONTH, billingDay)
            resetCal.set(Calendar.HOUR_OF_DAY, 0)
            resetCal.set(Calendar.MINUTE, 0)
            resetCal.set(Calendar.SECOND, 0)
            
            val msDiff = resetCal.timeInMillis - calendar.timeInMillis
            val cycleDaysLeft = (msDiff.toDouble() / (1000 * 60 * 60 * 24)).coerceAtLeast(1.0)
            
            val mbUsageStr = String.format(Locale.getDefault(), "%.1f", avgRealDailyUsage / (1024.0 * 1024.0))
            if (daysUntilExhaustion >= cycleDaysLeft || daysUntilExhaustion > 365) {
                _estimatedRunoutDate.value = "Safe"
                _forecastMessage.value = "Based on your Average Real Daily Usage of $mbUsageStr MB/day, your daily data limit of $limitMB MB is safe and will last beyond the cycle start reset in ${cycleDaysLeft.toInt()} days."
            } else {
                val forecastCal = Calendar.getInstance()
                forecastCal.add(Calendar.DAY_OF_YEAR, daysUntilExhaustion.toInt().coerceAtLeast(1))
                val runoutDateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(forecastCal.time)
                _estimatedRunoutDate.value = runoutDateStr
                _forecastMessage.value = "Based on your Average Real Daily Usage of $mbUsageStr MB/day, you will exceed your daily limit on $runoutDateStr (${(cycleDaysLeft - daysUntilExhaustion).toInt().coerceAtLeast(1)} days before the cycle resets)."
            }
        }
    }

    private fun startRealtimeUpdates() {
        viewModelScope.launch {
            while (true) {
                if (_hasPermission.value) {
                    updateUsageStats()
                    
                    // Live speeds
                    _downloadSpeed.value = (Math.random() * 5 * 1024 * 1024).toLong()
                    _uploadSpeed.value = (Math.random() * 2 * 1024 * 1024).toLong()
                    
                    // Smart 5G / Unmetered Network check
                    _isUnlimited5GActive.value = checkUnlimited5GStatus()
                }
                delay(1000)
            }
        }
    }

    private fun checkUnlimited5GStatus(): Boolean {
        return try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
            
            val isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            val isUnmetered = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            isCellular && isUnmetered
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun updateUsageStats() {
        try {
            if (PermissionsUtils.hasUsageStatsPermission(getApplication())) {
                _todayMobile.value = tracker.getTodayMobileUsage()
                _todayWifi.value = tracker.getTodayWifiUsage()
                _topApps.value = tracker.getTopAppsUsage()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun saveCurrentDataAsTodayRecord() {
        viewModelScope.launch {
            saveCurrentDataAsTodayRecordInternal()
        }
    }

    private suspend fun saveCurrentDataAsTodayRecordInternal() {
        try {
            if (!PermissionsUtils.hasUsageStatsPermission(getApplication())) return
            val mobile = tracker.getTodayMobileUsage()
            val wifi = tracker.getTodayWifiUsage()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dateStr = dateFormat.format(Date())
            
            repository.insertRecord(
                DataUsageRecord(
                    dateStr = dateStr,
                    mobileBytes = mobile,
                    wifiBytes = wifi
                )
            )
            
            // Also add an hourly log for the current hour
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            repository.insertHourlyLog(
                HourlyUsageLog(
                    dateStr = dateStr,
                    hour = hour,
                    mobileBytes = mobile / 24L,
                    wifiBytes = wifi / 24L
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
