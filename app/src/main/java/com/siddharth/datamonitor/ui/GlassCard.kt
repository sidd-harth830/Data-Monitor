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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.blur
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    // Check if light or dark theme based on MaterialTheme resources
    // Minimal Light backgrounds are clear whites, OLED Dark / Cyber Neon are dark
    val isLight = MaterialTheme.colorScheme.background.red > 0.5f && MaterialTheme.colorScheme.background.green > 0.5f
    
    val tintColor = if (isLight) {
        Color.White.copy(alpha = 0.55f) // solid high contrast light tint
    } else {
        Color.Black.copy(alpha = 0.45f) // solid high contrast dark tint
    }
    
    val borderColor = if (isLight) {
        Color.Black.copy(alpha = 0.12f)
    } else {
        Color.White.copy(alpha = 0.18f)
    }

    Box(
        modifier = modifier
            .clip(shape)
            .border(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, borderColor.copy(alpha = 0.05f))
                ),
                shape = shape
            )
    ) {
        // Background blur layer (does not blur child text content)
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(tintColor)
                .blur(16.dp)
        )
        
        // Children content slot
        CompositionLocalProvider(LocalContentColor provides if (isLight) Color.Black else Color.White) {
            Box(
                modifier = Modifier,
                content = content
            )
        }
    }
}

