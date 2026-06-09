package com.siddharth.datamonitor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
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

@Composable
fun DataMonitorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pureBlack: Boolean = false,
    themeColor: Color = DefaultThemeColor,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeColor == DefaultThemeColor) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        rememberDynamicColorScheme(
            seedColor = themeColor,
            isDark = darkTheme,
            style = PaletteStyle.TonalSpot
        )
    }.pureBlack(apply = pureBlack && darkTheme)

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

fun ColorScheme.pureBlack(apply: Boolean): ColorScheme {
    return if (apply) {
        this.copy(
            surface = Color.Black,
            background = Color.Black,
            surfaceContainer = Color.Black,
            surfaceContainerLow = Color.Black,
            surfaceContainerLowest = Color.Black,
            surfaceContainerHigh = Color(0xFF121212),
            surfaceContainerHighest = Color(0xFF1E1E1E)
        )
    } else this
}

@Composable
fun DynamicThemeProvider(
    themeManager: ThemeManager,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val themeColorInt by themeManager.themeColorFlow.collectAsStateWithLifecycle(initialValue = DefaultThemeColor.value.toInt())
    val pureBlack by themeManager.pureBlackFlow.collectAsStateWithLifecycle(initialValue = false)
    
    DataMonitorTheme(
        darkTheme = isSystemDark,
        pureBlack = pureBlack,
        themeColor = Color(themeColorInt),
        content = content
    )
}
