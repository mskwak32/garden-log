@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.mskwak.common_ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.mskwak.common_ui.R

val notoSansKr = FontFamily(
    Font(R.font.notosanskr_variable, FontWeight.Thin,     variationSettings = FontVariation.Settings(FontVariation.weight(100))),
    Font(R.font.notosanskr_variable, FontWeight.Light,    variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.notosanskr_variable, FontWeight.Normal,   variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.notosanskr_variable, FontWeight.Medium,   variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.notosanskr_variable, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.notosanskr_variable, FontWeight.Bold,     variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

val notoSansJp = FontFamily(
    Font(R.font.notosansjp_variable, FontWeight.Thin,     variationSettings = FontVariation.Settings(FontVariation.weight(100))),
    Font(R.font.notosansjp_variable, FontWeight.Light,    variationSettings = FontVariation.Settings(FontVariation.weight(300))),
    Font(R.font.notosansjp_variable, FontWeight.Normal,   variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.notosansjp_variable, FontWeight.Medium,   variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.notosansjp_variable, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.notosansjp_variable, FontWeight.Bold,     variationSettings = FontVariation.Settings(FontVariation.weight(700))),
)

// Default Material 3 typography values
val baseline = Typography()

fun appTypography(fontFamily: FontFamily) = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = fontFamily),
    displayMedium = baseline.displayMedium.copy(fontFamily = fontFamily),
    displaySmall = baseline.displaySmall.copy(fontFamily = fontFamily),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = fontFamily),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = fontFamily),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = fontFamily),
    titleLarge = baseline.titleLarge.copy(fontFamily = fontFamily),
    titleMedium = baseline.titleMedium.copy(fontFamily = fontFamily),
    titleSmall = baseline.titleSmall.copy(fontFamily = fontFamily),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = fontFamily),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = fontFamily),
    bodySmall = baseline.bodySmall.copy(fontFamily = fontFamily),
    labelLarge = baseline.labelLarge.copy(fontFamily = fontFamily),
    labelMedium = baseline.labelMedium.copy(fontFamily = fontFamily),
    labelSmall = baseline.labelSmall.copy(fontFamily = fontFamily),
)
