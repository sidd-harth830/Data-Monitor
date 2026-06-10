package com.siddharth.datamonitor.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.siddharth.datamonitor.ui.theme.ThemeManager
import com.siddharth.datamonitor.utils.PermissionsUtils
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable

import androidx.compose.ui.res.painterResource
import com.siddharth.datamonitor.R

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
    val showChatbot by themeManager.showChatbotFlow.collectAsStateWithLifecycle(initialValue = true)
    
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    var currentAuthUser by remember { mutableStateOf(auth.currentUser) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val showBars = hasPermission && currentRoute != "admin_dashboard" && currentRoute != "login"
    
    var isChatbotOpen by remember { mutableStateOf(false) }
    
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

    // Hide on scroll state
    var isNavBarVisible by remember { mutableStateOf(true) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If scrolling down (positive y in terms of screen movement), hide
                if (available.y < -15f) isNavBarVisible = false
                // If scrolling up, show
                if (available.y > 15f) isNavBarVisible = true
                return Offset.Zero
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
        },
        bottomBar = {
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
                                    navController.navigate("home") { popUpTo("login") { inclusive = true } }
                                },
                                onSkip = {
                                    scope.launch {
                                        themeManager.setSkipLogin(true)
                                        navController.navigate("home") { popUpTo("login") { inclusive = true } }
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
                                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                }
                            )
                        }
                        composable("settings") { 
                            SettingsScreen(
                                viewModel = viewModel,
                                themeManager = themeManager,
                                onNavigateToLimits = { /* Deprecated */ }
                            )
                        }
                        composable("admin_dashboard") {
                            AdminDashboardScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }

            // Floating Navigation Bar
            if (showBars) {
                val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()
                
                if (showChatbot) {
                    AnimatedVisibility(
                        visible = isNavBarVisible,
                        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 100.dp, end = 24.dp)
                            .zIndex(10f)
                    ) {
                        FloatingActionButton(
                            onClick = { isChatbotOpen = true },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Icon(painterResource(id = R.drawable.ic_chatbot), contentDescription = "Data Assistant")
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isNavBarVisible,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .zIndex(10f)
                ) {
                    FloatingNavigationToolbar(
                        currentRoute = currentRoute,
                        isAdmin = isAdmin,
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
        }
    }

    if (isChatbotOpen) {
        ChatbotBottomSheet(viewModel = viewModel, onDismissRequest = { isChatbotOpen = false })
    }

    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    var isDismissedByUser by remember { mutableStateOf(false) }

    updateState?.let { updateInfo ->
        if (!isDismissedByUser || updateInfo.isMandatory) {
            Dialog(
                onDismissRequest = {
                    if (!updateInfo.isMandatory) isDismissedByUser = true
                },
                properties = DialogProperties(
                    dismissOnBackPress = !updateInfo.isMandatory,
                    dismissOnClickOutside = !updateInfo.isMandatory
                )
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Color.Black)
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
                            text = "SYSTEM UPDATE",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "v${updateInfo.versionName}",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
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
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("UPDATE NOW")
                        }
                        if (!updateInfo.isMandatory) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { isDismissedByUser = true },
                                modifier = Modifier.fillMaxWidth().height(44.dp)
                            ) {
                                Text("REMIND ME LATER", color = MaterialTheme.colorScheme.onBackground)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingNavigationToolbar(
    currentRoute: String,
    isAdmin: Boolean,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val items = remember(isAdmin) {
        val base = mutableListOf(
            Triple("home", Icons.Outlined.Home, Icons.Filled.Home),
            Triple("history", Icons.Outlined.List, Icons.Filled.List),
            Triple("profile", Icons.Outlined.Person, Icons.Filled.Person),
            Triple("settings", Icons.Outlined.Settings, Icons.Filled.Settings)
        )
        if (isAdmin) {
            base.add(Triple("admin_dashboard", Icons.Outlined.VerifiedUser, Icons.Filled.VerifiedUser))
        }
        base
    }

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .height(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant) // Solid background instead of glass effect
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { (route, unselectedIcon, selectedIcon) ->
                val selected = currentRoute == route
                Icon(
                    imageVector = if (selected) selectedIcon else unselectedIcon,
                    contentDescription = route,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
                            onNavigate(route)
                        }
                )
            }
        }
    }
}

@Composable
fun PermissionRequestScreen(onRequest: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Usage Access Required", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Grant Permission", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

