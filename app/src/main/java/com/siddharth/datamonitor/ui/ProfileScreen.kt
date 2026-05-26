package com.siddharth.datamonitor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.siddharth.datamonitor.ui.theme.PrimaryNeon
import com.siddharth.datamonitor.ui.theme.SecondaryNeon
import com.siddharth.datamonitor.ui.theme.TextPrimary
import com.siddharth.datamonitor.ui.theme.TextSecondary
import com.siddharth.datamonitor.ui.theme.WifiActive

@Composable
fun ProfileScreen(viewModel: DataUsageViewModel) {
    val history by viewModel.history.collectAsStateWithLifecycle()
    val totalSavings by viewModel.totalSavings.collectAsStateWithLifecycle()
    val peakUsage by viewModel.peakUsage.collectAsStateWithLifecycle()
    val averageUsage by viewModel.averageUsage.collectAsStateWithLifecycle()

    val peakDate = remember(history, peakUsage) {
        val match = history.firstOrNull { (it.mobileBytes + it.wifiBytes) == peakUsage }
        match?.dateStr ?: "N/A"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "PROFILE",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(48.dp))
        
        ProfileStatCard(
            title = "Total Accumulated Savings", 
            value = formatBytes(totalSavings), 
            valueColor = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ProfileStatCard(
            title = "All-Time Peak Usage Day", 
            value = formatBytes(peakUsage), 
            valueColor = MaterialTheme.colorScheme.secondary, 
            subtitle = peakDate
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        ProfileStatCard(
            title = "Average Daily Consumed Bandwidth", 
            value = formatBytes(averageUsage), 
            valueColor = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
fun ProfileStatCard(title: String, value: String, valueColor: androidx.compose.ui.graphics.Color, subtitle: String? = null) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = MaterialTheme.colorScheme.onSecondary,
                fontSize = 12.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = valueColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = MaterialTheme.typography.displaySmall.fontFamily
                )
                if (subtitle != null && subtitle != "N/A") {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "on $subtitle",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
    }
}
