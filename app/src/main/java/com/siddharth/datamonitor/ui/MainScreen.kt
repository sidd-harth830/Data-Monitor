package com.siddharth.datamonitor.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siddharth.datamonitor.ui.theme.*
import com.siddharth.datamonitor.utils.PermissionsUtils
import java.text.DecimalFormat

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

import com.siddharth.datamonitor.R
import com.siddharth.datamonitor.ui.theme.ThemeManager

@Composable
fun MainScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val appAccentHex by themeManager.appAccentFlow.collectAsStateWithLifecycle(initialValue = "#19B1DC")
    val context = LocalContext.current

    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermission()
    }
    
    LaunchedEffect(Unit) {
        viewModel.checkPermission()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (hasPermission) {
                GlassTopAppBar()
            }
        },
        bottomBar = {
            if (hasPermission) {
                FloatingBottomNav(
                    currentRoute = currentRoute,
                    appAccentHex = appAccentHex,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            
            if (!hasPermission) {
                PermissionRequestScreen(
                    onRequest = {
                        permissionLauncher.launch(PermissionsUtils.getUsageStatsIntent())
                    }
                )
            } else {
                NavHost(
                    navController = navController,
                    startDestination = "home",
                    enterTransition = { fadeIn(tween(400)) },
                    exitTransition = { fadeOut(tween(400)) }
                ) {
                    composable("home") { DashboardScreen(viewModel, themeManager) }
                    composable("history") { HistoryScreen(viewModel) }
                    composable("profile") { ProfileScreen(viewModel) }
                    composable("settings") { SettingsScreen(viewModel, themeManager) }
                }
            }
        }
    }
}

@Composable
fun FloatingBottomNav(currentRoute: String, appAccentHex: String, onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        GlassCard(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth().height(72.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val accentColor = Color(android.graphics.Color.parseColor(appAccentHex))
                NavItem(
                    label = "Home",
                    iconRes = R.drawable.ic_launcher_default,
                    accentColor = accentColor,
                    isSelected = currentRoute == "home",
                    onClick = { onNavigate("home") }
                )
                NavItem(
                    label = "Analytics",
                    iconRes = R.drawable.ic_launcher_complex,
                    accentColor = accentColor,
                    isSelected = currentRoute == "history",
                    onClick = { onNavigate("history") }
                )
                NavItem(
                    label = "Profile",
                    iconRes = R.drawable.ic_launcher_minimal,
                    accentColor = accentColor,
                    isSelected = currentRoute == "profile",
                    onClick = { onNavigate("profile") }
                )
                NavItem(
                    label = "Config",
                    iconRes = R.drawable.ic_launcher_default,
                    accentColor = accentColor,
                    isSelected = currentRoute == "settings",
                    onClick = { onNavigate("settings") }
                )
            }
        }
    }
}

@Composable
fun NavItem(label: String, iconRes: Int, accentColor: Color, isSelected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    Button(
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) 
            onClick() 
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) accentColor.copy(alpha = 0.15f) else Color.Transparent,
            contentColor = if (isSelected) accentColor else MaterialTheme.colorScheme.onSecondary
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(16.dp),
                tint = if (isSelected) accentColor else MaterialTheme.colorScheme.onSecondary
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = accentColor
                )
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequest: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ACCESS REQUIRED",
            style = MaterialTheme.typography.titleLarge,
            color = PrimaryNeon,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Enable usage access to monitor real-time network traffic and provide insights.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        GlassCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = PrimaryNeon),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("AUTHENTICATE", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
        }
    }
}

fun formatBytes(bytes: Long): String {
    val formatter = DecimalFormat("#,##0.0")
    return when {
        bytes >= 1_073_741_824 -> "${formatter.format(bytes / 1_073_741_824.0)} GB"
        bytes >= 1_048_576 -> "${formatter.format(bytes / 1_048_576.0)} MB"
        bytes >= 1_024 -> "${formatter.format(bytes / 1_024.0)} KB"
        else -> "$bytes B"
    }
}

fun bytesToMB(bytes: Long): Double {
    return bytes / 1_048_576.0
}

@Composable
fun GlassTopAppBar() {
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f
    val tintColor = if (isLight) Color.White.copy(alpha = 0.55f) else Color.Black.copy(alpha = 0.45f)
    val borderColor = if (isLight) Color.Black.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.18f)
    val textColor = if (isLight) Color.Black else Color.White

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 24.dp, end = 24.dp, bottom = 4.dp)
            .height(56.dp)
            .graphicsLayer { alpha = 0.9f }
    ) {
        GlassCard(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "📶 DATA MONITOR SYSTEM",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = textColor
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.Green, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
