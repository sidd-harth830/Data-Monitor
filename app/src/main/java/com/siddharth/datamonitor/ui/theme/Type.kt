package com.siddharth.datamonitor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun createTypography(fontSelection: AppFont): Typography {
    val fontFamily = when (fontSelection) {
        AppFont.PREMIUM_SERIF -> FontFamily.Serif
        AppFont.CLEAN_SANS -> FontFamily.SansSerif
        AppFont.TECH_MODE -> FontFamily.Monospace
    }
    
    val base = Typography()

    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
        titleMedium = base.titleMedium.copy(fontFamily = fontFamily, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
        titleSmall = base.titleSmall.copy(fontFamily = fontFamily, fontWeight = FontWeight.Medium),
        bodyLarge = base.bodyLarge.copy(fontFamily = fontFamily, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.3.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = fontFamily, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.2.sp),
        bodySmall = base.bodySmall.copy(fontFamily = fontFamily, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
        labelLarge = base.labelLarge.copy(fontFamily = fontFamily, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
        labelMedium = base.labelMedium.copy(fontFamily = fontFamily, letterSpacing = 0.5.sp),
        labelSmall = base.labelSmall.copy(fontFamily = fontFamily, letterSpacing = 0.5.sp)
    )
}

