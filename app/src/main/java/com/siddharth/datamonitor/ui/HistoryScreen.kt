package com.siddharth.datamonitor.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siddharth.datamonitor.data.DataUsageRecord
import com.siddharth.datamonitor.ui.theme.*
import com.siddharth.datamonitor.utils.formatBytes
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val weekRecords by viewModel.weekRecords.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
    ) {
        Text(
            text = "ANALYTICS",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (weekRecords.isNotEmpty()) {
            UsageChartCard(records = weekRecords.takeLast(7))
            Spacer(modifier = Modifier.height(24.dp))
        }

        if (history.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No usage limits exceeded or history found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "USAGE EXCEEDED LOGS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    history.forEach { record ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.dateStr,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Text("Cellular: ${formatBytes(record.mobileBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Wi-Fi: ${formatBytes(record.wifiBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text(
                                text = formatBytes(record.mobileBytes + record.wifiBytes),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsageChartCard(records: List<DataUsageRecord>) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "WEEKLY TRENDS",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val maxUsage = records.maxOfOrNull { it.mobileBytes + it.wifiBytes }?.coerceAtLeast(1024L) ?: 1024L
            val colorMobile = MaterialTheme.colorScheme.primary
            val colorWifi = MaterialTheme.colorScheme.tertiary ?: Color(0xFF00E676)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(vertical = 16.dp)
            ) {
                if (records.size > 1) {
                    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
                    var canvasWidth by remember { mutableStateOf(0f) }

                    Canvas(modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val stepX = size.width / (records.size - 1).coerceAtLeast(1)
                                    selectedPointIndex = (offset.x / stepX).roundToInt().coerceIn(0, records.size - 1)
                                },
                                onDrag = { change, _ ->
                                    val stepX = size.width / (records.size - 1).coerceAtLeast(1)
                                    selectedPointIndex = (change.position.x / stepX).roundToInt().coerceIn(0, records.size - 1)
                                },
                                onDragEnd = { selectedPointIndex = null },
                                onDragCancel = { selectedPointIndex = null }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val stepX = size.width / (records.size - 1).coerceAtLeast(1)
                                    selectedPointIndex = (offset.x / stepX).roundToInt().coerceIn(0, records.size - 1)
                                    tryAwaitRelease()
                                    selectedPointIndex = null
                                }
                            )
                        }
                    ) {
                        val width = size.width
                        canvasWidth = width
                        val height = size.height
                        val stepX = width / (records.size - 1)

                        // Draw Mobile Path
                        val mobilePath = Path()
                        records.forEachIndexed { index, record ->
                            val x = index * stepX
                            val normalizeY = 1f - (record.mobileBytes.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
                            val y = normalizeY * height
                            if (index == 0) mobilePath.moveTo(x, y) else mobilePath.lineTo(x, y)
                            drawCircle(colorMobile, radius = 4.dp.toPx(), center = Offset(x, y))
                        }
                        drawPath(
                            path = mobilePath,
                            color = colorMobile,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw Wi-Fi Path
                        val wifiPath = Path()
                        records.forEachIndexed { index, record ->
                            val x = index * stepX
                            val normalizeY = 1f - (record.wifiBytes.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
                            val y = normalizeY * height
                            if (index == 0) wifiPath.moveTo(x, y) else wifiPath.lineTo(x, y)
                            drawCircle(colorWifi, radius = 4.dp.toPx(), center = Offset(x, y))
                        }
                        drawPath(
                            path = wifiPath,
                            color = colorWifi,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw highlight for selected point
                        if (selectedPointIndex != null) {
                            val index = selectedPointIndex!!
                            val x = index * stepX
                            
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.5f),
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 1.dp.toPx()
                            )
                            
                            val mNormY = 1f - (records[index].mobileBytes.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
                            drawCircle(colorMobile, radius = 6.dp.toPx(), center = Offset(x, mNormY * height))
                            drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(x, mNormY * height))
                            
                            val wNormY = 1f - (records[index].wifiBytes.toFloat() / maxUsage.toFloat()).coerceIn(0f, 1f)
                            drawCircle(colorWifi, radius = 6.dp.toPx(), center = Offset(x, wNormY * height))
                            drawCircle(Color.White, radius = 3.dp.toPx(), center = Offset(x, wNormY * height))
                        }
                    }

                    // Tooltip box
                    if (selectedPointIndex != null && canvasWidth > 0) {
                        val index = selectedPointIndex!!
                        val record = records[index]
                        val stepX = canvasWidth / (records.size - 1)
                        val xOffset = index * stepX
                        
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(8.dp),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .offset {
                                    val tooltipWidthPx = 350f // approximate width mapping to px
                                    var xPos = xOffset - (tooltipWidthPx / 2)
                                    if (xPos < 0) xPos = 0f
                                    if (xPos > canvasWidth - tooltipWidthPx) xPos = canvasWidth - tooltipWidthPx
                                    IntOffset(xPos.toInt(), 0)
                                }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(record.dateStr, style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(colorMobile, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Cellular: ${formatBytes(record.mobileBytes)}", style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).background(colorWifi, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Wi-Fi: ${formatBytes(record.wifiBytes)}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        "Not enough data for chart.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(colorMobile, RoundedCornerShape(6.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cellular", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(modifier = Modifier.width(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(colorWifi, RoundedCornerShape(6.dp)))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Wi-Fi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

