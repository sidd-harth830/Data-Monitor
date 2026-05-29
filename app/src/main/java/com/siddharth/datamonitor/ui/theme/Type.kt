package com.siddharth.datamonitor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Map the custom Acorn font to the theme typography.
val AcornFontFamily = FontFamily(
    androidx.compose.ui.text.font.Font(com.siddharth.datamonitor.R.font.acorn)
)

// Legacy Font Settings hook expected by MainActivity
object FontSettings {
    var onFontLoadFailed: (() -> Unit)? = null
    fun triggerFontLoadFailed(e: Throwable) {
        onFontLoadFailed?.invoke()
    }
}

// Backwards compatibility / alias bindings
val PremiumFontFamily: FontFamily = AcornFontFamily
val OutfitFontFamily: FontFamily = AcornFontFamily
val PlusJakartaSansFontFamily: FontFamily = AcornFontFamily
val SansSerifFontFamily: FontFamily = AcornFontFamily

// Ensure custom Typography object can be explicitly referenced
val AppTypography: Typography = createTypography()

fun createTypography(profile: FontProfile = FontProfile.DEFAULT): Typography {
    // Dynamic system font family mapping completely overruled with the custom Acorn font family
    val activeFontFamily = AcornFontFamily

    return Typography(
        // Display styles using Acorn Font Family
        displayLarge = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.2).sp
        ),
        displaySmall = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),

        // Headline styles using Acorn Font Family
        headlineLarge = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),

        // Title Header styles using Acorn Font Family
        titleLarge = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp
        ),
        titleMedium = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),

        // Body styles using Acorn Font Family
        bodyLarge = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // Label styles using Acorn Font Family
        labelLarge = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = activeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp
        )
    )
}
