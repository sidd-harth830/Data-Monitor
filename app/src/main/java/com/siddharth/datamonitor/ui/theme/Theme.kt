package com.siddharth.datamonitor.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class ThemePalette(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val text: Color,
    val isLight: Boolean
) {
    fun toColorScheme() = if (isLight) {
        lightColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = primary,
            background = background,
            surface = surface,
            onPrimary = Color.White,            // High-contrast clean white text on primary
            onSecondary = Color.Black,          // High-contrast clear dark text on secondary
            onTertiary = Color.White,
            onBackground = text,
            onSurface = text,
            onSurfaceVariant = VercelLightTextSecondary
        )
    } else {
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = primary,
            background = background,
            surface = surface,
            onPrimary = Color.Black,            // High-contrast clean black text on primary
            onSecondary = Color.White,          // High-contrast clear light text on secondary
            onTertiary = Color.Black,
            onBackground = text,
            onSurface = text,
            onSurfaceVariant = VercelDarkTextSecondary
        )
    }
}

// Map existing theme enums of Data Monitor cleanly to Next.js/Vercel styling tokens
// to ensure seamless backwards compatibility while strictly enforcing the Stark Vercel aesthetics
val palettes = mapOf(
    AppTheme.SPRING to ThemePalette(VercelLightPrimary, VercelLightSecondary, VercelLightBackground, VercelLightSurface, VercelLightTextPrimary, true),
    AppTheme.DESERT to ThemePalette(VercelLightPrimary, VercelLightSecondary, VercelLightBackground, VercelLightSurface, VercelLightTextPrimary, true),
    AppTheme.FOREST to ThemePalette(VercelDarkPrimary, VercelDarkSecondary, VercelDarkBackground, VercelDarkSurface, VercelDarkTextPrimary, false),
    AppTheme.MIDNIGHT_AMOLED to ThemePalette(VercelDarkPrimary, VercelDarkSecondary, VercelDarkBackground, VercelDarkSurface, VercelDarkTextPrimary, false),
    AppTheme.SOLARIZED_LIGHT to ThemePalette(VercelLightPrimary, VercelLightSecondary, VercelLightBackground, VercelLightSurface, VercelLightTextPrimary, true),
    AppTheme.OCEAN_DEEP to ThemePalette(VercelDarkPrimary, VercelDarkSecondary, VercelDarkBackground, VercelDarkSurface, VercelDarkTextPrimary, false),
    AppTheme.SUNSET_BLAZE to ThemePalette(VercelDarkPrimary, VercelDarkSecondary, VercelDarkBackground, VercelDarkSurface, VercelDarkTextPrimary, false),
    AppTheme.CYBERPUNK to ThemePalette(VercelDarkPrimary, VercelDarkSecondary, VercelDarkBackground, VercelDarkSurface, VercelDarkTextPrimary, false),
    AppTheme.LAVENDER_HAZE to ThemePalette(VercelLightPrimary, VercelLightSecondary, VercelLightBackground, VercelLightSurface, VercelLightTextPrimary, true),
    AppTheme.MATRIX to ThemePalette(VercelDarkPrimary, VercelDarkSecondary, VercelDarkBackground, VercelDarkSurface, VercelDarkTextPrimary, false)
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
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontProfile: FontProfile = FontProfile.DEFAULT,
    appAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    // Determine light or dark color scheme based strictly on darkTheme preference and next.js design standards
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = VercelDarkPrimary,
            secondary = VercelDarkSecondary,
            tertiary = StatusSuccess,
            background = VercelDarkBackground,
            surface = VercelDarkSurface,
            onPrimary = VercelDarkOnPrimary,      // Black on White (100% readable)
            onSecondary = VercelDarkOnSecondary,  // White on Slate Gray (100% readable)
            onBackground = VercelDarkTextPrimary,
            onSurface = VercelDarkTextPrimary,
            onSurfaceVariant = VercelDarkTextSecondary,
            outline = VercelDarkBorder,
            outlineVariant = VercelDarkBorder
        )
    } else {
        lightColorScheme(
            primary = VercelLightPrimary,
            secondary = VercelLightSecondary,
            tertiary = StatusSuccess,
            background = VercelLightBackground,
            surface = VercelLightSurface,
            onPrimary = VercelLightOnPrimary,      // White on Black (100% readable)
            onSecondary = VercelLightOnSecondary,  // Black on Light Gray (100% readable)
            onBackground = VercelLightTextPrimary,
            onSurface = VercelLightTextPrimary,
            onSurfaceVariant = VercelLightTextSecondary,
            outline = VercelLightBorder,
            outlineVariant = VercelLightBorder
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context.findActivity()
            if (activity != null) {
                val window = activity.window
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                // Draw navigation bar matching backing theme
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

// Deprecated forest-based interface backward compatibility mapping
@Composable
fun DataMonitorTheme(
    theme: AppTheme,
    fontProfile: FontProfile = FontProfile.DEFAULT,
    appAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val palette = palettes[theme] ?: palettes[AppTheme.MIDNIGHT_AMOLED]!!
    DataMonitorTheme(darkTheme = !palette.isLight, fontProfile = fontProfile, appAccentColor = appAccentColor, content = content)
}

@Composable
fun DynamicThemeProvider(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val monogramTheme by themeManager.monogramThemeFlow.collectAsStateWithLifecycle(initialValue = MonogramTheme.SYSTEM_DEFAULT)
    val fontProfile by themeManager.fontProfileFlow.collectAsStateWithLifecycle(initialValue = FontProfile.DEFAULT)
    
    val isDark = when (monogramTheme) {
        MonogramTheme.SYSTEM_DEFAULT -> isSystemDark
        MonogramTheme.LIGHT_MONOGRAM -> false
        MonogramTheme.DARK_MONOGRAM -> true
    }

    DataMonitorTheme(
        darkTheme = isDark,
        fontProfile = fontProfile,
        appAccentColor = null,
        content = content
    )
}
