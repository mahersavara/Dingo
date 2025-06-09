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

// Letter spacing for vintage feel
val HeaderTracking = 0.05.sp
val BodyTracking = 0.02.sp

// Define custom font families
// Note: These would need to be added to the res/font directory
// For now, we'll use system serif fonts as placeholders
val PlayfairDisplay = FontFamily.Serif
val Lora = FontFamily.Serif
val CormorantGaramond = FontFamily.Serif

// Typography set
val MountainSunriseTypography = Typography(
    // Large titles, like screen headers
    headlineLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = XXLargeFontSize,
        lineHeight = 40.sp,
        letterSpacing = HeaderTracking
    ),
    // Medium titles, like section headers
    headlineMedium = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Bold,
        fontSize = XLargeFontSize,
        lineHeight = 32.sp,
        letterSpacing = HeaderTracking
    ),
    // Small titles, like card headers
    headlineSmall = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = LargeFontSize,
        lineHeight = 28.sp,
        letterSpacing = HeaderTracking
    ),
    // Section headers in all caps
    titleLarge = TextStyle(
        fontFamily = CormorantGaramond,
        fontWeight = FontWeight.Medium,
        fontSize = MediumFontSize,
        lineHeight = 24.sp,
        letterSpacing = HeaderTracking * 1.5
    ),
    // Primary body text
    bodyLarge = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.Normal,
        fontSize = MediumFontSize,
        lineHeight = 24.sp,
        letterSpacing = BodyTracking
    ),
    // Secondary body text
    bodyMedium = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = BodyTracking
    ),
    // Small body text, like captions
    bodySmall = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.Normal,
        fontSize = SmallFontSize,
        lineHeight = 16.sp,
        letterSpacing = BodyTracking
    ),
    // Button text
    labelLarge = TextStyle(
        fontFamily = PlayfairDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = MediumFontSize,
        lineHeight = 20.sp,
        letterSpacing = HeaderTracking
    ),
    // Small labels and accent text
    labelMedium = TextStyle(
        fontFamily = CormorantGaramond,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = HeaderTracking
    )
)
