package com.siddharth.datamonitor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.content.Context
import android.content.ContextWrapper

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

val ModernDarkColorScheme = darkColorScheme(
    primary = NeonAccent,
    onPrimary = TextWhite,
    background = PureBlack,
    onBackground = TextWhite,
    surface = GlassSurface,
    onSurface = TextWhite,
    surfaceVariant = GlassSurface,
    onSurfaceVariant = TextGray,
    outline = GlassStroke,
    tertiaryContainer = GlassSurface,
    onTertiaryContainer = NeonAccent,
    error = Color(0xFFFF4C4C)
)

@Composable
fun DataMonitorTheme(
    darkTheme: Boolean = true, // Force dark theme aesthetically
    pureBlack: Boolean = true,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val colorScheme = ModernDarkColorScheme.copy(
        primary = themeColor,
        background = if (pureBlack) PureBlack else MidnightBlue
    )

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

@Composable
fun DynamicThemeProvider(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val themeColorInt by themeManager.themeColorFlow.collectAsStateWithLifecycle(initialValue = DefaultThemeColor.value.toInt())
    val pureBlack by themeManager.pureBlackFlow.collectAsStateWithLifecycle(initialValue = true)
    
    DataMonitorTheme(
        darkTheme = true,
        pureBlack = pureBlack,
        themeColor = Color(themeColorInt),
        content = content
    )
}
