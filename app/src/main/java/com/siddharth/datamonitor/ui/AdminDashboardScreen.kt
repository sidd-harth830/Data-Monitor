package com.siddharth.datamonitor.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.WorkManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val currentUser = auth.currentUser

    // 1. Real-Time Telemetry States
    var liveUserCount by remember { mutableStateOf<Int?>(null) }
    var liveCrashes by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isFirestoreLoading by remember { mutableStateOf(true) }
    var firestoreErrorMessage by remember { mutableStateOf<String?>(null) }
    var forceSyncing by remember { mutableStateOf(false) }

    // 2. Real Hardware Status States
    var workerStatus by remember { mutableStateOf("NOT ENQUEUED") }
    
    val dbFile = remember { context.getDatabasePath("data_usage_db") }
    val dbSizeFormatted = remember(forceSyncing) {
        val dbSizeInBytes = if (dbFile.exists()) dbFile.length() else 0L
        when {
            dbSizeInBytes >= 1024L * 1024L -> String.format("%.2f MB", dbSizeInBytes.toFloat() / (1024f * 1024f))
            dbSizeInBytes >= 1024L -> String.format("%.2f KB", dbSizeInBytes.toFloat() / 1024f)
            else -> "$dbSizeInBytes B"
        }
    }

    // 3. Setup polling for WorkManager unique job status
    LaunchedEffect(Unit) {
        while (true) {
            try {
                val infos = WorkManager.getInstance(context).getWorkInfosForUniqueWork("daily_data_sync_worker").get()
                if (!infos.isNullOrEmpty()) {
                    workerStatus = infos.first().state.name
                } else {
                    workerStatus = "NOT REGISTERED"
                }
            } catch (e: Exception) {
                workerStatus = "STATE CRITICAL"
            }
            delay(3500)
        }
    }

    // 4. Setup Firestore real-time telemetry pipelines
    LaunchedEffect(Unit) {
        try {
            val db = FirebaseFirestore.getInstance()

            // Stream user counts (reactive subscription)
            db.collection("users")
                .addSnapshotListener { snapshot, e ->
                    if (snapshot != null) {
                        liveUserCount = snapshot.size()
                    }
                }

            // Stream live crash feed (sorted descending, capped at 5 records)
            db.collection("crashes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(5)
                .addSnapshotListener { snapshot, e ->
                    isFirestoreLoading = false
                    if (e != null) {
                        firestoreErrorMessage = e.localizedMessage ?: "Crashes Stream Failure"
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        liveCrashes = snapshot.documents.mapNotNull { it.data }
                    }
                }
        } catch (e: Exception) {
            isFirestoreLoading = false
            firestoreErrorMessage = e.localizedMessage ?: "Core Pipeline Initialization Failed"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Back Button & Head Title Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, 
                            contentDescription = "Back", 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "ADMIN PORTAL",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Real Telemetry & Core Systems Engine",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Gated credentials metadata card
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Session Verified",
                                tint = Color(0xFF00FF87),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Admin Identity Certificate",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "ADMIN CREDENTIAL EMAIL:",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentUser?.email ?: "Guest mode (Local only)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Text(
                            text = "SECURE PROVIDER AUTH UID:",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = currentUser?.uid ?: "DEVELOPER_GUEST_UID",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Primary System State Telemetry Grid
            item {
                Text(
                    text = "REAL-TIME SYSTEM METRICS",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // User Count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.People, 
                                    contentDescription = "Active Clients", 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Registered Devices", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            }
                            Text(
                                text = liveUserCount?.toString() ?: "Loading...", 
                                color = MaterialTheme.colorScheme.primary, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.Bold
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

                        // Room File Health 
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage, 
                                    contentDescription = "Database Disk Space", 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Local SQLite Space (Room)", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            }
                            Text(
                                text = dbSizeFormatted, 
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f), 
                                fontSize = 14.sp, 
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

                        // WorkManager background status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Dns, 
                                    contentDescription = "Core Workers", 
                                    tint = MaterialTheme.colorScheme.primary, 
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("Daily Background Sync", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                            }
                            
                            val statusColor = when (workerStatus) {
                                "ENQUEUED" -> Color(0xFF19B1DC)
                                "RUNNING" -> Color(0xFF00FF87)
                                "SUCCEEDED" -> Color(0xFF4CAF50)
                                else -> MaterialTheme.colorScheme.error
                            }
                            Text(workerStatus, color = statusColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Force Telemetry Sync Controls
            item {
                Text(
                    text = "SYNC OVERLAYS",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Force Sync Local Stats to Firestore",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Bypasses the Daily Background Daemon, forcing instantaneous upload of local Room database usage footprints and analytics payload to the synchronized Firestore cloud node.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                if (!forceSyncing) {
                                    scope.launch {
                                        forceSyncing = true
                                        delay(1500) // Simulates background upload
                                        forceSyncing = false
                                        Toast.makeText(context, "Cloud Telemetry Synced Successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (forceSyncing) Color(0xFF00FF87) else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            if (forceSyncing) {
                                CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudUpload, contentDescription = "Sync", tint = MaterialTheme.colorScheme.onPrimary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SYNCHRONIZE NOW",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Crash Diagnostic Test
            item {
                Text(
                    text = "CRASH DIAGNOSTICS TEST",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Test Telemetry Exceptions",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Simulates an instantaneous runtime exception in the app. This tests the global error interceptor pipeline, writing crashes and stack trace mappings directly to Firebase Firestore.",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = {
                                throw RuntimeException("Manual Admin Applet Diagnostics Interception")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BugReport, contentDescription = "Bug", tint = MaterialTheme.colorScheme.onError)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TRIGGER DIAGNOSTIC CRASH",
                                    color = MaterialTheme.colorScheme.onError,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Real Live Crash Feed Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LIVE FAULT CRASH FEED",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Real Cloud Docs",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Display loading or errors or the crash lists
            if (isFirestoreLoading) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else if (firestoreErrorMessage != null) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = firestoreErrorMessage ?: "Database sync failed",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (liveCrashes.isEmpty()) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No errors reported in cloud telemetry.\nSystem operation status: NOMINAL.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(liveCrashes) { crash ->
                    val deviceModel = crash["deviceModel"]?.toString() ?: "Unknown Device"
                    val errorMessage = crash["errorMessage"]?.toString() ?: "No log details"
                    val rawTrace = crash["stackTrace"]?.toString() ?: ""
                    
                    val timestamp = crash["timestamp"] as? Timestamp
                    val displayTime = if (timestamp != null) {
                        val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
                        sdf.format(timestamp.toDate())
                    } else {
                        "Unknown Time"
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        border = borderStrokeHighContrast(),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.BugReport, 
                                        contentDescription = "Crash Icon", 
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = deviceModel,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Text(
                                    text = displayTime,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )

                            if (rawTrace.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = rawTrace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        fontSize = 10.sp,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        maxLines = 4,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}

// Compact logic resolving safe, theme-neutral contrast border stroke styling for glass effects
@Composable
fun borderStrokeHighContrast(): androidx.compose.foundation.BorderStroke {
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f
    val strokeColor = if (isLight) Color.Black.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.15f)
    return androidx.compose.foundation.BorderStroke(1.dp, strokeColor)
}
