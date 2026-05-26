package com.siddharth.datamonitor.ui

import android.widget.Toast
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.siddharth.datamonitor.R
import com.siddharth.datamonitor.ui.theme.*
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.testTag

fun changeAppIcon(context: Context, iconChoice: String) {
    Toast.makeText(context, "Applying icon...", Toast.LENGTH_SHORT).show()
    
    val activity = context as? android.app.Activity
    activity?.finishAffinity()
    
    kotlinx.coroutines.GlobalScope.launch {
        delay(1000)

        val pm = context.packageManager
        val basePackage = "com.siddharth.datamonitor"
        
        val targets = mapOf(
            "DEFAULT" to "$basePackage.MainActivityIconDefault",
            "ICON_1" to "$basePackage.MainActivityIcon1",
            "ICON_2" to "$basePackage.MainActivityIcon2",
            "ICON_3" to "$basePackage.MainActivityIcon3",
            "ICON_4" to "$basePackage.MainActivityIcon4",
            "ICON_5" to "$basePackage.MainActivityIcon5"
        )
        
        val targetClass = targets[iconChoice] ?: "$basePackage.MainActivityIconDefault"
        
        // 1. Enable the requested target FIRST
        try {
            pm.setComponentEnabledSetting(
                ComponentName(context, targetClass),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Disable all OTHER targets
        targets.forEach { (key, aliasClass) ->
            if (aliasClass != targetClass) {
                try {
                    pm.setComponentEnabledSetting(
                        ComponentName(context, aliasClass),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        
        System.exit(0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager) {
    val dataLimitMB by themeManager.dataLimitFlow.collectAsStateWithLifecycle(initialValue = "2000")
    val alertsEnabled by themeManager.alertsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val trackSeparated by themeManager.trackSeparatedFlow.collectAsStateWithLifecycle(initialValue = true)
    val dataSaverActive by themeManager.dataSaverActiveFlow.collectAsStateWithLifecycle(initialValue = false)

    val currentTheme by themeManager.themeFlow.collectAsStateWithLifecycle(initialValue = AppTheme.FOREST)
    val currentLayout by themeManager.dashboardLayoutFlow.collectAsStateWithLifecycle(initialValue = DashboardLayoutPreference.STANDARD)
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var fontDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
            .navigationBarsPadding()
            .testTag("configs_parent_column")
    ) {
        Text(
            text = "CONFIG",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(48.dp))
        
        // Horizontal Scroll Theme Selection
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "APPEARANCE THEME",
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppTheme.entries.forEach { theme ->
                    val isSelected = currentTheme == theme
                    
                    // Explicit theme previews colors (Primary, Background, Surface companion)
                    val colors = when (theme) {
                        AppTheme.SPRING -> Triple(Color(0xFFFFAAB8), Color(0xFFF0FFDF), Color(0xFFFFD8DF))
                        AppTheme.DESERT -> Triple(Color(0xFFC7522A), Color(0xFFFBF2C4), Color(0xFFE5C185))
                        AppTheme.FOREST -> Triple(Color(0xFF8BAE66), Color(0xFF1B211A), Color(0xFF628141))
                        AppTheme.MIDNIGHT_AMOLED -> Triple(Color(0xFF00FFFF), Color(0xFF000000), Color(0xFF000000))
                        AppTheme.SOLARIZED_LIGHT -> Triple(Color(0xFF268BD2), Color(0xFFFDF6E3), Color(0xFFEEE8D5))
                        AppTheme.OCEAN_DEEP -> Triple(Color(0xFF00FFFF), Color(0xFF0D1B2A), Color(0xFF1B263B))
                        AppTheme.SUNSET_BLAZE -> Triple(Color(0xFFFFD700), Color(0xFF3A0A0A), Color(0xFF5A1818))
                        AppTheme.CYBERPUNK -> Triple(Color(0xFFFFFF00), Color(0xFF000000), Color(0xFF1A001A))
                        AppTheme.LAVENDER_HAZE -> Triple(Color(0xFF8B5CF6), Color(0xFFF3E8FF), Color(0xFFE9D5FF))
                        AppTheme.MATRIX -> Triple(Color(0xFF00FF00), Color(0xFF000000), Color(0xFF0A140A))
                    }
                    
                    Card(
                        onClick = {
                            scope.launch { themeManager.setTheme(theme) }
                        },
                        modifier = Modifier
                            .width(135.dp)
                            .height(115.dp)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.second)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = theme.name.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.siddharth.datamonitor.ui.theme.palettes[theme]?.text ?: Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Colors Dots
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(colors.first, RoundedCornerShape(6.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(colors.third, RoundedCornerShape(6.dp))
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // App Icon Selection
        val currentIcon by themeManager.appIconFlow.collectAsStateWithLifecycle(initialValue = "DEFAULT")
        var showIconConfirmDialog by remember { mutableStateOf<String?>(null) }
        
        if (showIconConfirmDialog != null) {
            AlertDialog(
                onDismissRequest = { showIconConfirmDialog = null },
                title = { Text("Change App Icon") },
                text = { Text("The app must close to apply the new home screen icon. Continue?") },
                confirmButton = {
                    TextButton(onClick = {
                        val iconToApply = showIconConfirmDialog!!
                        showIconConfirmDialog = null
                        scope.launch {
                            themeManager.setAppIcon(iconToApply)
                            changeAppIcon(context, iconToApply)
                        }
                    }) {
                        Text("Continue")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showIconConfirmDialog = null }) {
                        Text("Cancel")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                textContentColor = MaterialTheme.colorScheme.onSecondary
            )
        }
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "APP ICON",
                color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val icons = listOf(
                    Triple("DEFAULT", "Studio Default", R.drawable.ic_launcher_spring),
                    Triple("ICON_1", "Custom 1", R.drawable.ic_launcher_custom1),
                    Triple("ICON_2", "Custom 2", R.drawable.ic_launcher_custom2),
                    Triple("ICON_3", "Custom 3", R.drawable.ic_launcher_custom3),
                    Triple("ICON_4", "Custom 4", R.drawable.ic_launcher_custom4),
                    Triple("ICON_5", "Custom 5", R.drawable.ic_launcher_custom5)
                )

                icons.forEach { (key, label, drawRes) ->
                    val isSelected = currentIcon == key
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            onClick = {
                                showIconConfirmDialog = key
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(id = drawRes),
                                    contentDescription = label,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))


        
        // Live Network Ping & Latency Tester
        var latencyValue by remember { mutableStateOf(-1L) }
        var isPinging by remember { mutableStateOf(false) }
        
        LaunchedEffect(isPinging) {
            if (isPinging) {
                while (isPinging) {
                    latencyValue = measureLatency()
                    kotlinx.coroutines.delay(1500)
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "LIVE NETWORK HEALTH TESTER",
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gauge Canvas
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        val indicatorColor = if (latencyValue in 0..100) Color.Green else if (latencyValue in 101..300) Color.Yellow else if (latencyValue > 300) Color.Red else Color.Gray.copy(alpha = 0.5f)
                        val sweepAngle = if (latencyValue >= 0) ((latencyValue.toFloat() / 500f).coerceAtMost(1f) * 360f) else 0f
                        
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track
                            drawArc(
                                color = Color.Gray.copy(alpha = 0.2f),
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                            )
                            if (sweepAngle > 0f) {
                                drawArc(
                                    color = indicatorColor,
                                    startAngle = -90f,
                                    sweepAngle = sweepAngle,
                                    useCenter = false,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (latencyValue >= 0) "$latencyValue" else if (isPinging) "..." else "Ping",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                                color = indicatorColor
                            )
                            if (latencyValue >= 0) {
                                Text("ms", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GOOGLE DNS PING",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (latencyValue in 0..100) "Stable Network Signal" else if (latencyValue in 101..300) "Transient Buffer Bloat" else if (latencyValue > 300) "Highly unstable" else "Ready to track lag metrics",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.8f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = { isPinging = !isPinging },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPinging) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            contentColor = if (isPinging) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(if (isPinging) "STOP" else "TEST PING", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dashboard layout switcher standard vs pro vs grid
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "DASHBOARD LAYOUT",
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        DashboardLayoutPreference.STANDARD to "Standard",
                        DashboardLayoutPreference.PRO to "Pro Wave",
                        DashboardLayoutPreference.GRID to "Grid Block"
                    ).forEach { (layout, label) ->
                        val isSelected = currentLayout == layout
                        Button(
                            onClick = { scope.launch { themeManager.setDashboardLayout(layout) } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                        ) {
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))



        // Data Limit
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "SET DAILY DATA LIMIT (MB)",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = dataLimitMB,
                    onValueChange = { scope.launch { themeManager.setDataLimit(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggles
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingToggle(
                    title = "Data Saver Mode",
                    subtitle = "Reduce background syncs to conserve battery/data",
                    checked = dataSaverActive,
                    onCheckedChange = { scope.launch { themeManager.setDataSaverActive(it) } }
                )
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 12.dp))

                SettingToggle(
                    title = "Trigger Push Warning Alerts",
                    subtitle = "Get notified near limit",
                    checked = alertsEnabled,
                    onCheckedChange = { scope.launch { themeManager.setAlertsEnabled(it) } }
                )
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f), modifier = Modifier.padding(vertical = 12.dp))
                
                SettingToggle(
                    title = "Isolate Wi-Fi vs. Cellular Tracks",
                    subtitle = "Separate network analytics",
                    checked = trackSeparated,
                    onCheckedChange = { scope.launch { themeManager.setTrackSeparated(it) } }
                )
            }
        }
    }
}

@Composable
fun SettingToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                fontFamily = MaterialTheme.typography.titleMedium.fontFamily
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 12.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
                uncheckedTrackColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

suspend fun measureLatency(host: String = "8.8.8.8", port: Int = 53, timeoutMs: Int = 1500): Long {
    return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val socket = java.net.Socket()
            socket.connect(java.net.InetSocketAddress(host, port), timeoutMs)
            socket.close()
            System.currentTimeMillis() - startTime
        } catch (e: Exception) {
            -1L
        }
    }
}
