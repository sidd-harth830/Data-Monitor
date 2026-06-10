package com.siddharth.datamonitor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
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

val ModernLightColorScheme = lightColorScheme(
    primary = NeonAccent,
    onPrimary = Color.White,
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1A1A1A),
    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),
    surfaceVariant = Color(0xFFE0E0E0),
    onSurfaceVariant = Color(0xFF555555),
    outline = Color(0xFFCCCCCC),
    tertiaryContainer = Color.White,
    onTertiaryContainer = NeonAccent,
    error = Color(0xFFFF4C4C)
)

val ModernDarkColorScheme = darkColorScheme(
    primary = NeonAccent,
    onPrimary = TextWhite,
    background = PureBlack,
    onBackground = TextWhite,
    surface = Color(0xFF121212),
    onSurface = TextWhite,
    surfaceVariant = Color(0xFF1E1E1E),
    onSurfaceVariant = TextGray,
    outline = Color(0xFF333333),
    tertiaryContainer = Color(0xFF2C2C2C),
    onTertiaryContainer = NeonAccent,
    error = Color(0xFFFF4C4C)
)

@Composable
fun DataMonitorTheme(
    darkTheme: Boolean = true,
    pureBlack: Boolean = true,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    val baseColorScheme = if (darkTheme) ModernDarkColorScheme else ModernLightColorScheme

    val colorScheme = baseColorScheme.copy(
        primary = themeColor,
        background = if (darkTheme) {
            if (pureBlack) PureBlack else MidnightBlue
        } else {
            baseColorScheme.background
        }
    )

    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
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
    val darkMode by themeManager.darkModeFlow.collectAsStateWithLifecycle(initialValue = true)
    
    DataMonitorTheme(
        darkTheme = darkMode,
        pureBlack = pureBlack,
        themeColor = Color(themeColorInt),
        content = content
    )
}
