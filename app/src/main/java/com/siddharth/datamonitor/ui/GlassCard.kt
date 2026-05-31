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
    
    // Premium linear glass reflection brush simulating specular light highlights
    val backgroundBrush = if (isLight) {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.12f),
                Color.Black.copy(alpha = 0.03f)
            )
        )
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.03f),
                Color.Black.copy(alpha = 0.15f)
            )
        )
    }
    
    // Glowing gradient border
    val borderBrush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.4f),
            Color.Transparent
        )
    )

    return this
        .clip(shape)
        .background(backgroundBrush)
        .border(
            width = 0.5.dp,
            brush = borderBrush,
            shape = shape
        )
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
        modifier = modifier
    ) {
        // Isolated background glass layer with backdrop blur
        Box(
            modifier = Modifier
                .matchParentSize()
                .glassCard(shape)
        )
        
        // Crisp foreground content container - 100% sharp and unblurred
        CompositionLocalProvider(LocalContentColor provides if (isLight) colorScheme.onBackground else Color.White) {
            Box(
                modifier = Modifier,
                content = content
            )
        }
    }
}
