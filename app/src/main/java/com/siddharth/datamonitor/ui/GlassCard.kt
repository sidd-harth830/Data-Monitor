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
    shape: Shape = RoundedCornerShape(6.dp), // Stark Vercel style (4dp to 8dp max)
    content: @Composable BoxScope.() -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    // Stark stark background matching themes directly
    val isLight = colorScheme.background.red > 0.5f && colorScheme.background.green > 0.5f
    
    val backgroundColor = if (isLight) Color(0xFFFFFFFF) else Color(0xFF000000)
    val borderColor = if (isLight) Color(0xFFEAEAEA) else Color(0xFF333333) // Subtle 1dp border

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(
                width = 1.dp, // Subtle exactly 1dp gray border
                color = borderColor,
                shape = shape
            )
    ) {
        // Flat styling - strictly no background blur, no drop shadows, no transparency gradients
        CompositionLocalProvider(LocalContentColor provides if (isLight) Color.Black else Color.White) {
            Box(
                modifier = Modifier,
                content = content
            )
        }
    }
}
