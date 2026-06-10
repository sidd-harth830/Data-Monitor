package com.siddharth.datamonitor.ui

import android.widget.Toast
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.siddharth.datamonitor.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: DataUsageViewModel,
    themeManager: ThemeManager,
    onNavigateToLimits: () -> Unit
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pureBlack by themeManager.pureBlackFlow.collectAsStateWithLifecycle(initialValue = true)
    
    // Limits state for bottom sheet
    var showLimitsSheet by remember { mutableStateOf(false) }
    var tempMonthlyLimit by remember { mutableStateOf("") }
    var tempDailyLimit by remember { mutableStateOf("") }
    
    val currentMonthlyLimit by themeManager.dataLimitFlow.collectAsStateWithLifecycle(initialValue = "2000")
    val currentDailyLimit by themeManager.dailyDataLimitFlow.collectAsStateWithLifecycle(initialValue = "1000")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
    ) {
        Text(
            text = "SETTINGS",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsGroupHeader("APPEARANCE")
        
        val darkMode by themeManager.darkModeFlow.collectAsStateWithLifecycle(initialValue = true)
        
        SettingItemToggle(
            title = "Dark Mode",
            subtitle = "Enable dark theme for improved visibility during night time",
            checked = darkMode,
            onCheckedChange = { 
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                scope.launch { themeManager.setDarkMode(it) }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (darkMode) {
            SettingItemToggle(
                title = "Pure Black Theme",
                subtitle = "Enable pure OLED black background to save battery",
                checked = pureBlack,
                onCheckedChange = { 
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    scope.launch { themeManager.setPureBlack(it) }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        SettingsGroupHeader("DATA TRACKING")
        
        SettingItemClickable(
            title = "Usage Limits",
            subtitle = "Set monthly and daily billing cycle caps",
            onClick = {
                tempMonthlyLimit = currentMonthlyLimit
                tempDailyLimit = currentDailyLimit
                showLimitsSheet = true
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        SettingsGroupHeader("AI ASSISTANT")
        
        val showChatbot by themeManager.showChatbotFlow.collectAsStateWithLifecycle(initialValue = true)
        SettingItemToggle(
            title = "Show AI Assistant",
            subtitle = "Display chatbot floating action button for data insights",
            checked = showChatbot,
            onCheckedChange = { 
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                scope.launch { themeManager.setShowChatbot(it) }
            }
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        SettingsGroupHeader("SYSTEM")
        
        var showIconDialog by remember { mutableStateOf(false) }

        SettingItemClickable(
            title = "Change App Icon",
            subtitle = "Select a custom launcher icon",
            onClick = { showIconDialog = true }
        )

        val iconAliases = listOf(
            Triple("Spring Green", ".MainActivityIconDefault", com.siddharth.datamonitor.R.drawable.ic_launcher_spring),
            Triple("Neon Purple", ".MainActivityIcon1", com.siddharth.datamonitor.R.drawable.ic_launcher_custom1),
            Triple("Ocean Deep", ".MainActivityIcon2", com.siddharth.datamonitor.R.drawable.ic_launcher_custom2),
            Triple("Sunset Blaze", ".MainActivityIcon3", com.siddharth.datamonitor.R.drawable.ic_launcher_custom3),
            Triple("Lavender Haze", ".MainActivityIcon4", com.siddharth.datamonitor.R.drawable.ic_launcher_custom4),
            Triple("Midnight AMOLED", ".MainActivityIcon5", com.siddharth.datamonitor.R.drawable.ic_launcher_custom5)
        )

        if (showIconDialog) {
            AlertDialog(
                onDismissRequest = { showIconDialog = false },
                title = { Text("Select App Icon", color = MaterialTheme.colorScheme.primary) },
                text = {
                    Column {
                        iconAliases.forEach { (name, alias, drawableRes) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        setAppIcon(context, alias)
                                        Toast.makeText(context, "Icon changed to $name. It may take a moment to update.", Toast.LENGTH_LONG).show()
                                        showIconDialog = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = androidx.compose.ui.res.painterResource(id = drawableRes),
                                    contentDescription = name,
                                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showIconDialog = false }) {
                        Text("CLOSE")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }

    if (showLimitsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLimitsSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // Support dark and light theme
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DATA LIMITS",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = tempMonthlyLimit,
                    onValueChange = { tempMonthlyLimit = it },
                    label = { Text("Monthly Limit (MB)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = tempDailyLimit,
                    onValueChange = { tempDailyLimit = it },
                    label = { Text("Daily Limit (MB)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        scope.launch {
                            themeManager.setDataLimit(tempMonthlyLimit)
                            themeManager.setDailyDataLimit(tempDailyLimit)
                            showLimitsSheet = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SAVE LIMITS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SettingsGroupHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun SettingItemToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingItemClickable(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

fun setAppIcon(context: Context, activeAlias: String) {
    val pm = context.packageManager
    val aliases = listOf(
        ".MainActivityIconDefault",
        ".MainActivityIcon1",
        ".MainActivityIcon2",
        ".MainActivityIcon3",
        ".MainActivityIcon4",
        ".MainActivityIcon5"
    )
    aliases.forEach { alias ->
        val componentName = android.content.ComponentName(context.packageName, context.packageName + alias)
        val state = if (alias == activeAlias) android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED else android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        pm.setComponentEnabledSetting(componentName, state, android.content.pm.PackageManager.DONT_KILL_APP)
    }
}
