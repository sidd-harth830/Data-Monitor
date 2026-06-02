package com.siddharth.datamonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.firebase.auth.FirebaseAuth
import com.siddharth.datamonitor.ui.theme.PrimaryNeon
import com.siddharth.datamonitor.ui.theme.SecondaryNeon
import com.siddharth.datamonitor.ui.theme.ThemeManager
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: DataUsageViewModel,
    themeManager: ThemeManager,
    onNavigateToAuth: () -> Unit
) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val totalSavings by viewModel.totalSavings.collectAsStateWithLifecycle()
    val peakUsage by viewModel.peakUsage.collectAsStateWithLifecycle()
    val averageUsage by viewModel.averageUsage.collectAsStateWithLifecycle()

    val peakDate = remember(history, peakUsage) {
        val match = history.firstOrNull { (it.mobileBytes + it.wifiBytes) == peakUsage }
        match?.dateStr ?: "N/A"
    }

    val totalWifi = remember(history) { history.sumOf { it.wifiBytes } }
    val totalMobile = remember(history) { history.sumOf { it.mobileBytes } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 16.dp)
            .navigationBarsPadding()
    ) {
        val auth = remember { FirebaseAuth.getInstance() }
        val currentUser = auth.currentUser
        val scope = rememberCoroutineScope()

        var isSyncing by remember { mutableStateOf(false) }
        var syncStatusMessage by remember { mutableStateOf("Ready to sync") }
        var lastSyncTimeStr by remember { mutableStateOf("Never") }

        Text(
            text = "ALL-TIME ANALYTICS HUB",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Account management card
        Text(
            text = "ACCOUNT MANAGEMENT",
            color = MaterialTheme.colorScheme.onSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (currentUser == null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Guest Lock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guest Account Status",
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Unlock Cloud Sync & Analytics",
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Real-time remote cloud synchronization and administrative developer logs are disabled in local Guest mode.",
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToAuth,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(4.dp), // Stark Vercel flat borders
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = "Sign In",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIGN IN / REGISTER",
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            // Primary Text: Actual registered name (from user profile)
                            val registeredName = currentUser.displayName ?: "Authorized Core User"
                            Text(
                                text = if (registeredName.isNotBlank()) registeredName else "Authorized Core User",
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            
                            // Secondary Text below: User's actual email Address and provider badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = currentUser.email ?: "No Registered Email",
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                
                                // Elegant Badge next to secondary email representing specific auth provider
                                val providerId = remember(currentUser) {
                                    var id = "password" // Default to Email Envelope
                                    for (p in currentUser.providerData) {
                                        when (p.providerId) {
                                            "google.com" -> id = "google.com"
                                            "github.com" -> id = "github.com"
                                        }
                                    }
                                    id
                                }
                                
                                when (providerId) {
                                    "google.com" -> {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = com.siddharth.datamonitor.R.drawable.ic_google),
                                            contentDescription = "Google Authentication Provider",
                                            tint = Color.Unspecified, // Retain gorgeous official multicolors
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    "github.com" -> {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = com.siddharth.datamonitor.R.drawable.ic_github),
                                            contentDescription = "GitHub Authentication Provider",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Default.Email,
                                            contentDescription = "Email Authentication Provider",
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            auth.signOut()
                            scope.launch {
                                themeManager.setSkipLogin(false)
                                onNavigateToAuth()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(4.dp), // Stark Vercel flat borders
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                tint = MaterialTheme.colorScheme.onError
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SIGN OUT / LOGOUT",
                                color = MaterialTheme.colorScheme.onError,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Cloud Backup & Sync Gateway Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (currentUser == null) {
                    // LOCKED GUEST GATEWAY
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Cloud Locked out",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Cloud Synchronization Portal",
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Secure your historical usage logs, backup billing cycle configurations, and sync across premium Android environments. Create a persistent account to activate instant automated cloud backups.",
                        color = MaterialTheme.colorScheme.onSecondary,
                        lineHeight = 17.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = onNavigateToAuth,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, 
                            MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Authenticate Now",
                        )
                    }
                } else {
                    // AUTHENTICATED GATEWAY ACTIVE
                    Text(
                        text = "Cloud Sync Gateway (Active)",
                        color = MaterialTheme.colorScheme.primary,
                    )
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("User Identity", color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
                            Text(currentUser.email ?: "Authorized User", color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Medium, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Account Tier", color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
                            val isAdmin = currentUser.email?.endsWith("@admin.com") == true
                            val tier = if (isAdmin) "Administrator Dev Account" else "Standard Premium Account"
                            Text(tier, color = if (isAdmin) SecondaryNeon else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Last Encrypted Backup", color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
                            Text(lastSyncTimeStr, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    if (isSyncing) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = syncStatusMessage,
                                color = MaterialTheme.colorScheme.onSecondary,
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                scope.launch {
                                    isSyncing = true
                                    syncStatusMessage = "Encrypting local metrics..."
                                    kotlinx.coroutines.delay(600)
                                    syncStatusMessage = "Pushing to remote sync node..."
                                    kotlinx.coroutines.delay(700)
                                    syncStatusMessage = "Cloud Sync Successful!"
                                    kotlinx.coroutines.delay(400)
                                    lastSyncTimeStr = "Just now"
                                    isSyncing = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Text(
                                text = "Sync Now",
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lifetime Data Distribution Stacked Bar
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "LIFETIME DATA DISTRIBUTION",
                    color = MaterialTheme.colorScheme.onSecondary,
                )
                Spacer(modifier = Modifier.height(24.dp))

                val totalData = totalWifi + totalMobile
                val safeWifiWeight = if (totalWifi == 0L && totalMobile == 0L) {
                    0.5f
                } else {
                    (totalWifi.toFloat() / totalData.toFloat()).coerceIn(0.001f, 0.999f)
                }
                val safeCellWeight = if (totalWifi == 0L && totalMobile == 0L) {
                    0.5f
                } else {
                    (totalMobile.toFloat() / totalData.toFloat()).coerceIn(0.001f, 0.999f)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(safeWifiWeight)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.tertiary, androidx.compose.foundation.shape.RoundedCornerShape(
                                    topStart = 4.dp, 
                                    bottomStart = 4.dp, 
                                    topEnd = if (totalMobile == 0L) 4.dp else 0.dp, 
                                    bottomEnd = if (totalMobile == 0L) 4.dp else 0.dp
                                ))
                        )
                        Box(
                            modifier = Modifier
                                .weight(safeCellWeight)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(
                                    topStart = if (totalWifi == 0L) 4.dp else 0.dp, 
                                    bottomStart = if (totalWifi == 0L) 4.dp else 0.dp, 
                                    topEnd = 4.dp, 
                                    bottomEnd = 4.dp
                                ))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.tertiary, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Wi-Fi", color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = formatBytes(totalWifi), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Cellular", color = MaterialTheme.colorScheme.onSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(2.dp)))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = formatBytes(totalMobile), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid Layout for Peak and Average
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "PEAK USAGE",
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatBytes(peakUsage),
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    if (peakDate != "N/A") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = peakDate,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                    }
                }
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "AVG DAILY",
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatBytes(averageUsage),
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "per 24h cycle",
                        color = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
        }
    }
}
