package com.mskwak.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.mskwak.design.R

private val notoSansKr = FontFamily(
    Font(R.font.notosanskr_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.notosanskr_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.notosanskr_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.notosanskr_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.notosanskr_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.notosanskr_thin, FontWeight.Thin, FontStyle.Normal)
)

// Default Material 3 typography values
val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = notoSansKr),
    displayMedium = baseline.displayMedium.copy(fontFamily = notoSansKr),
    displaySmall = baseline.displaySmall.copy(fontFamily = notoSansKr),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = notoSansKr),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = notoSansKr),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = notoSansKr),
    titleLarge = baseline.titleLarge.copy(fontFamily = notoSansKr),
    titleMedium = baseline.titleMedium.copy(fontFamily = notoSansKr),
    titleSmall = baseline.titleSmall.copy(fontFamily = notoSansKr),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = notoSansKr),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = notoSansKr),
    bodySmall = baseline.bodySmall.copy(fontFamily = notoSansKr),
    labelLarge = baseline.labelLarge.copy(fontFamily = notoSansKr),
    labelMedium = baseline.labelMedium.copy(fontFamily = notoSansKr),
    labelSmall = baseline.labelSmall.copy(fontFamily = notoSansKr),
)
