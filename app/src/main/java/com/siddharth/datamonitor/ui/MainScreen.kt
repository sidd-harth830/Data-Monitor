package com.siddharth.datamonitor.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSaveLayer
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.foundation.Canvas

import com.siddharth.datamonitor.R
import com.siddharth.datamonitor.ui.theme.ThemeManager

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.rounded.NetworkCheck

import androidx.compose.material.icons.rounded.Warning
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material.icons.rounded.CloudDownload

@Composable
fun MainScreen(
    viewModel: DataUsageViewModel,
    themeManager: ThemeManager,
    gitHubUrl: String = "https://github.com/sid-yadav7307/Data-Monitor-Releases/releases/latest"
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "home"
    
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val skipAuth by themeManager.skipLoginFlow.collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var currentAuthUser by remember { mutableStateOf(auth.currentUser) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val showBars = hasPermission && currentRoute != "admin_dashboard" && currentRoute != "login"
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.checkPermission()
    }
    
    LaunchedEffect(Unit) {
        viewModel.checkPermission()
    }

    LaunchedEffect(auth) {
        auth.addAuthStateListener { firebaseAuth ->
            currentAuthUser = firebaseAuth.currentUser
        }
    }

    val startDestination = remember(skipAuth, currentAuthUser) {
        if (currentAuthUser == null && !skipAuth) "login" else "home"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (showBars) {
                GlassTopAppBar(
                    modifier = Modifier.zIndex(10f)
                )
            }
        },
        bottomBar = {
            if (showBars) {
                val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
                FloatingBottomNav(
                    currentRoute = currentRoute,
                    isAdmin = isAdmin,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.zIndex(10f)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!hasPermission) {
                PermissionRequestScreen(
                    onRequest = {
                        permissionLauncher.launch(PermissionsUtils.getUsageStatsIntent())
                    }
                )
            } else {
                key(startDestination) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.fillMaxSize(),
                        enterTransition = { fadeIn(tween(400)) },
                        exitTransition = { fadeOut(tween(400)) }
                    ) {
                        composable("login") {
                            LoginScreen(
                                themeManager = themeManager,
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onSkip = {
                                    scope.launch {
                                        themeManager.setSkipLogin(true)
                                        navController.navigate("home") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    }
                                }
                            )
                        }
                        composable("home") { DashboardScreen(viewModel, themeManager) }
                        composable("history") { HistoryScreen(viewModel, themeManager) }
                        composable("profile") { 
                            ProfileScreen(
                                viewModel = viewModel,
                                themeManager = themeManager,
                                onNavigateToAuth = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("settings") { 
                            SettingsScreen(
                                viewModel = viewModel,
                                themeManager = themeManager,
                                onNavigateToLimits = {
                                    navController.navigate("usage_limits")
                                }
                            )
                        }
                        composable("admin_dashboard") {
                            AdminDashboardScreen(onBack = { navController.popBackStack() })
                        }
                        composable("usage_limits") {
                            UsageLimitsScreen(onNavigateBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }

    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    var isDismissedByUser by remember { mutableStateOf(false) }

    updateState?.let { updateInfo ->
        if (!isDismissedByUser || updateInfo.isMandatory) {
            Dialog(
                onDismissRequest = {
                    if (!updateInfo.isMandatory) {
                        isDismissedByUser = true
                    }
                },
                properties = DialogProperties(
                    dismissOnBackPress = !updateInfo.isMandatory,
                    dismissOnClickOutside = !updateInfo.isMandatory
                )
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.Black) // Extra dark overlay to ensure premium contrast
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CloudDownload,
                            contentDescription = "System Update Available",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "SYSTEM UPDATE AVAILABLE",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = "v${updateInfo.versionName}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "RELEASE NOTES",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            val scrollState = rememberScrollState()
                            Text(
                                text = updateInfo.releaseNotes.ifEmpty { "Performance optimizations and system stability enhancements." },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.verticalScroll(scrollState)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                val targetUrl = if (gitHubUrl.isNotBlank()) gitHubUrl else (if (updateInfo.downloadUrl.isNotBlank()) updateInfo.downloadUrl else "https://github.com/sid-yadav7307/Data-Monitor-Releases/releases/latest")
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                "UPDATE NOW",
                            )
                        }
                        
                        if (!updateInfo.isMandatory) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { isDismissedByUser = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text(
                                    "REMIND ME LATER",
                                    color = MaterialTheme.colorScheme.onBackground,
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "This update is required to maintain core gateway synchronization features.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingBottomNav(
    currentRoute: String,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val items = remember(isAdmin) {
        val base = mutableListOf(
            Triple("home", "Home", Icons.Filled.Home),
            Triple("history", "Analytics", Icons.Filled.List),
            Triple("profile", "Profile", Icons.Filled.Person),
            Triple("settings", "Config", Icons.Filled.Settings)
        )
        if (isAdmin) {
            base.add(Triple("admin_dashboard", "Admin Core", Icons.Filled.VerifiedUser))
        }
        base
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxWidth()
    ) {
        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                    onNavigate(route)
                },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}


@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun GlassTopAppBar(modifier: Modifier = Modifier) {
    androidx.compose.material3.TopAppBar(
        title = { Text("Data Monitor") },
        modifier = modifier
    )
}

@Composable
fun PermissionRequestScreen(onRequest: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Usage Access Required", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRequest) {
                Text("Grant Permission")
            }
        }
    }
}
