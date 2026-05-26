package com.example.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import kotlinx.coroutines.launch

fun changeAppIcon(context: Context, iconChoice: String) {
    val pm = context.packageManager
    val basePackage = "com.example" // match the main namespace package path
    
    val targets = mapOf(
        "DEFAULT" to "$basePackage.MainActivityDefault",
        "DARK" to "$basePackage.MainActivityDark",
        "NEON" to "$basePackage.MainActivityNeon",
        "MINIMAL" to "$basePackage.MainActivityMinimal"
    )
    
    targets.forEach { (key, aliasClass) ->
        val enableState = if (key == iconChoice) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }
        
        try {
            pm.setComponentEnabledSetting(
                ComponentName(context, aliasClass),
                enableState,
                PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager) {
    val dataLimitMB by themeManager.dataLimitFlow.collectAsStateWithLifecycle(initialValue = "2000")
    val alertsEnabled by themeManager.alertsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)
    val trackSeparated by themeManager.trackSeparatedFlow.collectAsStateWithLifecycle(initialValue = true)
    val billingCycleDay by themeManager.billingCycleDayFlow.collectAsStateWithLifecycle(initialValue = 1)

    val currentTheme by themeManager.themeFlow.collectAsStateWithLifecycle(initialValue = AppTheme.OLED_DARK)
    val currentFont by themeManager.fontFlow.collectAsStateWithLifecycle(initialValue = AppFont.ACORN)
    val currentLayout by themeManager.dashboardLayoutFlow.collectAsStateWithLifecycle(initialValue = DashboardLayoutPreference.STANDARD)
    val currentIcon by themeManager.appIconFlow.collectAsStateWithLifecycle(initialValue = "DEFAULT")
    
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var fontDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
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
                        AppTheme.OLED_DARK -> Triple(PrimaryNeon, DarkBackground, Color(0xFF1E1E24))
                        AppTheme.CYBER_NEON -> Triple(CyberpunkPrimary, CyberpunkBackground, Color(0xFF1F1230))
                        AppTheme.MINIMAL_LIGHT -> Triple(LightPrimary, LightBackground, Color(0xFFE2E8F0))
                        AppTheme.PREMIUM_GLASS -> Triple(Color(0xFFA855F7), Color(0xFF0D0B18), Color(0xFF161426))
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
                                color = if (theme == AppTheme.MINIMAL_LIGHT) LightTextPrimary else TextPrimary
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
        
        // Font Selection
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "TYPOGRAPHY",
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = fontDropdownExpanded,
                    onExpandedChange = { fontDropdownExpanded = !fontDropdownExpanded }
                ) {
                    TextField(
                        readOnly = true,
                        value = currentFont.name.replace("_", " "),
                        onValueChange = { },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = fontDropdownExpanded,
                        onDismissRequest = { fontDropdownExpanded = false }
                    ) {
                        AppFont.entries.forEach { font ->
                            DropdownMenuItem(
                                text = { Text(font.name.replace("_", " ")) },
                                onClick = {
                                    scope.launch { themeManager.setFont(font) }
                                    fontDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dashboard layout switcher standard vs pro
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(
                        DashboardLayoutPreference.STANDARD to "Standard (Ring)",
                        DashboardLayoutPreference.PRO to "Pro Analytics"
                    ).forEach { (layout, label) ->
                        val isSelected = currentLayout == layout
                        Button(
                            onClick = { scope.launch { themeManager.setDashboardLayout(layout) } },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Launcher App Icon switch grid
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "COSMETIC APP LAUNCHER ICON",
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
                    val iconOptions = listOf(
                        "DEFAULT" to "Default",
                        "DARK" to "Dark",
                        "NEON" to "Neon",
                        "MINIMAL" to "Minimal"
                    )
                    
                    iconOptions.forEach { (iconKey, label) ->
                        val isSelected = currentIcon == iconKey
                        Card(
                            onClick = {
                                scope.launch {
                                    themeManager.setAppIcon(iconKey)
                                    changeAppIcon(context, iconKey)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(85.dp)
                                .border(
                                    width = if (isSelected) 1.8.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                val circleColor = when (iconKey) {
                                    "DEFAULT" -> PrimaryNeon
                                    "DARK" -> Color(0xFF1E1C1F)
                                    "NEON" -> CyberpunkPrimary
                                    "MINIMAL" -> LightPrimary
                                    else -> PrimaryNeon
                                }
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .background(circleColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = iconKey.take(1),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1
                                )
                            }
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

        // Billing Cycle Reset
        var billingCycleExpanded by remember { mutableStateOf(false) }
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "BILLING CYCLE START DATE",
                    color = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                ExposedDropdownMenuBox(
                    expanded = billingCycleExpanded,
                    onExpandedChange = { billingCycleExpanded = !billingCycleExpanded }
                ) {
                    TextField(
                        readOnly = true,
                        value = "Day $billingCycleDay of month",
                        onValueChange = { },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = billingCycleExpanded) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                        ),
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = billingCycleExpanded,
                        onDismissRequest = { billingCycleExpanded = false }
                    ) {
                        (1..31).forEach { day ->
                            DropdownMenuItem(
                                text = { Text("Day $day of month") },
                                onClick = {
                                    scope.launch { themeManager.setBillingCycleDay(day) }
                                    billingCycleExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggles
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
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
