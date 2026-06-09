package com.siddharth.datamonitor.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.siddharth.datamonitor.api.AiOptimizerViewModel
import kotlinx.coroutines.launch

@Composable
fun AiOptimizerCard(
    dataUsageViewModel: DataUsageViewModel,
    modifier: Modifier = Modifier
) {
    val aiViewModel: AiOptimizerViewModel = viewModel()
    
    val insights by aiViewModel.insights.collectAsStateWithLifecycle()
    val isLoading by aiViewModel.isLoading.collectAsStateWithLifecycle()
    val error by aiViewModel.error.collectAsStateWithLifecycle()
    
    val weekRecords by dataUsageViewModel.weekRecords.collectAsStateWithLifecycle()
    val todayMobile by dataUsageViewModel.todayMobile.collectAsStateWithLifecycle()
    val todayWifi by dataUsageViewModel.todayWifi.collectAsStateWithLifecycle()

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AI OPTIMIZER",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = {
                        aiViewModel.fetchInsights(weekRecords, todayMobile, todayWifi)
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (isLoading) "Analyzing..." else "Insight",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            AnimatedVisibility(visible = error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            AnimatedVisibility(visible = insights != null) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = insights ?: "",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
