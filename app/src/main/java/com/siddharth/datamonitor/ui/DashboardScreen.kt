package com.siddharth.datamonitor.ui

import com.siddharth.datamonitor.utils.formatBytes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siddharth.datamonitor.ui.theme.*
import com.siddharth.datamonitor.utils.AppUsageInfo
import kotlinx.coroutines.launch
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView

private val iconCache = java.util.concurrent.ConcurrentHashMap<String, android.graphics.drawable.Drawable>()

@Composable
fun AppIconImage(packageName: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx -> ImageView(ctx).apply { scaleType = ImageView.ScaleType.FIT_CENTER } },
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        update = { imageView ->
            val cachedIcon = iconCache[packageName]
            if (cachedIcon != null) {
                imageView.setImageDrawable(cachedIcon)
            } else {
                try {
                    if (packageName == "uid_removed" || packageName == "uid_tethering" || packageName == "android") {
                        imageView.setImageResource(android.R.drawable.sym_def_app_icon)
                    } else {
                        val icon = imageView.context.packageManager.getApplicationIcon(packageName)
                        iconCache[packageName] = icon
                        imageView.setImageDrawable(icon)
                    }
                } catch (e: Exception) {
                    imageView.setImageResource(android.R.drawable.sym_def_app_icon)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val mobileUsage by viewModel.todayMobile.collectAsStateWithLifecycle()
    val wifiUsage by viewModel.todayWifi.collectAsStateWithLifecycle()
    val downloadSpeed by viewModel.downloadSpeed.collectAsStateWithLifecycle()
    val uploadSpeed by viewModel.uploadSpeed.collectAsStateWithLifecycle()
    val dataLimitMBStr by themeManager.dataLimitFlow.collectAsStateWithLifecycle(initialValue = "2000")
    val dailyDataLimitMBStr by themeManager.dailyDataLimitFlow.collectAsStateWithLifecycle(initialValue = "1000")
    val topApps by viewModel.topApps.collectAsStateWithLifecycle()
    
    val totalUsage = mobileUsage + wifiUsage
    val limitBytes = (dailyDataLimitMBStr.toLongOrNull() ?: 1000L) * 1024L * 1024L
    
    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

    var selectedFilter by remember { mutableStateOf("Today") }
    val filters = listOf("Today", "This Week", "This Month", "Wi-Fi Only", "Cellular Only")

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.checkPermission()
                kotlinx.coroutines.delay(800)
                isRefreshing = false
            }
        },
        state = pullToRefreshState,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 100.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DASHBOARD",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // ChipsRow Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                filters.forEach { filter ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color(0x1AFFFFFF))
                            .border(1.dp, if (isSelected) Color.Transparent else GlassStroke, RoundedCornerShape(20.dp))
                            .clickable {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                selectedFilter = filter
                            }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
            
            AiOptimizerCard(
                dataUsageViewModel = viewModel,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Main Data Card
            GlassCard(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "USAGE - $selectedFilter",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val displayUsage = when(selectedFilter) {
                        "Wi-Fi Only" -> wifiUsage
                        "Cellular Only" -> mobileUsage
                        else -> totalUsage
                    }
                    
                    Text(
                        text = formatBytes(displayUsage),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    val usageFraction = (displayUsage.toFloat() / limitBytes.toFloat()).coerceIn(0f, 1f)
                    val animatedFraction by animateFloatAsState(targetValue = usageFraction, animationSpec = tween(1000))
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val trackColor = MaterialTheme.colorScheme.surfaceVariant
                    
                    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                        drawRoundRect(
                            color = trackColor,
                            size = androidx.compose.ui.geometry.Size(width = size.width, height = size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2, size.height / 2)
                        )
                        if (animatedFraction > 0f) {
                            drawRoundRect(
                                color = primaryColor,
                                size = androidx.compose.ui.geometry.Size(width = size.width * animatedFraction, height = size.height),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2, size.height / 2)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("0 MB", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatBytes(limitBytes) + " Limit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Live Speeds
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("DOWNLOAD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(formatBytes(downloadSpeed) + "/s", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("UPLOAD", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(formatBytes(uploadSpeed) + "/s", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            // Top Apps List
            Text(
                text = "TOP APPLICATIONS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (topApps.isEmpty()) {
                        Text("No detailed app usage available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        topApps.take(10).forEach { app ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    AppIconImage(packageName = app.packageName, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = app.appName.ifBlank { app.packageName },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onBackground,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Bytes: ${formatBytes(app.bytes)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = formatBytes(app.bytes),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
