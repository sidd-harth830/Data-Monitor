package com.siddharth.datamonitor.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.shimmerEffect(): Modifier = composed {
    val shimmerColors = listOf(
        Color.White,
        Color.White,
        Color.White
    )

    val transition = rememberInfiniteTransition(label = "shimmer_transition")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate_anim"
    )

    background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(x = translateAnim, y = 0f),
            end = Offset(x = translateAnim + 300f, y = 0f)
        )
    )
}
