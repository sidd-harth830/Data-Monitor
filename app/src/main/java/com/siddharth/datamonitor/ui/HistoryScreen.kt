package com.siddharth.datamonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siddharth.datamonitor.ui.theme.*
import com.siddharth.datamonitor.ui.theme.ThemeManager
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Brush

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDateStr.collectAsStateWithLifecycle()
    
    val billingDay by themeManager.billingCycleDayFlow.collectAsStateWithLifecycle(initialValue = 1)
    val dataLimitMB by themeManager.dataLimitFlow.collectAsStateWithLifecycle(initialValue = "2000")
    val pingQualityLog by themeManager.pingQualityLogFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    
    // Day progress of the 30-day billing cycle
    val cycleDay = if (currentDay >= billingDay) {
        currentDay - billingDay + 1
    } else {
        val tempCal = Calendar.getInstance()
        tempCal.add(Calendar.MONTH, -1)
        val maxDaysInPrevMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        maxDaysInPrevMonth - billingDay + currentDay + 1
    }
    val cycleDayClamped = cycleDay.coerceIn(1, 30)
    val billingProgressPercent = (cycleDayClamped.toFloat() / 30f) * 100f
    
    // Accumulate total cycle usage bytes
    val cycleStartCal = Calendar.getInstance()
    if (currentDay < billingDay) {
        cycleStartCal.add(Calendar.MONTH, -1)
    }
    cycleStartCal.set(Calendar.DAY_OF_MONTH, billingDay)
    cycleStartCal.set(Calendar.HOUR_OF_DAY, 0)
    cycleStartCal.set(Calendar.MINUTE, 0)
    cycleStartCal.set(Calendar.SECOND, 0)
    cycleStartCal.set(Calendar.MILLISECOND, 0)
    
    val dfStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cycleRecords = history.filter { rec ->
        try {
            val recDate = dfStr.parse(rec.dateStr)
            recDate != null && !recDate.before(cycleStartCal.time)
        } catch (e: Exception) {
            false
        }
    }
    
    val mobileToday by viewModel.todayMobile.collectAsStateWithLifecycle()
    val wifiToday by viewModel.todayWifi.collectAsStateWithLifecycle()
    val totalToday = mobileToday + wifiToday
    
    val cycleBytes = cycleRecords.sumOf { it.mobileBytes + it.wifiBytes } + totalToday
    val cycleMB = cycleBytes / (1024f * 1024f)
    
    val limitMB = dataLimitMB.toFloatOrNull() ?: 2000f
    val quotaUsagePercent = if (limitMB > 0) ((cycleMB / limitMB) * 100f).coerceIn(0f, 100f) else 0f
    
    val pacingRatio = if (billingProgressPercent > 0) (quotaUsagePercent / billingProgressPercent) else 0f
    val (warningColor, warningLabel, pacingMessage) = when {
        quotaUsagePercent >= 100f -> Triple(
            MaterialTheme.colorScheme.error,
            "LIMIT EXCEEDED",
            "Warning: You have reached 100% of your current data threshold limit! All additional traffic is unmetered or metered at secondary premium rates."
        )
        pacingRatio > 1.25f -> Triple(
            MaterialTheme.colorScheme.error,
            "CRITICAL EXHAUSTION RISK",
            "High data burn rate! You have consumed ${String.format(Locale.getDefault(), "%.1f", quotaUsagePercent)}% of your limit, but only ${String.format(Locale.getDefault(), "%.1f", billingProgressPercent)}% of your billing cycle has passed. Reduce streaming to avoid early exhaustion."
        )
        pacingRatio > 1.0f -> Triple(
            MaterialTheme.colorScheme.tertiary,
            "PACING ALERT",
            "You are slightly ahead of your proportional cycle budget. You have used ${String.format(Locale.getDefault(), "%.1f", quotaUsagePercent)}% of data compared to ${String.format(Locale.getDefault(), "%.1f", billingProgressPercent)}% of the cycle. Lean on Wi-Fi."
        )
        else -> Triple(
            MaterialTheme.colorScheme.primary,
            "OPTIMAL PACE",
            "Excellent budget control! Your data consumption matches the progress of your billing cycle. You are paced to safely finish the month with data to spare."
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 110.dp, start = 24.dp, end = 24.dp, bottom = 100.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "ANALYTICS",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature 1: Data Burn Rate / Pacing Forecast
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Data Burn Rate / Pacing Forecast",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Billing Cycle Progress",
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Day $cycleDayClamped of 30",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Quota Used",
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f%% / %.0f MB", quotaUsagePercent, limitMB),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Overlay linear progress bars representing billing vs quota pacing
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(5.dp))
                ) {
                    // Quota Used fill
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(quotaUsagePercent / 100f)
                            .background(warningColor, RoundedCornerShape(5.dp))
                    )
                    
                    // Billing Cycle Day mark / overlay pointer
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(3.dp)
                            .fillMaxWidth(billingProgressPercent / 100f)
                            .background(Color.White.copy(alpha = 0.85f))
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Quota progress",
                        color = warningColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Cycle day cursor",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Color coded message badge
                Surface(
                    color = warningColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, warningColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(warningColor, RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = warningLabel,
                                color = warningColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = pacingMessage,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Feature 2: Network Quality Log
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Network Quality Log (Last 5 Pings)",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (pingQualityLog.isEmpty()) {
                    Text(
                        text = "Waiting for network parameters. Run live Ping latency tests from the Config settings tab to populate stability history logs.",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    pingQualityLog.forEachIndexed { index, pair ->
                        val timestamp = pair.first
                        val latency = pair.second
                        val timeLabel = formatRelativeTime(timestamp)
                        
                        val statusColor = if (latency in 0..100) Color.Green else if (latency in 101..300) Color.Yellow else Color.Red
                        val statusText = if (latency in 0..100) "Stable connection" else if (latency in 101..300) "Transient bufferbloat" else "Unstable packet delivery"
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(statusColor, RoundedCornerShape(5.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "$latency ms",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = statusText,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            Text(
                                text = timeLabel,
                                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                        
                        if (index < pingQualityLog.size - 1) {
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

// Relative timestamp helper for ping list
fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
