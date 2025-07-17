package io.sukhuat.dingo.common.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Represents different screen size classes
 */
enum class ScreenSizeClass {
    COMPACT, // Phone portrait
    MEDIUM, // Phone landscape, tablet portrait
    EXPANDED // Tablet landscape
}

/**
 * Utility class to provide responsive dimensions and behaviors
 * based on the current screen size
 */
class ResponsiveValues(
    val screenSizeClass: ScreenSizeClass,
    val columns: Int,
    val gridSpacing: Dp,
    val contentPadding: Dp,
    val cardElevation: Dp,
    val iconSize: Dp,
    val headerTextSize: Float,
    val bodyTextSize: Float
)

/**
 * Returns responsive values based on the current screen width
 */
@Composable
fun rememberResponsiveValues(): ResponsiveValues {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    return remember(screenWidth) {
        when {
            screenWidth < 600 -> ResponsiveValues(
                screenSizeClass = ScreenSizeClass.COMPACT,
                columns = 3,
                gridSpacing = 12.dp,
                contentPadding = 16.dp,
                cardElevation = 4.dp,
                iconSize = 40.dp,
                headerTextSize = 1.0f,
                bodyTextSize = 1.0f
            )
            screenWidth < 840 -> ResponsiveValues(
                screenSizeClass = ScreenSizeClass.MEDIUM,
                columns = 4,
                gridSpacing = 16.dp,
                contentPadding = 24.dp,
                cardElevation = 4.dp,
                iconSize = 48.dp,
                headerTextSize = 1.1f,
                bodyTextSize = 1.05f
            )
            else -> ResponsiveValues(
                screenSizeClass = ScreenSizeClass.EXPANDED,
                columns = 6,
                gridSpacing = 20.dp,
                contentPadding = 32.dp,
                cardElevation = 6.dp,
                iconSize = 56.dp,
                headerTextSize = 1.2f,
                bodyTextSize = 1.1f
            )
        }
    }
}

/**
 * Returns the number of columns to use for a grid based on screen size
 */
@Composable
fun rememberResponsiveGridColumns(): Int {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    return remember(screenWidth) {
        when {
            screenWidth < 600 -> 3 // Phone portrait
            screenWidth < 840 -> 4 // Phone landscape or small tablet
            screenWidth < 1080 -> 5 // Medium tablet
            else -> 6 // Large tablet
        }
    }
}

/**
 * Returns a responsive padding value based on screen size
 */
@Composable
fun rememberResponsivePadding(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    return remember(screenWidth) {
        when {
            screenWidth < 600 -> 16.dp
            screenWidth < 840 -> 24.dp
            else -> 32.dp
        }
    }
}

/**
 * Returns responsive grid spacing based on screen size
 */
@Composable
fun rememberResponsiveGridSpacing(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    return remember(screenWidth) {
        when {
            screenWidth < 600 -> 12.dp
            screenWidth < 840 -> 16.dp
            else -> 20.dp
        }
    }
}