package io.sukhuat.dingo.common.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

/**
 * Custom color properties not included in Material3 ColorScheme
 */
data class ExtendedColors(
    val surfaceGradientStart: Color,
    val surfaceGradientMiddle: Color,
    val surfaceGradientEnd: Color,
    val cardBackground: Color,
    val elevatedSurface: Color,
    val backgroundVariant: Color,
    val surfaceTint: Color
)

// Provide the extended colors through CompositionLocal
val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        surfaceGradientStart = Color.Unspecified,
        surfaceGradientMiddle = Color.Unspecified,
        surfaceGradientEnd = Color.Unspecified,
        cardBackground = Color.Unspecified,
        elevatedSurface = Color.Unspecified,
        backgroundVariant = Color.Unspecified,
        surfaceTint = Color.Unspecified
    )
}

private val MountainSunriseDarkColorScheme = darkColorScheme(
    primary = RusticGold,
    onPrimary = White,
    primaryContainer = DeepIndigo,
    onPrimaryContainer = White,

    secondary = DeepPurple,
    onSecondary = White,
    secondaryContainer = MidnightBlue,
    onSecondaryContainer = White,

    tertiary = AmberHorizon,
    onTertiary = White,
    tertiaryContainer = MountainShadow,
    onTertiaryContainer = White,

    background = DarkBackgroundVariant,
    onBackground = White,
    surface = MidnightBlue,
    onSurface = White,

    error = DustyRose,
    onError = White,

    outline = MountainShadow.copy(alpha = 0.7f),
    outlineVariant = MountainShadow.copy(alpha = 0.4f),

    surfaceTint = DarkSurfaceTint,
    surfaceVariant = DarkBackgroundVariant
)

private val MountainSunriseLightColorScheme = lightColorScheme(
    primary = RusticGold,
    onPrimary = White,
    primaryContainer = LightBackgroundVariant,
    onPrimaryContainer = DeepIndigo,

    secondary = DeepPurple,
    onSecondary = White,
    secondaryContainer = LightBackgroundVariant,
    onSecondaryContainer = DeepIndigo,

    tertiary = AmberHorizon,
    onTertiary = White,
    tertiaryContainer = LightBackgroundVariant,
    onTertiaryContainer = DeepIndigo,

    background = LightBackgroundVariant,
    onBackground = DeepIndigo,
    surface = LightBackgroundVariant,
    onSurface = DeepIndigo,

    error = DustyRose,
    onError = White,

    outline = MountainShadow.copy(alpha = 0.5f),
    outlineVariant = MountainShadow.copy(alpha = 0.2f),

    surfaceTint = LightSurfaceTint,
    surfaceVariant = LightBackgroundVariant
)

// Extended colors for dark theme
private val DarkExtendedColors = ExtendedColors(
    surfaceGradientStart = SunriseStart,
    surfaceGradientMiddle = SunriseMid,
    surfaceGradientEnd = SunriseEnd,
    cardBackground = DarkCardBackground,
    elevatedSurface = DarkElevatedSurface,
    backgroundVariant = DarkBackgroundVariant,
    surfaceTint = DarkSurfaceTint
)

// Extended colors for light theme
private val LightExtendedColors = ExtendedColors(
    surfaceGradientStart = LightBackgroundVariant,
    surfaceGradientMiddle = LightBackgroundVariant.copy(alpha = 0.7f),
    surfaceGradientEnd = AmberHorizon.copy(alpha = 0.2f),
    cardBackground = LightCardBackground,
    elevatedSurface = LightElevatedSurface,
    backgroundVariant = LightBackgroundVariant,
    surfaceTint = LightSurfaceTint
)

@Composable
fun MountainSunriseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> MountainSunriseDarkColorScheme
        else -> MountainSunriseLightColorScheme
    }

    val extendedColors = when {
        darkTheme -> DarkExtendedColors
        else -> LightExtendedColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepIndigo.toArgb() // Always use DeepIndigo for status bar
        }
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MountainSunriseTypography,
            content = content
        )
    }
}

// Helper function to access extended colors
object MountainSunriseTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}

// Keeping the old theme name for backward compatibility
@Composable
fun DingoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MountainSunriseTheme(darkTheme, content)
}
