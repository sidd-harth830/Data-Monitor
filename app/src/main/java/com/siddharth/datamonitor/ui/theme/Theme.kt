package com.siddharth.datamonitor.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val SpringColorScheme = lightColorScheme(
    primary = Color(0xFFFFAAB8),
    secondary = Color(0xFFA8DF8E),
    tertiary = Color(0xFFFFAAB8),
    background = Color(0xFFF0FFDF),
    surface = Color(0xFFFFD8DF),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val DesertColorScheme = lightColorScheme(
    primary = Color(0xFFC7522A),
    secondary = Color(0xFF008585),
    tertiary = Color(0xFF74A892),
    background = Color(0xFFFBF2C4),
    surface = Color(0xFFE5C185),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF8BAE66),
    secondary = Color(0xFFEBD5AB),
    tertiary = Color(0xFF628141),
    background = Color(0xFF1B211A),
    surface = Color(0xFF628141),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val MidnightAmoledColorScheme = darkColorScheme(
    primary = Color(0xFF00FFFF),
    secondary = Color(0xFF00CCCC),
    tertiary = Color(0xFF00FFFF),
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val SolarizedLightColorScheme = lightColorScheme(
    primary = Color(0xFF268BD2),
    secondary = Color(0xFF2AA198),
    tertiary = Color(0xFF268BD2),
    background = Color(0xFFFDF6E3),
    surface = Color(0xFFEEE8D5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val OceanDeepColorScheme = darkColorScheme(
    primary = Color(0xFF00FFFF),
    secondary = Color(0xFF008080),
    tertiary = Color(0xFF00FFFF),
    background = Color(0xFF0D1B2A),
    surface = Color(0xFF1B263B),
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val SunsetBlazeColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700),
    secondary = Color(0xFFFFA500),
    tertiary = Color(0xFFFFD700),
    background = Color(0xFF3A0A0A),
    surface = Color(0xFF5A1818),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFF00),
    secondary = Color(0xFFFF00FF),
    tertiary = Color(0xFFFFFF00),
    background = Color(0xFF000000),
    surface = Color(0xFF1A001A),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LavenderHazeColorScheme = lightColorScheme(
    primary = Color(0xFF8B5CF6),
    secondary = Color(0xFFA855F7),
    tertiary = Color(0xFF8B5CF6),
    background = Color(0xFFF3E8FF),
    surface = Color(0xFFE9D5FF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

private val MatrixColorScheme = darkColorScheme(
    primary = Color(0xFF00FF00),
    secondary = Color(0xFF00CC00),
    tertiary = Color(0xFF00FF00),
    background = Color(0xFF000000),
    surface = Color(0xFF0A140A),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun DataMonitorTheme(
    theme: AppTheme = AppTheme.FOREST,
    appAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val baseScheme = when (theme) {
        AppTheme.SPRING -> SpringColorScheme
        AppTheme.DESERT -> DesertColorScheme
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.MIDNIGHT_AMOLED -> MidnightAmoledColorScheme
        AppTheme.SOLARIZED_LIGHT -> SolarizedLightColorScheme
        AppTheme.OCEAN_DEEP -> OceanDeepColorScheme
        AppTheme.SUNSET_BLAZE -> SunsetBlazeColorScheme
        AppTheme.CYBERPUNK -> CyberpunkColorScheme
        AppTheme.LAVENDER_HAZE -> LavenderHazeColorScheme
        AppTheme.MATRIX -> MatrixColorScheme
    }
    
    val colorScheme = if (appAccentColor != null) {
        baseScheme.copy(primary = appAccentColor)
    } else {
        baseScheme
    }
    
    val typography = createTypography()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = 
                    (theme == AppTheme.SPRING || theme == AppTheme.DESERT || theme == AppTheme.SOLARIZED_LIGHT || theme == AppTheme.LAVENDER_HAZE)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
fun DynamicThemeProvider(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val currentTheme by themeManager.themeFlow.collectAsStateWithLifecycle(initialValue = AppTheme.FOREST)

    DataMonitorTheme(
        theme = currentTheme,
        appAccentColor = null,
        content = content
    )
}

