package com.siddharth.datamonitor.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import com.siddharth.datamonitor.R

// Secure downloadable fonts provider pointing to Google's authority
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms.fonts",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Initialize downloadable Google fonts explicitly as requested
val OswaldFont = FontFamily(Font(googleFont = GoogleFont("Oswald"), fontProvider = provider))
val BricolageFont = FontFamily(Font(googleFont = GoogleFont("Bricolage Grotesque"), fontProvider = provider))
val AktFont = FontFamily(Font(googleFont = GoogleFont("Akt"), fontProvider = provider))

// Modern aliases for backwards compatibility
val OswaldFontFamily = OswaldFont
val BricolageFontFamily = BricolageFont
val AktFontFamily = AktFont

// Legacy Font Settings hook expected by MainActivity
object FontSettings {
    var onFontLoadFailed: (() -> Unit)? = null
    fun triggerFontLoadFailed(e: Throwable) {
        onFontLoadFailed?.invoke()
    }
}

// Backwards compatibility / alias bindings
val AcornFontFamily: FontFamily = BricolageFontFamily
val PremiumFontFamily: FontFamily = BricolageFontFamily
val OutfitFontFamily: FontFamily = BricolageFontFamily
val PlusJakartaSansFontFamily: FontFamily = BricolageFontFamily
val SansSerifFontFamily: FontFamily = BricolageFontFamily

// Ensure custom Typography object can be explicitly referenced
val AppTypography: Typography = createTypography()

fun createTypography(profile: FontProfile = FontProfile.OSWALD): Typography {
    val family = when (profile) {
        FontProfile.SYSTEM_DEFAULT -> FontFamily.Default
        FontProfile.OSWALD -> OswaldFontFamily
        FontProfile.BRICOLAGE -> BricolageFontFamily
        FontProfile.AKT -> AktFontFamily
    }

    return Typography(
        // Display styles
        displayLarge = TextStyle(
            lineHeight = 40.sp,
        ),
        displayMedium = TextStyle(
            lineHeight = 36.sp,
        ),
        displaySmall = TextStyle(
            lineHeight = 32.sp,
        ),

        // Headline styles
        headlineLarge = TextStyle(
            lineHeight = 28.sp,
        ),
        headlineMedium = TextStyle(
            lineHeight = 26.sp,
        ),
        headlineSmall = TextStyle(
            lineHeight = 24.sp,
        ),

        // Title Header styles
        titleLarge = TextStyle(
            lineHeight = 24.sp,
        ),
        titleMedium = TextStyle(
            lineHeight = 22.sp,
        ),
        titleSmall = TextStyle(
            lineHeight = 20.sp,
        ),

        // Body styles
        bodyLarge = TextStyle(
            lineHeight = 24.sp,
        ),
        bodyMedium = TextStyle(
            lineHeight = 20.sp,
        ),
        bodySmall = TextStyle(
            lineHeight = 16.sp,
        ),

        // Label styles
        labelLarge = TextStyle(
            lineHeight = 20.sp,
        ),
        labelMedium = TextStyle(
            lineHeight = 16.sp,
        ),
        labelSmall = TextStyle(
            lineHeight = 14.sp,
        )
    )
}
