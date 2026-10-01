@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package com.mskwak.common_ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
    displayLarge = baseline.displayLarge.enlarged(fontFamily),
    displayMedium = baseline.displayMedium.enlarged(fontFamily),
    displaySmall = baseline.displaySmall.enlarged(fontFamily),
    headlineLarge = baseline.headlineLarge.enlarged(fontFamily),
    headlineMedium = baseline.headlineMedium.enlarged(fontFamily),
    headlineSmall = baseline.headlineSmall.enlarged(fontFamily),
    titleLarge = baseline.titleLarge.enlarged(fontFamily),
    titleMedium = baseline.titleMedium.enlarged(fontFamily),
    titleSmall = baseline.titleSmall.enlarged(fontFamily),
    bodyLarge = baseline.bodyLarge.enlarged(fontFamily),
    bodyMedium = baseline.bodyMedium.enlarged(fontFamily),
    bodySmall = baseline.bodySmall.enlarged(fontFamily),
    labelLarge = baseline.labelLarge.enlarged(fontFamily),
    labelMedium = baseline.labelMedium.enlarged(fontFamily),
    labelSmall = baseline.labelSmall.enlarged(fontFamily),
)

// 기본 스타일의 자간·굵기를 유지하고 글자 크기와 행 높이만 1sp 확대
private fun TextStyle.enlarged(fontFamily: FontFamily): TextStyle = copy(
    fontFamily = fontFamily,
    fontSize = (fontSize.value + 1).sp,
    lineHeight = (lineHeight.value + 1).sp
)
