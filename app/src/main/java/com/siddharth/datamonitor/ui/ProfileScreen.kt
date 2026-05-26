package com.siddharth.datamonitor.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
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

    val totalWifi = remember(history) { history.sumOf { it.wifiBytes } }
    val totalMobile = remember(history) { history.sumOf { it.mobileBytes } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 24.dp, end = 24.dp, bottom = 120.dp)
            .navigationBarsPadding()
    ) {
        Text(
            text = "ALL-TIME ANALYTICS HUB",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

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
                val wifiWeight = if (totalData > 0) totalWifi.toFloat() / totalData.toFloat() else 0.5f
                val mobileWeight = if (totalData > 0) totalMobile.toFloat() / totalData.toFloat() else 0.5f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                ) {
                    if (totalData > 0) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .weight(wifiWeight)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.tertiary, androidx.compose.foundation.shape.RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp, topEnd = if (mobileWeight == 0f) 12.dp else 0.dp, bottomEnd = if (mobileWeight == 0f) 12.dp else 0.dp))
                            )
                            Box(
                                modifier = Modifier
                                    .weight(mobileWeight)
                                    .fillMaxHeight()
                                    .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.RoundedCornerShape(topStart = if (wifiWeight == 0f) 12.dp else 0.dp, bottomStart = if (wifiWeight == 0f) 12.dp else 0.dp, topEnd = 12.dp, bottomEnd = 12.dp))
                            )
                        }
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
