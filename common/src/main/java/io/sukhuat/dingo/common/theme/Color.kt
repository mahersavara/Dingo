package io.sukhuat.dingo.common.theme

import androidx.compose.ui.graphics.Color

// Primary Colors
val DeepIndigo = Color(0xFF1F2937)
val MidnightBlue = Color(0xFF2D3748)
val RusticGold = Color(0xFFD69E2E)
val AmberHorizon = Color(0xFFF6AD55)

// Secondary Colors
val MistyLavender = Color(0xFF9F7AEA)
val MountainShadow = Color(0xFF4A5568)
val CloudGray = Color(0xFFE2E8F0)
val SunriseYellow = Color(0xFFF6E05E)

// Accent Colors
val WarmOrange = Color(0xFFED8936)
val DeepPurple = Color(0xFF553C9A)
val DustyRose = Color(0xFFED64A6)

// Common UI Colors
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// Status Colors
val Success = RusticGold
val Error = DustyRose
val Info = MistyLavender
val Warning = AmberHorizon

// Text Colors
val TextPrimary = DeepIndigo
val TextSecondary = MountainShadow
val TextOnDark = White

// Surface Colors
val Surface = White
val Background = White.copy(alpha = 0.97f)
val Divider = MountainShadow.copy(alpha = 0.3f)
val Disabled = CloudGray.copy(alpha = 0.5f)

// Gradient Colors
val SunriseStart = Color(0xFF1F2937) // Deep Indigo
val SunriseMid = Color(0xFF553C9A) // Deep Purple
val SunriseEnd = Color(0xFFED8936) // Warm Orange

// Light Theme Specific
val LightSurfaceTint = RusticGold.copy(alpha = 0.05f)
val LightBackgroundVariant = Color(0xFFF8F6F0) // Warm off-white
val LightCardBackground = White.copy(alpha = 0.85f)
val LightElevatedSurface = White.copy(alpha = 0.92f)

// Dark Theme Specific
val DarkSurfaceTint = RusticGold.copy(alpha = 0.15f)
val DarkBackgroundVariant = Color(0xFF1A2435) // Darker for better contrast
val DarkCardBackground = Color(0xFF1E293B) // Darker card background for better contrast
val DarkElevatedSurface = Color(0xFF2C3A4F) // Adjusted for better contrast

// Overlay Colors
val ScrimLight = Black.copy(alpha = 0.32f)
val ScrimMedium = Black.copy(alpha = 0.48f)
val ScrimHeavy = Black.copy(alpha = 0.62f)
