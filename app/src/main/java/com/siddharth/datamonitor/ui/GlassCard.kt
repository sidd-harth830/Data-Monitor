package com.siddharth.datamonitor.ui

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun Modifier.glassCard(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier {
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.background.red > 0.5f && colorScheme.background.green > 0.5f
    
    // Transparent glass backdrop: White (0.05f) for dark mode, black (0.05f) for light mode
    val backgroundColor = if (isLight) {
        Color.Black.copy(alpha = 0.05f)
    } else {
        Color.White.copy(alpha = 0.05f)
    }
    
    // Ultra-thin high-contrast border: White (0.2f) for dark mode, black (0.12f) for light mode
    val borderColor = if (isLight) {
        Color.Black.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    val baseModifier = this
        .clip(shape)
        .background(backgroundColor)
        .border(
            border = BorderStroke(0.5.dp, borderColor),
            shape = shape
        )

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        baseModifier.blur(12.dp, BlurredEdgeTreatment(shape))
    } else {
        baseModifier
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.background.red > 0.5f && colorScheme.background.green > 0.5f

    Box(
        modifier = modifier.glassCard(shape)
    ) {
        CompositionLocalProvider(LocalContentColor provides if (isLight) colorScheme.onBackground else Color.White) {
            Box(
                modifier = Modifier,
                content = content
            )
        }
    }
}
