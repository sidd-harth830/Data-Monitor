package com.siddharth.datamonitor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp), // Elegant modern round corner
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isLight = colorScheme.background.red > 0.5f && colorScheme.background.green > 0.5f
    
    // Premium Glassmorphism background colors (alpha 0.1 to 0.2)
    val backgroundColor = if (isLight) {
        Color(0xFFFFFFFF).copy(alpha = 0.15f) // Subtle semi-transparent clean white
    } else {
        Color(0xFF0A0A0A).copy(alpha = 0.15f) // Subtle premium semi-transparent dark slate
    }
    
    // Very thin, elegant, high-contrast border (0.5dp) to remove ugly thick borders
    val borderColor = if (isLight) {
        Color.Black.copy(alpha = 0.06f)
    } else {
        Color.White.copy(alpha = 0.12f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 0.5.dp, // 0.5dp elegant high-contrast border
                color = borderColor,
                shape = shape
            )
    ) {
        CompositionLocalProvider(LocalContentColor provides if (isLight) colorScheme.onBackground else Color.White) {
            Box(
                modifier = Modifier,
                content = content
            )
        }
    }
}
