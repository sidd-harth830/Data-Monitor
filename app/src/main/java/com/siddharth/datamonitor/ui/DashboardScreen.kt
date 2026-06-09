package com.siddharth.datamonitor.ui

import com.siddharth.datamonitor.utils.formatBytes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siddharth.datamonitor.ui.theme.*
import com.siddharth.datamonitor.ui.theme.ThemeManager
import com.siddharth.datamonitor.utils.AppUsageInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.ImageView
import java.util.Locale
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

private val iconCache = java.util.concurrent.ConcurrentHashMap<String, android.graphics.drawable.Drawable>()

@Composable
fun AppIconImage(packageName: String, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
        },
        modifier = modifier,
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
    val isUnlimited5GActive by viewModel.isUnlimited5GActive.collectAsStateWithLifecycle()
    val liveSpeeds by viewModel.liveSpeeds.collectAsStateWithLifecycle()
    
    val dashboardLayout by themeManager.dashboardLayoutFlow.collectAsStateWithLifecycle(initialValue = DashboardLayoutPreference.STANDARD)

    val totalUsage = mobileUsage + wifiUsage
    val dataLimitBytes = (dataLimitMBStr.toLongOrNull() ?: 2000L) * 1024L * 1024L
    val bytesLeft = (dataLimitBytes - totalUsage).coerceAtLeast(0L)

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()

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
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
                .navigationBarsPadding()
        ) {
            // Header (Greeting/Authentication badges)
            val auth = remember { com.google.firebase.auth.FirebaseAuth.getInstance() }
            val currentUser = auth.currentUser
            val userEmail = currentUser?.email
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello,",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    val greetingName = remember(currentUser, userEmail) {
                        val displayName = currentUser?.displayName
                        if (!displayName.isNullOrBlank()) {
                            displayName
                        } else if (!userEmail.isNullOrBlank()) {
                            val scrubbed = userEmail.substringBefore("@").replace(Regex("[0-9._-]+"), " ")
                            val cleaned = scrubbed.trim().replace(Regex("\\s+"), " ")
                            if (cleaned.isBlank()) {
                                "Explorer"
                            } else {
                                cleaned.split(" ").joinToString(" ") { word ->
                                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                                }
                            }
                        } else {
                            "Explorer"
                        }
                    }

                    Text(
                        text = greetingName,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                // Premium elegant auth status badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (userEmail != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (userEmail != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    color = if (userEmail != null) MaterialTheme.colorScheme.primary else Color(0xFFFFB300),
                                    shape = RoundedCornerShape(3.dp)
                                )
                        )
                        Text(
                            text = if (userEmail != null) "SYNC REGISTRY" else "LOCAL ACCOUNT",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (userEmail != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            if (isUnlimited5GActive) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "⚡ UNLIMITED 5G CARRIER DETECTED",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))

            when (dashboardLayout) {
                DashboardLayoutPreference.STANDARD -> {
                    // Daily Data Limit tracker (placed perfectly for Standard)
                    DailyDataLimitTracker(
                        todayUsageBytes = mobileUsage,
                        dailyLimitMBStr = dailyDataLimitMBStr,
                        onSaveLimit = { limit ->
                            scope.launch {
                                themeManager.setDailyDataLimit(limit)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "DETAILED GATEWAY METRICS",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DashboardLayoutStandard(
                        mobileUsage = mobileUsage,
                        wifiUsage = wifiUsage,
                        downloadSpeed = downloadSpeed,
                        uploadSpeed = uploadSpeed,
                        dataLimitBytes = dataLimitBytes,
                        topApps = topApps,
                        isUnlimited5GActive = isUnlimited5GActive,
                        bytesLeft = bytesLeft
                    )
                }
                DashboardLayoutPreference.PRO -> {
                    // Pro Wave Graph (Main Analytics Engine as a standalone premium card below the header)
                    Text(
                        text = "REAL-TIME SPECTRUM WAVE CHART",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    AnalyticalWaveChart(
                        liveSpeeds = liveSpeeds,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daily Data Limit tracker
                    DailyDataLimitTracker(
                        todayUsageBytes = mobileUsage,
                        dailyLimitMBStr = dailyDataLimitMBStr,
                        onSaveLimit = { limit ->
                            scope.launch {
                                themeManager.setDailyDataLimit(limit)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "DETAILED GATEWAY METRICS",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DashboardLayoutPro(
                        mobileUsage = mobileUsage,
                        wifiUsage = wifiUsage,
                        downloadSpeed = downloadSpeed,
                        uploadSpeed = uploadSpeed,
                        dataLimitBytes = dataLimitBytes,
                        topApps = topApps,
                        isUnlimited5GActive = isUnlimited5GActive,
                        bytesLeft = bytesLeft
                    )
                }
                DashboardLayoutPreference.GRID -> {
                    val todayHourlyLogs by viewModel.todayHourlyLogs.collectAsStateWithLifecycle(initialValue = emptyList())

                    // Daily Data Limit tracker
                    DailyDataLimitTracker(
                        todayUsageBytes = mobileUsage,
                        dailyLimitMBStr = dailyDataLimitMBStr,
                        onSaveLimit = { limit ->
                            scope.launch {
                                themeManager.setDailyDataLimit(limit)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "DETAILED GATEWAY METRICS",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    DashboardLayoutGrid(
                        mobileUsage = mobileUsage,
                        wifiUsage = wifiUsage,
                        downloadSpeed = downloadSpeed,
                        uploadSpeed = uploadSpeed,
                        dataLimitBytes = dataLimitBytes,
                        topApps = topApps,
                        isUnlimited5GActive = isUnlimited5GActive,
                        bytesLeft = bytesLeft,
                        todayHourlyLogs = todayHourlyLogs
                    )
                }
            }
        }
}
}

@Composable
fun DashboardLayoutStandard(
    mobileUsage: Long,
    wifiUsage: Long,
    downloadSpeed: Long,
    uploadSpeed: Long,
    dataLimitBytes: Long,
    topApps: List<AppUsageInfo>,
    isUnlimited5GActive: Boolean,
    bytesLeft: Long
) {
    Column {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            UsageRing(
                mobileBytes = mobileUsage,
                wifiBytes = wifiUsage,
                dataLimitBytes = dataLimitBytes,
                is5G = isUnlimited5GActive
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        // Data Left Card
        GlassCard(modifier = Modifier.fillMaxWidth().height(84.dp)) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isUnlimited5GActive) "CURRENT NETWORK PLAN" else "ESTIMATED DATA LEFT",
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isUnlimited5GActive) "Bypassing warnings on 5G" else "Resets inside monthly cycle", 
                        color = MaterialTheme.colorScheme.onSecondary, 
                    )
                }
                Text(
                    text = if (isUnlimited5GActive) "UNLIMITED" else formatBytes(bytesLeft),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "LIVE NETWORK SPEED",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LiveSpeedCard(
                title = "DOWNLOAD",
                speedColor = MaterialTheme.colorScheme.primary,
                speedBytes = downloadSpeed,
                modifier = Modifier.weight(1f)
            )
            LiveSpeedCard(
                title = "UPLOAD",
                speedColor = MaterialTheme.colorScheme.onSurface,
                speedBytes = uploadSpeed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "TOP DATA HUNGRY APPS",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (topApps.isEmpty()) {
                    Text(
                        text = "Calculating system metrics...",
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    topApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AppIconImage(
                                packageName = app.packageName,
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { (app.bytes.toFloat() / dataLimitBytes.toFloat()).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = formatBytes(app.bytes),
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardLayoutPro(
    mobileUsage: Long,
    wifiUsage: Long,
    downloadSpeed: Long,
    uploadSpeed: Long,
    dataLimitBytes: Long,
    topApps: List<AppUsageInfo>,
    isUnlimited5GActive: Boolean,
    bytesLeft: Long
) {
    Column {
        // High density analytics splits card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Detailed data splits in horizontal layout (removed duplication of Wave chart)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "MOBILE BILLING CYCLE", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary, 
                        )
                        Text(
                            text = formatBytes(mobileUsage), 
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground, 
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "LOCAL WI-FI ENGINE", 
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondary, 
                        )
                        Text(
                            text = formatBytes(wifiUsage), 
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground, 
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Grid Analytics Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // First Stats Card
            GlassCard(modifier = Modifier.weight(1f).wrapContentHeight()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "TOTAL BANDWIDTH",
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatBytes(mobileUsage + wifiUsage),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calculated daily log",
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Second Stats Card
            GlassCard(modifier = Modifier.weight(1f).wrapContentHeight()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.Center) {
                    Text(
                        text = "REMAINING",
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isUnlimited5GActive) "UNLIMITED" else formatBytes(bytesLeft),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isUnlimited5GActive) "5G network bypass" else "Before hard limit",
                        color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Speed Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LiveSpeedCard(
                title = "DOWNLOAD RX",
                speedColor = MaterialTheme.colorScheme.primary,
                speedBytes = downloadSpeed,
                modifier = Modifier.weight(1f)
            )
            LiveSpeedCard(
                title = "UPLOAD TX",
                speedColor = MaterialTheme.colorScheme.onSurface,
                speedBytes = uploadSpeed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Top Apps Inside Clean Table Grid
        Text(
            text = "DENSE BANDWIDTH DISPATCH",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (topApps.isEmpty()) {
                    Text(
                        text = "Analyzing diagnostic threads...",
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    topApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppIconImage(
                                packageName = app.packageName,
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = app.appName,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = formatBytes(app.bytes),
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val ratio = (app.bytes.toFloat() / dataLimitBytes.toFloat()).coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { ratio },
                                    modifier = Modifier.fillMaxWidth().height(3.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyticalWaveChart(
    liveSpeeds: List<Pair<Long, Long>>?,
    modifier: Modifier = Modifier
) {
    if (liveSpeeds == null) {
        GlassCard(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shimmerEffect()
            )
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.tertiary

    val isLight = MaterialTheme.colorScheme.background.red > 0.5f

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val midY = height / 2f
            
            // Draw grid lines - highly clear contrast and beautifully soft
            for (i in 1..4) {
                val y = height * (i / 5f)
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(0f, y), end = androidx.compose.ui.geometry.Offset(width, y), strokeWidth = 1.dp.toPx())
            }
            for (i in 1..9) {
                val x = width * (i / 10f)
                drawLine(gridColor, start = androidx.compose.ui.geometry.Offset(x, 0f), end = androidx.compose.ui.geometry.Offset(x, height), strokeWidth = 1.dp.toPx())
            }

            if (liveSpeeds.isEmpty()) return@Canvas

            val mapSize = 30
            val paddedSpeeds = if (liveSpeeds.size < mapSize) {
                List(mapSize - liveSpeeds.size) { Pair(0L, 0L) } + liveSpeeds
            } else {
                liveSpeeds.takeLast(mapSize)
            }

            val maxDown = paddedSpeeds.maxOfOrNull { it.first }?.coerceAtLeast(1024L) ?: 1024L
            val maxUp = paddedSpeeds.maxOfOrNull { it.second }?.coerceAtLeast(1024L) ?: 1024L

            // Draw Download curve (Primary path)
            val path1 = androidx.compose.ui.graphics.Path()
            val startY1 = height - (paddedSpeeds[0].first.toFloat() / maxDown.toFloat()) * (height * 0.8f) - 5.dp.toPx()
            path1.moveTo(0f, startY1)

            for (i in 1 until mapSize) {
                val x = i * (width / (mapSize - 1).toFloat())
                val y = height - (paddedSpeeds[i].first.toFloat() / maxDown.toFloat()) * (height * 0.8f) - 5.dp.toPx()
                
                val prevX = (i - 1) * (width / (mapSize - 1).toFloat())
                val prevY = height - (paddedSpeeds[i - 1].first.toFloat() / maxDown.toFloat()) * (height * 0.8f) - 5.dp.toPx()
                
                val controlX1 = prevX + (x - prevX) / 2f
                val controlX2 = prevX + (x - prevX) / 2f
                
                path1.cubicTo(controlX1, prevY, controlX2, y, x, y)
            }
            drawPath(
                path = path1,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Draw Upload curve (Secondary path)
            val path2 = androidx.compose.ui.graphics.Path()
            val startY2 = height - (paddedSpeeds[0].second.toFloat() / maxUp.toFloat()) * (height * 0.8f) - 5.dp.toPx()
            path2.moveTo(0f, startY2)

            for (i in 1 until mapSize) {
                val x = i * (width / (mapSize - 1).toFloat())
                val y = height - (paddedSpeeds[i].second.toFloat() / maxUp.toFloat()) * (height * 0.8f) - 5.dp.toPx()
                
                val prevX = (i - 1) * (width / (mapSize - 1).toFloat())
                val prevY = height - (paddedSpeeds[i - 1].second.toFloat() / maxUp.toFloat()) * (height * 0.8f) - 5.dp.toPx()
                
                val controlX1 = prevX + (x - prevX) / 2f
                val controlX2 = prevX + (x - prevX) / 2f
                
                path2.cubicTo(controlX1, prevY, controlX2, y, x, y)
            }
            drawPath(
                path = path2,
                color = secondaryColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun DashboardLayoutGrid(
    mobileUsage: Long,
    wifiUsage: Long,
    downloadSpeed: Long,
    uploadSpeed: Long,
    dataLimitBytes: Long,
    topApps: List<AppUsageInfo>,
    isUnlimited5GActive: Boolean,
    bytesLeft: Long,
    todayHourlyLogs: List<com.siddharth.datamonitor.data.HourlyUsageLog>
) {
    Column {
        Text(
            text = "DENSE SYSTEM GRID",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 2x3 Block Grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GridBlockCard(
                    title = "MOBILE INBOUND",
                    value = formatBytes(mobileUsage),
                    subtitle = "SIM cellular carrier",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                GridBlockCard(
                    title = "WI-FI TELEMETRY",
                    value = formatBytes(wifiUsage),
                    subtitle = "Local wireless network",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GridBlockCard(
                    title = "DOWNLOAD SPEED",
                    value = "${formatSpeed(downloadSpeed)}/s",
                    subtitle = "Live receiver rate",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                GridBlockCard(
                    title = "UPLOAD SPEED",
                    value = "${formatSpeed(uploadSpeed)}/s",
                    subtitle = "Live sender rate",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                GridBlockCard(
                    title = "REMAINING CYCLES",
                    value = if (isUnlimited5GActive) "UNLIMITED" else formatBytes(bytesLeft),
                    subtitle = "Remaining plan space",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                GridBlockCard(
                    title = "TOTAL ALL-TIME",
                    value = formatBytes(mobileUsage + wifiUsage),
                    subtitle = "Combined bandwidth",
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "FAST DISPATCH STREAM",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (topApps.isEmpty()) {
                    Text(
                        text = "Analyzing grid stream...",
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.padding(8.dp)
                    )
                } else {
                    topApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AppIconImage(
                                packageName = app.packageName,
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                                    .padding(4.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = app.appName,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = formatBytes(app.bytes),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridBlockCard(
    title: String,
    value: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier.height(96.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSecondary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = color,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSecondary,
            )
        }
    }
}

@Composable
fun LiveSpeedCard(title: String, speedColor: androidx.compose.ui.graphics.Color, speedBytes: Long, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.height(100.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatSpeed(speedBytes),
                    color = speedColor,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "/s",
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(bottom = 4.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

fun formatSpeed(bytes: Long): String {
    return when {
        bytes >= 1_048_576 -> String.format(Locale.getDefault(), "%.1f MB", bytes / 1_048_576.0)
        bytes >= 1_024 -> String.format(Locale.getDefault(), "%.0f KB", bytes / 1_024.0)
        else -> "$bytes B"
    }
}

@Composable
fun UsageRing(mobileBytes: Long, wifiBytes: Long, dataLimitBytes: Long, is5G: Boolean) {
    var animatedMobileTarget by remember { mutableStateOf(0f) }
    var animatedWifiTarget by remember { mutableStateOf(0f) }

    LaunchedEffect(mobileBytes, wifiBytes, dataLimitBytes) {
        animatedMobileTarget = if (dataLimitBytes == 0L) 0f else mobileBytes.toFloat() / dataLimitBytes.toFloat()
        animatedWifiTarget = if (dataLimitBytes == 0L) 0f else wifiBytes.toFloat() / dataLimitBytes.toFloat()
    }

    val mobileFraction by animateFloatAsState(
        targetValue = animatedMobileTarget.coerceAtMost(1f),
        animationSpec = tween(durationMillis = 1000),
        label = "mobileFraction"
    )

    val wifiFraction by animateFloatAsState(
        targetValue = animatedWifiTarget.coerceAtMost(1f - mobileFraction),
        animationSpec = tween(durationMillis = 1000, delayMillis = 400),
        label = "wifiFraction"
    )

    val surfaceColor = MaterialTheme.colorScheme.surface
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.size(280.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 16.dp.toPx()
            
            // Background track
            drawArc(
                color = surfaceColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            
            // Glow Effect Cellular
            if (mobileFraction > 0f) {
                drawArc(
                    color = tertiaryColor,
                    startAngle = 135f,
                    sweepAngle = mobileFraction * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth * 2f, cap = StrokeCap.Round)
                )
            }
            
            // Glow Effect Wi-Fi
            if (wifiFraction > 0f) {
                drawArc(
                    color = primaryColor,
                    startAngle = 135f + (mobileFraction * 360f),
                    sweepAngle = wifiFraction * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth * 2f, cap = StrokeCap.Round)
                )
            }

            // Cellular Usage
            if (mobileFraction > 0f) {
                drawArc(
                    color = tertiaryColor,
                    startAngle = 135f,
                    sweepAngle = mobileFraction * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
            
            // Wi-Fi Usage
            if (wifiFraction > 0f) {
                drawArc(
                    color = primaryColor,
                    startAngle = 135f + (mobileFraction * 360f),
                    sweepAngle = wifiFraction * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (is5G) "Unlimited 5G" else formatBytes(mobileBytes + wifiBytes),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (is5G) "Active" else "Total Tracked",
                color = MaterialTheme.colorScheme.primary,
              )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cellular",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Wi-Fi",
                    color = MaterialTheme.colorScheme.onSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun DailyDataLimitTracker(
    todayUsageBytes: Long,
    dailyLimitMBStr: String,
    onSaveLimit: (String) -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val limitMB = dailyLimitMBStr.toFloatOrNull() ?: 1000f
    val limitBytes = (limitMB * 1024 * 1024).toLong()
    val progress = if (limitBytes > 0) (todayUsageBytes.toFloat() / limitBytes.toFloat()).coerceIn(0f, 1f) else 0f
    val percentString = String.format(Locale.getDefault(), "%.1f", progress * 100)
    val isExceeded = todayUsageBytes > limitBytes

    var showDialog by remember { mutableStateOf(false) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "DAILY DATA LIMIT TRACKER",
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Used Today vs. Daily Soft Limit",
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))

                // Pure Vercel button with 1dp border and flat text
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                        .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                        .clickable { 
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            showDialog = true 
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "CONFIGURE",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                if (todayUsageBytes == 0L) {
                    // Shimmer placeholder for loading state
                    Box(
                        modifier = Modifier
                            .height(30.dp)
                            .width(140.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatBytes(todayUsageBytes),
                            color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "of ${if (limitMB >= 1024f) String.format(Locale.getDefault(), "%.1f GB", limitMB / 1024f) else String.format(Locale.getDefault(), "%.0f MB", limitMB)}",
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                if (todayUsageBytes == 0L) {
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .width(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                } else {
                    Text(
                        text = "$percentString%",
                        color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Premium minimalist progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(2.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(
                            color = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            if (isExceeded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⚠️ Daily data limit exceeded by ${formatBytes(todayUsageBytes - limitBytes)}!",
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    if (showDialog) {
        var enteredLimit by remember { mutableStateOf(if (limitMB >= 1024f && limitMB % 1024f == 0f) (limitMB / 1024f).toString() else limitMB.toString()) }
        var selectedUnit by remember { mutableStateOf(if (limitMB >= 1024f) "GB" else "MB") }
        var errorLocalMsg by remember { mutableStateOf<String?>(null) }

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showDialog = false }
        ) {
            // Sleek flat Vercel dialog
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Daily Data Limit Configuration".uppercase(Locale.getDefault()),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enter your desired daily maximum soft ceiling to pace billing usage.",
                        color = MaterialTheme.colorScheme.onSecondary,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (errorLocalMsg != null) {
                        Text(
                            text = errorLocalMsg!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    // Numeric entry
                    OutlinedTextField(
                        value = enteredLimit,
                        onValueChange = {
                            enteredLimit = it
                            errorLocalMsg = null
                        },
                        label = {
                            Text(
                                text = "Daily Limit Ceiling",
                                color = MaterialTheme.colorScheme.onSecondary,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Unit Picker Layout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("MB", "GB").forEach { unit ->
                            val isSelected = selectedUnit == unit
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                    .border(
                                        width = 0.5.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { 
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                        selectedUnit = unit 
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unit,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action buttons with Vercel styling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                                .clickable { 
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    showDialog = false 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CANCEL",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Save button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .clickable {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                                    val numericValue = enteredLimit.toFloatOrNull()
                                    if (numericValue == null || numericValue <= 0f) {
                                        errorLocalMsg = "Please enter a valid positive number"
                                        return@clickable
                                    }
                                    val finalLimitMB = if (selectedUnit == "GB") {
                                        (numericValue * 1024f).toInt().toString()
                                    } else {
                                        numericValue.toInt().toString()
                                    }
                                    onSaveLimit(finalLimitMB)
                                    showDialog = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "SAVE CEILING",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
