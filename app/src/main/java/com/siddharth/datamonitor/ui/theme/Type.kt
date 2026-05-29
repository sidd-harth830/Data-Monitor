package com.siddharth.datamonitor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.google.firebase.crashlytics.FirebaseCrashlytics

// Configuration & state for dynamic font load failure notifier
object FontSettings {
    var onFontLoadFailed: (() -> Unit)? = null
    private var hasLoggedFailed = false

    fun triggerFontLoadFailed(e: Throwable) {
        if (!hasLoggedFailed) {
            hasLoggedFailed = true
            try {
                FirebaseCrashlytics.getInstance().recordException(e)
            } catch (ex: Throwable) {
                // Safe check if Firebase not initialized
            }
            onFontLoadFailed?.invoke()
        }
    }
}

// Safer Google Font Provider Setup
val googleFontProvider: GoogleFont.Provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.siddharth.datamonitor.R.array.com_google_android_gms_fonts_certs
)

// Premium Outfit Font Family for all Display, Headline, and Title headers
val OutfitFontFamily: FontFamily = try {
    val fontName = GoogleFont("Outfit")
    FontFamily(
        Font(googleFont = fontName, fontProvider = googleFontProvider, weight = FontWeight.Normal),
        Font(googleFont = fontName, fontProvider = googleFontProvider, weight = FontWeight.Medium),
        Font(googleFont = fontName, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = fontName, fontProvider = googleFontProvider, weight = FontWeight.Bold)
    )
} catch (e: Throwable) {
    FontSettings.triggerFontLoadFailed(e)
    FontFamily.SansSerif
}

// Premium Plus Jakarta Sans Font Family for all Body and Label text
val PlusJakartaSansFontFamily: FontFamily = try {
    val fontName = GoogleFont("Plus Jakarta Sans")
    FontFamily(
        Font(googleFont = fontName, fontProvider = googleFontProvider, weight = FontWeight.Normal),
        Font(googleFont = fontName, fontProvider = googleFontProvider, weight = FontWeight.Medium),
        Font(googleFont = fontName, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
        Font(googleFont = fontName, fontProvider = googleFontProvider, weight = FontWeight.Bold)
    )
} catch (e: Throwable) {
    FontSettings.triggerFontLoadFailed(e)
    FontFamily.SansSerif
}

// Backwards compatibility / alias bindings
val PremiumFontFamily: FontFamily = PlusJakartaSansFontFamily
val AcornFontFamily: FontFamily = OutfitFontFamily
val SansSerifFontFamily: FontFamily = FontFamily.SansSerif

fun createTypography(profile: FontProfile = FontProfile.DEFAULT): Typography {
    // Standardizing on the premium multi-font typography system
    val displayFontFamily = OutfitFontFamily
    val bodyFontFamily = PlusJakartaSansFontFamily

    return Typography(
        // Display styles using Outfit Font Family
        displayLarge = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.2).sp
        ),
        displaySmall = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),

        // Headline styles using Outfit Font Family
        headlineLarge = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),

        // Title Header styles using Outfit Font Family
        titleLarge = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp
        ),
        titleMedium = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),

        // Body styles using Plus Jakarta Sans Font Family
        bodyLarge = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // Label styles using Plus Jakarta Sans Font Family
        labelLarge = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp
        )
    )
}
