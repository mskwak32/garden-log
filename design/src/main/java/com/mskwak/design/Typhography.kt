package com.mskwak.design

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp

private val defaultTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(
        includeFontPadding = false
    ),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )
)

val XLarge_bold = defaultTextStyle.copy(
    fontWeight = FontWeight.Bold,
    fontSize = 25.sp
)
val XLarge_medium = defaultTextStyle.copy(
    fontWeight = FontWeight.Medium,
    fontSize = 25.sp
)
val XLarge_regular = defaultTextStyle.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 25.sp
)
val Large_bold = defaultTextStyle.copy(
    fontWeight = FontWeight.Bold,
    fontSize = 20.sp
)
val Large_medium = defaultTextStyle.copy(
    fontWeight = FontWeight.Medium,
    fontSize = 20.sp
)
val Large_regular = defaultTextStyle.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp
)
val Medium_bold = defaultTextStyle.copy(
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp
)
val Medium_medium = defaultTextStyle.copy(
    fontWeight = FontWeight.Medium,
    fontSize = 16.sp
)
val Medium_regular = defaultTextStyle.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
)
val Regular_bold = defaultTextStyle.copy(
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp
)
val Regular_medium = defaultTextStyle.copy(
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp
)
val Regular_regular = defaultTextStyle.copy(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
)