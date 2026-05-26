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

import com.siddharth.datamonitor.ui.theme.ThemeManager

@Composable
fun MainScreen(viewModel: DataUsageViewModel, themeManager: ThemeManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
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
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (hasPermission) {
                FloatingBottomNav(
                    currentRoute = currentRoute,
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
            .padding(bottom = paddingValues.calculateBottomPadding())) {
            
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
fun FloatingBottomNav(currentRoute: String, onNavigate: (String) -> Unit) {
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
                NavItem(
                    label = "Home",
                    isSelected = currentRoute == "home",
                    onClick = { onNavigate("home") }
                )
                NavItem(
                    label = "History",
                    isSelected = currentRoute == "history",
                    onClick = { onNavigate("history") }
                )
                NavItem(
                    label = "Profile",
                    isSelected = currentRoute == "profile",
                    onClick = { onNavigate("profile") }
                )
                NavItem(
                    label = "Config",
                    isSelected = currentRoute == "settings",
                    onClick = { onNavigate("settings") }
                )
            }
        }
    }
}

@Composable
fun NavItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    Button(
        onClick = { 
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) 
            onClick() 
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) GlassWhite else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
        ),
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
    ) {
        Text(
            text = label,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondary
        )
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
