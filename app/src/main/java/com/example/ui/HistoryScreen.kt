package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DataUsageRecord
import com.example.data.HourlyUsageLog
import com.example.ui.theme.MobileActive
import com.example.ui.theme.PrimaryNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WifiActive
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.chart.line.LineChart.LineSpec
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: DataUsageViewModel) {
    val selectedDate by viewModel.selectedDateStr.collectAsStateWithLifecycle()
    val weekRecords by viewModel.weekRecords.collectAsStateWithLifecycle()
    val weekHourlyLogs by viewModel.weekHourlyLogs.collectAsStateWithLifecycle()
    val selectedDayRecord by viewModel.selectedDayRecord.collectAsStateWithLifecycle()
    
    val estimatedRunoutDate by viewModel.estimatedRunoutDate.collectAsStateWithLifecycle()
    val forecastMessage by viewModel.forecastMessage.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }

    val formattedDatePickerLabel = remember(selectedDate) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            parser.parse(selectedDate)?.let { formatter.format(it) } ?: selectedDate
        } catch (e: Exception) {
            selectedDate
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = try {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectedDate)?.time
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                        viewModel.selectDate(dateStr)
                    }
                    showDatePicker = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 100.dp)
    ) {
        Text(
            text = "ANALYTICS",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        // Date Selector Row (Wired to material DatePickerDialog)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassCard(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center, 
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "End of Cycle: $formattedDatePickerLabel", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 14.sp
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { showDatePicker = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text("📅", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Chart Area: weekly activity bytes
        GlassCard(modifier = Modifier.fillMaxWidth().height(260.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Weekly Activity (MB)",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (weekRecords.isNotEmpty()) {
                    val wifiEntries = weekRecords.mapIndexed { index, record ->
                        FloatEntry(x = index.toFloat(), y = bytesToMB(record.wifiBytes).toFloat())
                    }
                    val mobileEntries = weekRecords.mapIndexed { index, record ->
                        FloatEntry(x = index.toFloat(), y = bytesToMB(record.mobileBytes).toFloat())
                    }

                    if (wifiEntries.isNotEmpty()) {
                        val chartEntryModel = entryModelOf(wifiEntries, mobileEntries)
                        Chart(
                            chart = lineChart(
                                spacing = 32.dp,
                                lines = listOf(
                                    LineSpec(
                                        lineColor = WifiActive.toArgb(),
                                        lineBackgroundShader = verticalGradient(arrayOf(WifiActive.copy(alpha = 0.4f), Color.Transparent)),
                                        pointConnector = DefaultPointConnector(cubicStrength = 0.5f)
                                    ),
                                    LineSpec(
                                        lineColor = MobileActive.toArgb(),
                                        lineBackgroundShader = verticalGradient(arrayOf(MobileActive.copy(alpha = 0.4f), Color.Transparent)),
                                        pointConnector = DefaultPointConnector(cubicStrength = 0.5f)
                                    )
                                )
                            ),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(),
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No data logs detected", color = MaterialTheme.colorScheme.onSecondary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Donut Chart: Usage Breakdown
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Daily Usage Breakdown",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val wifiMB = selectedDayRecord?.wifiBytes?.let { bytesToMB(it).toFloat() } ?: 0f
                val mobileMB = selectedDayRecord?.mobileBytes?.let { bytesToMB(it).toFloat() } ?: 0f
                val roamingMB = mobileMB * 0.05f // Estimate roaming details

                DonutChart(wifi = wifiMB, mobile = mobileMB, roaming = roamingMB)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Heatmap: Hourly Tracker Matrix
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp), 
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Weekly Heatmap (24x7 Activity)",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                val rangeDates = remember(selectedDate) {
                    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = try { df.parse(selectedDate) ?: Date() } catch (e: Exception) { Date() }
                    val cal = Calendar.getInstance()
                    cal.time = date
                    val dates = mutableListOf<String>()
                    for (i in 0..6) {
                        dates.add(df.format(cal.time))
                        cal.add(Calendar.DAY_OF_YEAR, -1)
                    }
                    dates.reversed()
                }

                HourlyHeatmap(dates = rangeDates, logs = weekHourlyLogs)
                
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Out of Hours (Sleep)", color = MaterialTheme.colorScheme.onSecondary, fontSize = 11.sp)
                    Text("Peak Traffic Active", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Dynamic Exhaustion Forecasting regression
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Data Exhaustion Forecast",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val isExhaustionSafe = estimatedRunoutDate == "Safe"
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isExhaustionSafe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer, 
                                RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isExhaustionSafe) "✓" else "📉", fontSize = if (isExhaustionSafe) 18.sp else 24.sp, color = if (isExhaustionSafe) Color.Green else MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = if (isExhaustionSafe) "Status Trajectory" else "Estimated Runout Date",
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = estimatedRunoutDate,
                            color = if (isExhaustionSafe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = forecastMessage,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun DonutChart(wifi: Float, mobile: Float, roaming: Float) {
    val total = wifi + mobile + roaming
    
    var animationPlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val fraction by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "donutFraction"
    )

    val colorPrimary = MaterialTheme.colorScheme.primary
    val colorTertiary = MaterialTheme.colorScheme.tertiary
    val colorSecondary = MaterialTheme.colorScheme.secondary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (total <= 0f) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("0%", color = MaterialTheme.colorScheme.onSecondary, fontSize = 14.sp)
            }
        } else {
            val wifiAngleTarget = (wifi / total) * 360f
            val mobileAngleTarget = (mobile / total) * 360f
            val roamingAngleTarget = (roaming / total) * 360f

            val wifiAngle = wifiAngleTarget * fraction
            val mobileAngle = mobileAngleTarget * fraction
            val roamingAngle = roamingAngleTarget * fraction

            androidx.compose.foundation.Canvas(modifier = Modifier.size(100.dp)) {
                val strokeWidth = 30f
                drawArc(
                    color = colorTertiary,
                    startAngle = 0f,
                    sweepAngle = wifiAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
                drawArc(
                    color = colorPrimary,
                    startAngle = wifiAngle,
                    sweepAngle = mobileAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
                drawArc(
                    color = colorSecondary,
                    startAngle = wifiAngle + mobileAngle,
                    sweepAngle = roamingAngle,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(24.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            val wifiPct = if (total > 0) ((wifi / total) * 100).toInt() else 0
            val mobilePct = if (total > 0) ((mobile / total) * 100).toInt() else 0
            val roamingPct = if (total > 0) ((roaming / total) * 100).toInt() else 0

            LegendItem("Wi-Fi Connection", "$wifiPct%", colorTertiary)
            LegendItem("Cellular Data", "$mobilePct%", colorPrimary)
            LegendItem("Roaming Metrics", "$roamingPct%", colorSecondary)
        }
    }
}

@Composable
fun LegendItem(label: String, value: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(5.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
        Text(text = value, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

@Composable
fun HourlyHeatmap(dates: List<String>, logs: List<HourlyUsageLog>) {
    val colorPrimary = MaterialTheme.colorScheme.primary
    
    val logMap = remember(logs) {
        logs.associateBy { Pair(it.dateStr, it.hour) }
    }
    val maxBytes = remember(logs) {
        logs.maxOfOrNull { it.mobileBytes + it.wifiBytes }?.toFloat()?.coerceAtLeast(1f) ?: 1f
    }

    androidx.compose.foundation.Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(vertical = 8.dp)
    ) {
        val cols = 24
        val rows = 7
        val spacing = 4.dp.toPx()
        val cellWidth = (size.width - (cols - 1) * spacing) / cols
        val cellHeight = (size.height - (rows - 1) * spacing) / rows
        val cornerRadius = 2.dp.toPx()
        
        for (r in 0 until rows) {
            val d = dates.getOrNull(r) ?: ""
            for (c in 0 until cols) {
                val log = logMap[Pair(d, c)]
                val bytes = if (log != null) (log.mobileBytes + log.wifiBytes) else 0L
                
                val intensity = if (bytes == 0L) {
                    0.08f
                } else {
                    (bytes.toFloat() / maxBytes).coerceIn(0.12f, 1f)
                }
                
                val x = c * (cellWidth + spacing)
                val y = r * (cellHeight + spacing)
                
                drawRoundRect(
                    color = colorPrimary.copy(alpha = intensity),
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(cellWidth, cellHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius)
                )
            }
        }
    }
}
