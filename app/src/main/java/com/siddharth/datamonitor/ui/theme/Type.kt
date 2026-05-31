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
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.2).sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),

        // Headline styles
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-0.5).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = (-0.25).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),

        // Title Header styles
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),

        // Body styles
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // Label styles
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 1.sp
        ),
        labelSmall = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 1.sp
        )
    )
}
