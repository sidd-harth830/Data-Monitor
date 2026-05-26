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
import androidx.compose.ui.graphics.Color
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
    primary = Color(0xFFA855F7), // Purple Neon
    secondary = Color(0xFF3B82F6), // Vibrant Blue
    tertiary = Color(0xFFA855F7),
    background = Color(0xFF0D0B18), // High contrast deep space
    surface = Color(0xFF161426),
    onPrimary = Color(0xFF0D0B18),
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val MidnightAmoledColorScheme = darkColorScheme(
    primary = Color(0xFFE2E8F0), // clean white/slate
    secondary = Color(0xFFFF007F), // pink neon
    tertiary = Color(0xFF00FFD1), // cyan
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    onPrimary = Color(0xFF000000),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFFFFFFFF),
    onSurface = Color(0xFFFFFFFF),
)

private val SolarizedLightColorScheme = lightColorScheme(
    primary = Color(0xFF268BD2), // Solarized Blue
    secondary = Color(0xFF2AA198), // Solarized Cyan
    tertiary = Color(0xFFD33682), // Solarized Magenta
    background = Color(0xFFFDF6E3), // Solarized Base3 (Beige)
    surface = Color(0xFFEEE8D5), // Solarized Base2 (Lighter beige)
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = Color(0xFF073642), // Solarized Base02 (Deep blue-green)
    onSurface = Color(0xFF073642),
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
    theme: AppTheme = AppTheme.OLED_DARK,
    font: AppFont = AppFont.PREMIUM_SERIF,
    content: @Composable () -> Unit
) {
    val colorScheme = when (theme) {
        AppTheme.OLED_DARK -> OledColorScheme
        AppTheme.CYBER_NEON -> CyberpunkColorScheme
        AppTheme.MINIMAL_LIGHT -> MinimalLightColorScheme
        AppTheme.PREMIUM_GLASS -> PremiumGlassColorScheme
        AppTheme.MIDNIGHT_AMOLED -> MidnightAmoledColorScheme
        AppTheme.SOLARIZED_LIGHT -> SolarizedLightColorScheme
    }
    
    val typography = createTypography(font)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = 
                    (theme == AppTheme.MINIMAL_LIGHT || theme == AppTheme.SOLARIZED_LIGHT)
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
    val currentTheme by themeManager.themeFlow.collectAsStateWithLifecycle(initialValue = AppTheme.OLED_DARK)
    val currentFont by themeManager.fontFlow.collectAsStateWithLifecycle(initialValue = AppFont.PREMIUM_SERIF)

    DataMonitorTheme(theme = currentTheme, font = currentFont, content = content)
}

