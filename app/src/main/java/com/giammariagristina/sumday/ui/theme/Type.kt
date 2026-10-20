package com.giammariagristina.sumday.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.DeviceFontFamilyName
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Stessi font di SkyShapes/SkipMaze: alias di sistema, nessun file custom da includere.
val SumdaySansSerif = FontFamily(Font(DeviceFontFamilyName("sans-serif")))
val SumdaySansSerifBlack = FontFamily(Font(DeviceFontFamilyName("sans-serif-black")))

val SumdayTypography = Typography(
    headlineLarge = TextStyle(fontFamily = SumdaySansSerifBlack, fontWeight = FontWeight.Black, fontSize = 40.sp),
    titleLarge = TextStyle(fontFamily = SumdaySansSerifBlack, fontWeight = FontWeight.Black, fontSize = 22.sp),
    bodyLarge = TextStyle(fontFamily = SumdaySansSerif, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = SumdaySansSerif, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = SumdaySansSerifBlack, fontWeight = FontWeight.Black, fontSize = 16.sp)
)
