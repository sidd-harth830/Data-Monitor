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
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextAlign
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

@Composable
fun <T> PremiumTabSelector(
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f
    val customDividerColor = MaterialTheme.colorScheme.onBackground
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { (option, label) ->
            val isSelected = option == selectedOption
            
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            }
            
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onBackground
            }
            
            val borderStroke = androidx.compose.foundation.BorderStroke(
                width = 0.5.dp,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    customDividerColor
                }
            )

            Surface(
                onClick = { 
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onOptionSelected(option)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                color = containerColor,
                border = borderStroke
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager, onNavigateToLimits: () -> Unit) {
    val dataLimitMB by themeManager.dataLimitFlow.collectAsStateWithLifecycle(initialValue = "2000")
    val dailyDataLimitMB by themeManager.dailyDataLimitFlow.collectAsStateWithLifecycle(initialValue = "1000")
    val alertsEnabled by themeManager.alertsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val trackSeparated by themeManager.trackSeparatedFlow.collectAsStateWithLifecycle(initialValue = true)
    val dataSaverActive by themeManager.dataSaverActiveFlow.collectAsStateWithLifecycle(initialValue = false)

    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val uiStyle by themeManager.uiStyleFlow.collectAsStateWithLifecycle(initialValue = UiStyle.DEFAULT_MONOGRAM)
    val monogramTheme by themeManager.monogramThemeFlow.collectAsStateWithLifecycle(initialValue = MonogramTheme.SYSTEM_DEFAULT)
    val materialPalette by themeManager.materialPaletteFlow.collectAsStateWithLifecycle(initialValue = MaterialColorPalette.DYNAMIC)
    val materialDarkMode by themeManager.materialDarkModeFlow.collectAsStateWithLifecycle(initialValue = MaterialDarkMode.SYSTEM)
    val currentLayout by themeManager.dashboardLayoutFlow.collectAsStateWithLifecycle(initialValue = DashboardLayoutPreference.STANDARD)
    val fontProfile by themeManager.fontProfileFlow.collectAsStateWithLifecycle(initialValue = com.siddharth.datamonitor.ui.theme.FontProfile.OSWALD)
    
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f
    val customDividerColor = MaterialTheme.colorScheme.onBackground
    val customIndicatorColor = MaterialTheme.colorScheme.onBackground
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var fontDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
            .navigationBarsPadding()
            .testTag("configs_parent_column")
    ) {
        Text(
            text = "CONFIG",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        // Global UI Style Selector
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "GLOBAL UI STYLE",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTabSelector(
                    options = listOf(
                        UiStyle.DEFAULT_MONOGRAM to "Default UI",
                        UiStyle.MATERIAL_3 to "Material UI"
                    ),
                    selectedOption = uiStyle,
                    onOptionSelected = { style ->
                        scope.launch { themeManager.setUiStyle(style) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        if (uiStyle == UiStyle.DEFAULT_MONOGRAM) {
            // Default UI Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "THEME SELECTOR",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumTabSelector(
                        options = listOf(
                            MonogramTheme.SYSTEM_DEFAULT to "System",
                            MonogramTheme.LIGHT_MONOGRAM to "Light",
                            MonogramTheme.DARK_MONOGRAM to "Dark"
                        ),
                        selectedOption = monogramTheme,
                        onOptionSelected = { theme ->
                            scope.launch { themeManager.setMonogramTheme(theme) }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "FONT SELECTOR",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "System fonts are enforced for the Default UI.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        } else {
            // Material UI Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "DARK MODE",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumTabSelector(
                        options = listOf(
                            MaterialDarkMode.SYSTEM to "System",
                            MaterialDarkMode.LIGHT to "Light",
                            MaterialDarkMode.DARK to "Dark"
                        ),
                        selectedOption = materialDarkMode,
                        onOptionSelected = { mode ->
                            scope.launch { themeManager.setMaterialDarkMode(mode) }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "MATERIAL COLOR THEME",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumTabSelector(
                        options = listOf(
                            MaterialColorPalette.DYNAMIC to "Dynamic",
                            MaterialColorPalette.OCEAN_BLUE to "Blue",
                            MaterialColorPalette.FOREST_GREEN to "Green",
                            MaterialColorPalette.AMETHYST_PURPLE to "Purple",
                            MaterialColorPalette.SUNSET_ORANGE to "Orange"
                        ),
                        selectedOption = materialPalette,
                        onOptionSelected = { palette ->
                            scope.launch { themeManager.setMaterialPalette(palette) }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "FONT SELECTOR",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PremiumTabSelector(
                        options = listOf(
                            com.siddharth.datamonitor.ui.theme.FontProfile.SYSTEM_DEFAULT to "Sys",
                            com.siddharth.datamonitor.ui.theme.FontProfile.OSWALD to "Oswald",
                            com.siddharth.datamonitor.ui.theme.FontProfile.BRICOLAGE to "Bricolage",
                            com.siddharth.datamonitor.ui.theme.FontProfile.AKT to "Akt"
                        ),
                        selectedOption = fontProfile,
                        onOptionSelected = { profile ->
                            scope.launch { themeManager.setFontProfile(profile) }
                        }
                    )
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
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "APP ICON SELECTOR",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
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
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else customDividerColor
                                ),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = drawRes),
                                        contentDescription = label,
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                        }
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
                    val result = measureLatency()
                    latencyValue = result
                    if (result >= 0) {
                        themeManager.recordPingResult(result)
                    }
                    kotlinx.coroutines.delay(1500)
                }
            }
        }

        GlassCard(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "LIVE NETWORK HEALTH TESTER",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gauge Canvas
                    Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                        val colorScheme = MaterialTheme.colorScheme
                        val indicatorColor = if (latencyValue in 0..100) colorScheme.primary else if (latencyValue in 101..300) colorScheme.tertiary else if (latencyValue > 300) colorScheme.error else colorScheme.onSurfaceVariant
                        val trackColor = colorScheme.surfaceVariant
                        val sweepAngle = if (latencyValue >= 0) ((latencyValue.toFloat() / 500f).coerceAtMost(1f) * 360f) else 0f
                        
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            // Track
                            drawArc(
                                color = trackColor,
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
                                style = MaterialTheme.typography.titleMedium,
                                color = indicatorColor
                            )
                            if (latencyValue >= 0) {
                                Text(
                                    text = "ms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GOOGLE DNS PING",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (latencyValue in 0..100) "Stable Network Signal" else if (latencyValue in 101..300) "Transient Buffer Bloat" else if (latencyValue > 300) "Highly unstable" else "Ready to track lag metrics",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondary
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
                        Text(
                            text = if (isPinging) "STOP" else "TEST PING",
                            style = MaterialTheme.typography.labelLarge
                        )
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTabSelector(
                    options = listOf(
                        DashboardLayoutPreference.STANDARD to "Standard",
                        DashboardLayoutPreference.PRO to "Pro Wave",
                        DashboardLayoutPreference.GRID to "Grid Block"
                    ),
                    selectedOption = currentLayout,
                    onOptionSelected = { layout ->
                        scope.launch { themeManager.setDashboardLayout(layout) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))





        // Advanced Limits Button
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToLimits() }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Advanced Usage Limits",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set custom limits and notifications",
                            color = MaterialTheme.colorScheme.onSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Navigate to limits",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
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
                
                HorizontalDivider(color = customDividerColor, modifier = Modifier.padding(vertical = 12.dp))

                SettingToggle(
                    title = "Trigger Push Warning Alerts",
                    subtitle = "Get notified near limit",
                    checked = alertsEnabled,
                    onCheckedChange = { scope.launch { themeManager.setAlertsEnabled(it) } }
                )
                
                HorizontalDivider(color = customDividerColor, modifier = Modifier.padding(vertical = 12.dp))
                
                SettingToggle(
                    title = "Isolate Wi-Fi vs. Cellular Tracks",
                    subtitle = "Separate network analytics",
                    checked = trackSeparated,
                    onCheckedChange = { scope.launch { themeManager.setTrackSeparated(it) } }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(
            onClick = { throw RuntimeException("Test Crash") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text(
                text = "Force Crash (Crashlytics Diagnostic Test)",
            )
        }
    }
}

@Composable
fun SettingToggle(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { 
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onCheckedChange(!checked)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                onCheckedChange(it)
            },
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
