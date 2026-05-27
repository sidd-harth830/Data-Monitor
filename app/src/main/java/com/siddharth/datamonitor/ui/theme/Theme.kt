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
            onPrimary = text,
            onSecondary = text,
            onTertiary = text,
            onBackground = text,
            onSurface = text
        )
    } else {
        darkColorScheme(
            primary = primary,
            secondary = secondary,
            tertiary = primary,
            background = background,
            surface = surface,
            onPrimary = text,
            onSecondary = text,
            onTertiary = text,
            onBackground = text,
            onSurface = text
        )
    }
}

val palettes = mapOf(
    AppTheme.SPRING to ThemePalette(Color(0xFFA8DF8E), Color(0xFFFFAAB8), Color(0xFFF0FFDF), Color(0xFFFFD8DF), Color.Black, true),
    AppTheme.DESERT to ThemePalette(Color(0xFFC7522A), Color(0xFF74A892), Color(0xFFFBF2C4), Color(0xFFE5C185), Color(0xFF008585), true),
    AppTheme.FOREST to ThemePalette(Color(0xFF628141), Color(0xFF8BAE66), Color(0xFF1B211A), Color(0xFF1B211A), Color(0xFFEBD5AB), false),
    AppTheme.MIDNIGHT_AMOLED to ThemePalette(Color(0xFF00FFFF), Color(0xFF00CCCC), Color(0xFF000000), Color(0xFF000000), Color.White, false),
    AppTheme.SOLARIZED_LIGHT to ThemePalette(Color(0xFF268BD2), Color(0xFF2AA198), Color(0xFFFDF6E3), Color(0xFFEEE8D5), Color.Black, true),
    AppTheme.OCEAN_DEEP to ThemePalette(Color(0xFF008080), Color(0xFFFFDC00), Color(0xFF001F3F), Color(0xFF1B263B), Color.White, false),
    AppTheme.SUNSET_BLAZE to ThemePalette(Color(0xFFFF5722), Color(0xFFFFD54F), Color(0xFF3E0000), Color(0xFF5A1818), Color.White, false),
    AppTheme.CYBERPUNK to ThemePalette(Color(0xFFFFFF00), Color(0xFFFF00FF), Color(0xFF000000), Color(0xFF1A001A), Color.White, false),
    AppTheme.LAVENDER_HAZE to ThemePalette(Color(0xFF9C27B0), Color(0xFFFFCDD2), Color(0xFFF3E5F5), Color(0xFFE9D5FF), Color.Black, true),
    AppTheme.MATRIX to ThemePalette(Color(0xFF00FF00), Color(0xFF00FFFF), Color(0xFF000000), Color(0xFF0A140A), Color.White, false)
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
    val palette = palettes[theme] ?: palettes[AppTheme.FOREST]!!
    val baseScheme = palette.toColorScheme()
    
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
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = palette.isLight
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
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val defaultTheme = if (isSystemDark) AppTheme.MIDNIGHT_AMOLED else AppTheme.LAVENDER_HAZE
    val currentThemeRaw by themeManager.themeFlow.collectAsStateWithLifecycle(initialValue = null)
    val currentTheme = currentThemeRaw ?: defaultTheme

    DataMonitorTheme(
        theme = currentTheme,
        appAccentColor = null,
        content = content
    )
}

