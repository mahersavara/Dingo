package io.sukhuat.dingo.common.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Font sizes
val SmallFontSize = 12.sp
val MediumFontSize = 16.sp
val LargeFontSize = 20.sp
val XLargeFontSize = 24.sp
val XXLargeFontSize = 32.sp

// Typography set
val DingoTypography = Typography(
    // Large titles, like screen headers
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = XXLargeFontSize,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    // Medium titles, like section headers
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = XLargeFontSize,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Small titles, like card headers
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = LargeFontSize,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    // Primary body text
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = MediumFontSize,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    // Secondary body text
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    // Small body text, like captions
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = SmallFontSize,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // Button text
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = MediumFontSize,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)
