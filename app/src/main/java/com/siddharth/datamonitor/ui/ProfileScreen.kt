package com.siddharth.datamonitor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Brush
import com.siddharth.datamonitor.ui.theme.PrimaryNeon
import com.siddharth.datamonitor.ui.theme.SecondaryNeon
import com.siddharth.datamonitor.ui.theme.TextPrimary
import com.siddharth.datamonitor.ui.theme.TextSecondary
import com.siddharth.datamonitor.ui.theme.WifiActive
import com.siddharth.datamonitor.ui.theme.ThemeManager
import com.google.firebase.auth.FirebaseAuth
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
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
            .navigationBarsPadding()
    ) {
        val auth = remember { FirebaseAuth.getInstance() }
        val currentUser = auth.currentUser
        val scope = rememberCoroutineScope()

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
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            // Background blur layer (does not blur child text content)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                    .blur(20.dp)
            )

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
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
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
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Unlock Cloud Sync & Analytics",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Real-time remote cloud synchronization and administrative developer logs are disabled in local Guest mode.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToAuth,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
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
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                letterSpacing = 1.sp
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
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(24.dp)),
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
                            Text(
                                text = currentUser.displayName ?: currentUser.email ?: "Authorized User",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val provider = remember(currentUser) {
                                var pName = "Local Email"
                                for (p in currentUser.providerData) {
                                    when (p.providerId) {
                                        "google.com" -> pName = "Google Sign-In"
                                        "github.com" -> pName = "GitHub Sign-In"
                                    }
                                }
                                pName
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Provider: $provider",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (currentUser.email != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Email: ${currentUser.email}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
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
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Total Accumulated Savings Card
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "TOTAL ACCUMULATED SAVINGS",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = formatBytes(totalSavings),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily
                )
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
                    fontSize = 12.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
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
                        .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(safeWifiWeight)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.tertiary, androidx.compose.foundation.shape.RoundedCornerShape(
                                    topStart = 12.dp, 
                                    bottomStart = 12.dp, 
                                    topEnd = if (totalMobile == 0L) 12.dp else 0.dp, 
                                    bottomEnd = if (totalMobile == 0L) 12.dp else 0.dp
                                ))
                        )
                        Box(
                            modifier = Modifier
                                .weight(safeCellWeight)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(
                                    topStart = if (totalWifi == 0L) 12.dp else 0.dp, 
                                    bottomStart = if (totalWifi == 0L) 12.dp else 0.dp, 
                                    topEnd = 12.dp, 
                                    bottomEnd = 12.dp
                                ))
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.tertiary, androidx.compose.foundation.shape.RoundedCornerShape(5.dp)))
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
                            Box(modifier = Modifier.size(10.dp).background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(5.dp)))
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
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatBytes(peakUsage),
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (peakDate != "N/A") {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = peakDate,
                            color = MaterialTheme.colorScheme.onSecondary,
                            fontSize = 11.sp
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
                        fontSize = 11.sp,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatBytes(averageUsage),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "per 24h cycle",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

    }
}
