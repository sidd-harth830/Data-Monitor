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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
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

@Composable
fun <T> PremiumTabSelector(
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f
    val customDividerColor = if (isLight) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.15f)
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        options.forEach { (option, label) ->
            val isSelected = option == selectedOption
            
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
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
                onClick = { onOptionSelected(option) },
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
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = BricolageFontFamily
                        ),
                        color = contentColor,
                        maxLines = 1,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager) {
    val dataLimitMB by themeManager.dataLimitFlow.collectAsStateWithLifecycle(initialValue = "2000")
    val dailyDataLimitMB by themeManager.dailyDataLimitFlow.collectAsStateWithLifecycle(initialValue = "1000")
    val alertsEnabled by themeManager.alertsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val trackSeparated by themeManager.trackSeparatedFlow.collectAsStateWithLifecycle(initialValue = true)
    val dataSaverActive by themeManager.dataSaverActiveFlow.collectAsStateWithLifecycle(initialValue = false)

    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val monogramTheme by themeManager.monogramThemeFlow.collectAsStateWithLifecycle(initialValue = MonogramTheme.SYSTEM_DEFAULT)
    val currentLayout by themeManager.dashboardLayoutFlow.collectAsStateWithLifecycle(initialValue = DashboardLayoutPreference.STANDARD)
    val fontProfile by themeManager.fontProfileFlow.collectAsStateWithLifecycle(initialValue = com.siddharth.datamonitor.ui.theme.FontProfile.DEFAULT)
    
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f
    val customDividerColor = if (isLight) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.15f)
    val customIndicatorColor = if (isLight) Color.Black.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.15f)
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var fontDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 110.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
            .navigationBarsPadding()
            .testTag("configs_parent_column")
    ) {
        Text(
            text = "CONFIG",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = BricolageFontFamily
            ),
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
        
        // Premium Theme Selector: System Default, Light Monogram, Dark Monogram
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "THEME SELECTOR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BricolageFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val themeOptions = listOf(
                        MonogramTheme.SYSTEM_DEFAULT to "SYSTEM",
                        MonogramTheme.LIGHT_MONOGRAM to "LIGHT",
                        MonogramTheme.DARK_MONOGRAM to "DARK"
                    )

                    themeOptions.forEach { (option, label) ->
                        val isSelected = option == monogramTheme
                        val containerColor = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        }

                        val contentColor = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }

                        val isSystemLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f
                        val customDividerColor = if (isSystemLight) Color.Black.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.15f)

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
                                scope.launch { themeManager.setMonogramTheme(option) }
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
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = BricolageFontFamily,
                                        fontSize = 11.sp,
                                        letterSpacing = 1.sp
                                    ),
                                    color = contentColor,
                                    maxLines = 1
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
        
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "APP ICON SELECTOR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BricolageFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
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
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = BricolageFontFamily
                                ),
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BricolageFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
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
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = BricolageFontFamily
                                ),
                                color = indicatorColor
                            )
                            if (latencyValue >= 0) {
                                Text(
                                    text = "ms",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 9.sp,
                                        fontFamily = BricolageFontFamily
                                    ),
                                    color = MaterialTheme.colorScheme.onSecondary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "GOOGLE DNS PING",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BricolageFontFamily
                            ),
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (latencyValue in 0..100) "Stable Network Signal" else if (latencyValue in 101..300) "Transient Buffer Bloat" else if (latencyValue > 300) "Highly unstable" else "Ready to track lag metrics",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontFamily = BricolageFontFamily
                            ),
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
                        Text(
                            text = if (isPinging) "STOP" else "TEST PING",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = BricolageFontFamily
                            )
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
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BricolageFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
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

        // Font Selector Section
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "FONT SELECTOR",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BricolageFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                PremiumTabSelector(
                    options = listOf(
                        com.siddharth.datamonitor.ui.theme.FontProfile.DEFAULT to "System Default",
                        com.siddharth.datamonitor.ui.theme.FontProfile.PREMIUM to "Plus Jakarta Sans"
                    ),
                    selectedOption = fontProfile,
                    onOptionSelected = { profile ->
                        scope.launch { themeManager.setFontProfile(profile) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Data Limits Configuration
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "DATA CEILING CONFIGURATION",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = BricolageFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "DAILY CEILING LIMIT (MB)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = BricolageFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = dailyDataLimitMB,
                    onValueChange = { scope.launch { themeManager.setDailyDataLimit(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.primary,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = customIndicatorColor,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BricolageFontFamily
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "MONTHLY BILLING CYCLE LIMIT (MB)",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = BricolageFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                        unfocusedIndicatorColor = customIndicatorColor,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = BricolageFontFamily
                    ),
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
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
        ) {
            Text(
                text = "Force Crash (Crashlytics Diagnostic Test)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.5.sp
            )
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
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    fontFamily = BricolageFontFamily
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSecondary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontFamily = BricolageFontFamily
                )
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
