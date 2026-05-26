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

private val MinimalLightColorScheme = lightColorScheme(
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

private val PremiumGlassColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFA855F7), // Purple Neon
    secondary = androidx.compose.ui.graphics.Color(0xFF3B82F6), // Vibrant Blue
    tertiary = androidx.compose.ui.graphics.Color(0xFFA855F7),
    background = androidx.compose.ui.graphics.Color(0xFF0D0B18), // High contrast deep space
    surface = androidx.compose.ui.graphics.Color(0xFF161426),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF0D0B18),
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

@Composable
fun DataMonitorTheme(
    theme: AppTheme = AppTheme.OLED_DARK,
    font: AppFont = AppFont.ACORN,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.OLED_DARK -> OledColorScheme
        AppTheme.CYBER_NEON -> CyberpunkColorScheme
        AppTheme.MINIMAL_LIGHT -> MinimalLightColorScheme
        AppTheme.PREMIUM_GLASS -> PremiumGlassColorScheme
    }
    
    val typography = createTypography(font)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = (theme == AppTheme.MINIMAL_LIGHT)
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
    val currentTheme by themeManager.themeFlow.collectAsStateWithLifecycle(initialValue = AppTheme.OLED_DARK)
    val currentFont by themeManager.fontFlow.collectAsStateWithLifecycle(initialValue = AppFont.ACORN)

    DataMonitorTheme(theme = currentTheme, font = currentFont, content = content)
}

