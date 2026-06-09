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
        
        SettingItemToggle(
            title = "Pure Black Theme",
            subtitle = "Enable pure OLED black background to save battery",
            checked = pureBlack,
            onCheckedChange = { 
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                scope.launch { themeManager.setPureBlack(it) }
            }
        )
        
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

        SettingsGroupHeader("SYSTEM")
        
        SettingItemToggle(
            title = "Hide App Icon",
            subtitle = "Remove app from launcher (Dial #*1234# to open)",
            checked = isIconHidden(context),
            onCheckedChange = { hide ->
                toggleAppIcon(context, hide)
                Toast.makeText(context, if (hide) "Icon Hidden" else "Icon Restored", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showLimitsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLimitsSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xFF1E1E1E), // Darker than normal surface for contrast
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
            .background(Color(0x0DFFFFFF))
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
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
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
            .background(Color(0x0DFFFFFF))
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

fun isIconHidden(context: Context): Boolean {
    val pm = context.packageManager
    val componentName = ComponentName(context, "com.siddharth.datamonitor.MainActivityLauncher")
    val state = pm.getComponentEnabledSetting(componentName)
    return state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
}

fun toggleAppIcon(context: Context, hide: Boolean) {
    val pm = context.packageManager
    val componentName = ComponentName(context, "com.siddharth.datamonitor.MainActivityLauncher")
    val state = if (hide) PackageManager.COMPONENT_ENABLED_STATE_DISABLED else PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    pm.setComponentEnabledSetting(componentName, state, PackageManager.DONT_KILL_APP)
}
