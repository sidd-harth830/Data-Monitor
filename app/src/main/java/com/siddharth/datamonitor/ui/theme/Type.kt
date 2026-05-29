package com.siddharth.datamonitor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
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

// Loading Plus Jakarta Sans premium Google Font safely
val PremiumFontFamily: FontFamily = try {
    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = com.siddharth.datamonitor.R.array.com_google_android_gms_fonts_certs
    )
    val fontName = GoogleFont("Plus Jakarta Sans")
    FontFamily(
        Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.SemiBold),
        Font(googleFont = fontName, fontProvider = provider, weight = FontWeight.Bold)
    )
} catch (e: Throwable) {
    FontSettings.triggerFontLoadFailed(e)
    FontFamily.SansSerif
}

// Clean default standard system Sans Serif for layout body, microcopy, and actions
val SansSerifFontFamily = FontFamily.SansSerif

fun createTypography(profile: FontProfile = FontProfile.DEFAULT): Typography {
    val displayFontFamily = if (profile == FontProfile.PREMIUM) PremiumFontFamily else SansSerifFontFamily
    val bodyFontFamily = SansSerifFontFamily

    return Typography(
        // Display & Headers
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
        
        // section headers
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
        
        // Sans Serif for labels, body, inputs, and button helpers
        titleSmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
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
