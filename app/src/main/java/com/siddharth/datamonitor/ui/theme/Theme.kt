package com.siddharth.datamonitor.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.platform.LocalContext
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
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

val LocalUiStyle = staticCompositionLocalOf { UiStyle.DEFAULT_MONOGRAM }

@Composable
fun DataMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    uiStyle: UiStyle = UiStyle.DEFAULT_MONOGRAM,
    materialPalette: MaterialColorPalette = MaterialColorPalette.DYNAMIC,
    fontProfile: FontProfile = FontProfile.SYSTEM_DEFAULT,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val colorScheme = if (uiStyle == UiStyle.MATERIAL_3) {
        when (materialPalette) {
            MaterialColorPalette.DYNAMIC -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (darkTheme) darkColorScheme() else lightColorScheme()
            }
            MaterialColorPalette.OCEAN_BLUE -> if (darkTheme) {
                darkColorScheme(primary = Color(0xFF82B1FF), secondary = Color(0xFF448AFF), background = Color(0xFF121212), surface = Color(0xFF1E1E1E))
            } else {
                lightColorScheme(primary = Color(0xFF1976D2), secondary = Color(0xFF0D47A1))
            }
            MaterialColorPalette.FOREST_GREEN -> if (darkTheme) {
                darkColorScheme(primary = Color(0xFFA5D6A7), secondary = Color(0xFF81C784), background = Color(0xFF121212), surface = Color(0xFF1E1E1E))
            } else {
                lightColorScheme(primary = Color(0xFF388E3C), secondary = Color(0xFF1B5E20))
            }
            MaterialColorPalette.AMETHYST_PURPLE -> if (darkTheme) {
                darkColorScheme(primary = Color(0xFFCE93D8), secondary = Color(0xFFBA68C8), background = Color(0xFF121212), surface = Color(0xFF1E1E1E))
            } else {
                lightColorScheme(primary = Color(0xFF7B1FA2), secondary = Color(0xFF4A148C))
            }
            MaterialColorPalette.SUNSET_ORANGE -> if (darkTheme) {
                darkColorScheme(primary = Color(0xFFFFCC80), secondary = Color(0xFFFFB74D), background = Color(0xFF121212), surface = Color(0xFF1E1E1E))
            } else {
                lightColorScheme(primary = Color(0xFFF57C00), secondary = Color(0xFFE65100))
            }
        }
    } else {
        if (darkTheme) {
            darkColorScheme(
                primary = VercelDarkPrimary,
                secondary = VercelDarkSecondary,
                tertiary = StatusSuccess,
                background = VercelDarkBackground,
                surface = VercelDarkSurface,
                onPrimary = VercelDarkOnPrimary,
                onSecondary = VercelDarkOnSecondary,
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
                onPrimary = VercelLightOnPrimary,
                onSecondary = VercelLightOnSecondary,
                onBackground = VercelLightTextPrimary,
                onSurface = VercelLightTextPrimary,
                onSurfaceVariant = VercelLightTextSecondary,
                outline = VercelLightBorder,
                outlineVariant = VercelLightBorder
            )
        }
    }

    val view = LocalView.current
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

    val currentTypography = if (uiStyle == UiStyle.DEFAULT_MONOGRAM) {
        createTypography(FontProfile.SYSTEM_DEFAULT)
    } else {
        createTypography(fontProfile)
    }

    CompositionLocalProvider(LocalUiStyle provides uiStyle) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = currentTypography,
            content = content
        )
    }
}

// Deprecated forest-based interface backward compatibility mapping
@Composable
fun DataMonitorTheme(
    theme: AppTheme,
    fontProfile: FontProfile = FontProfile.OSWALD,
    appAccentColor: Color? = null,
    content: @Composable () -> Unit
) {
    val palette = palettes[theme] ?: palettes[AppTheme.MIDNIGHT_AMOLED]!!
    DataMonitorTheme(darkTheme = !palette.isLight, uiStyle = UiStyle.DEFAULT_MONOGRAM, fontProfile = fontProfile, content = content)
}

@Composable
fun DynamicThemeProvider(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val uiStyle by themeManager.uiStyleFlow.collectAsStateWithLifecycle(initialValue = UiStyle.DEFAULT_MONOGRAM)
    val monogramTheme by themeManager.monogramThemeFlow.collectAsStateWithLifecycle(initialValue = MonogramTheme.SYSTEM_DEFAULT)
    val materialPalette by themeManager.materialPaletteFlow.collectAsStateWithLifecycle(initialValue = MaterialColorPalette.DYNAMIC)
    val materialDarkMode by themeManager.materialDarkModeFlow.collectAsStateWithLifecycle(initialValue = MaterialDarkMode.SYSTEM)
    val fontProfile by themeManager.fontProfileFlow.collectAsStateWithLifecycle(initialValue = FontProfile.OSWALD)
    
    val isDark = if (uiStyle == UiStyle.MATERIAL_3) {
        when (materialDarkMode) {
            MaterialDarkMode.LIGHT -> false
            MaterialDarkMode.DARK -> true
            MaterialDarkMode.SYSTEM -> isSystemDark
        }
    } else {
        when (monogramTheme) {
            MonogramTheme.LIGHT_MONOGRAM -> false
            MonogramTheme.DARK_MONOGRAM -> true
            else -> isSystemDark
        }
    }

    DataMonitorTheme(
        darkTheme = isDark,
        uiStyle = uiStyle,
        materialPalette = materialPalette,
        fontProfile = fontProfile,
        content = content
    )
}
