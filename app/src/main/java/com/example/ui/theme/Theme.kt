package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val OledColorScheme = darkColorScheme(
    primary = PrimaryNeon,
    secondary = SecondaryNeon,
    tertiary = WifiActive,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = DarkBackground,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val NordColorScheme = darkColorScheme(
    primary = NordPrimary,
    secondary = NordSecondary,
    tertiary = NordPrimary,
    background = NordBackground,
    surface = NordBackground,
    onPrimary = NordBackground,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val CyberpunkColorScheme = darkColorScheme(
    primary = CyberpunkPrimary,
    secondary = CyberpunkSecondary,
    tertiary = CyberpunkPrimary,
    background = CyberpunkBackground,
    surface = CyberpunkBackground,
    onPrimary = CyberpunkBackground,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val LightGlassColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightPrimary,
    background = LightBackground,
    surface = LightBackground,
    onPrimary = LightBackground,
    onSecondary = LightTextPrimary,
    onTertiary = LightTextPrimary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
)

@Composable
fun DataMonitorTheme(
    theme: AppTheme = AppTheme.OLED_PITCH_BLACK,
    font: AppFont = AppFont.ACORN,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.OLED_PITCH_BLACK -> OledColorScheme
        AppTheme.MINIMALIST_NORD -> NordColorScheme
        AppTheme.CYBERPUNK_NEON -> CyberpunkColorScheme
        AppTheme.LIGHT_GLASS -> LightGlassColorScheme
    }
    
    val typography = createTypography(font)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = theme == AppTheme.LIGHT_GLASS
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
    val currentTheme by themeManager.themeFlow.collectAsStateWithLifecycle(initialValue = AppTheme.OLED_PITCH_BLACK)
    val currentFont by themeManager.fontFlow.collectAsStateWithLifecycle(initialValue = AppFont.ACORN)

    DataMonitorTheme(theme = currentTheme, font = currentFont, content = content)
}
