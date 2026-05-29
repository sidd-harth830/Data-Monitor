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
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Oswald FontFamily definition using downloadable source
val OswaldFont = GoogleFont("Oswald")
val OswaldFontFamily = FontFamily(
    Font(googleFont = OswaldFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = OswaldFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = OswaldFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = OswaldFont, fontProvider = provider, weight = FontWeight.Bold)
)

// Bricolage Grotesque FontFamily definition using downloadable source
val BricolageFont = GoogleFont("Bricolage Grotesque")
val BricolageFontFamily = FontFamily(
    Font(googleFont = BricolageFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = BricolageFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = BricolageFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = BricolageFont, fontProvider = provider, weight = FontWeight.Bold)
)

// Akt FontFamily definition using downloadable source
val AktFont = GoogleFont("Akt")
val AktFontFamily = FontFamily(
    Font(googleFont = AktFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = AktFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = AktFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = AktFont, fontProvider = provider, weight = FontWeight.Bold)
)

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

fun createTypography(profile: FontProfile = FontProfile.DEFAULT): Typography {
    return Typography(
        // Display styles using Oswald
        displayLarge = TextStyle(
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.5).sp
        ),
        displayMedium = TextStyle(
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = (-0.2).sp
        ),
        displaySmall = TextStyle(
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),

        // Headline styles using Oswald
        headlineLarge = TextStyle(
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = OswaldFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp
        ),

        // Title Header styles using Bricolage Grotesque
        titleLarge = TextStyle(
            fontFamily = BricolageFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp
        ),
        titleMedium = TextStyle(
            fontFamily = BricolageFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.1.sp
        ),
        titleSmall = TextStyle(
            fontFamily = BricolageFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),

        // Body styles using Bricolage Grotesque
        bodyLarge = TextStyle(
            fontFamily = BricolageFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = BricolageFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = BricolageFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),

        // Label styles using Akt
        labelLarge = TextStyle(
            fontFamily = AktFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = AktFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = AktFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.5.sp
        )
    )
}
