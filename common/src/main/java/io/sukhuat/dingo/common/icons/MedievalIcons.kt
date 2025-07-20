package io.sukhuat.dingo.common.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Medieval/Doodle style custom icons for the Dingo app
 * These icons follow a hand-drawn, medieval manuscript aesthetic
 */
object MedievalIcons {

    /**
     * Medieval scroll with quill - for language/text settings
     */
    val Scroll: ImageVector
        get() = materialIcon(name = "Medieval.Scroll") {
            materialPath {
                // Scroll body
                moveTo(4f, 6f)
                curveTo(4f, 4f, 6f, 2f, 8f, 2f)
                lineTo(16f, 2f)
                curveTo(18f, 2f, 20f, 4f, 20f, 6f)
                lineTo(20f, 18f)
                curveTo(20f, 20f, 18f, 22f, 16f, 22f)
                lineTo(8f, 22f)
                curveTo(6f, 22f, 4f, 20f, 4f, 18f)
                close()

                // Decorative lines on scroll
                moveTo(7f, 8f)
                lineTo(17f, 8f)
                moveTo(7f, 11f)
                lineTo(17f, 11f)
                moveTo(7f, 14f)
                lineTo(14f, 14f)

                // Quill feather
                moveTo(15f, 5f)
                curveTo(16f, 4f, 17f, 4f, 18f, 5f)
                curveTo(19f, 6f, 19f, 7f, 18f, 8f)
                lineTo(16f, 10f)
                lineTo(14f, 8f)
                close()
            }
        }

    /**
     * Medieval sun with rays - for brightness/dark mode
     */
    val Sun: ImageVector
        get() = materialIcon(name = "Medieval.Sun") {
            materialPath {
                // Sun center
                moveTo(12f, 8f)
                curveTo(14.2f, 8f, 16f, 9.8f, 16f, 12f)
                curveTo(16f, 14.2f, 14.2f, 16f, 12f, 16f)
                curveTo(9.8f, 16f, 8f, 14.2f, 8f, 12f)
                curveTo(8f, 9.8f, 9.8f, 8f, 12f, 8f)
                close()

                // Medieval-style rays (wavy)
                moveTo(12f, 2f)
                curveTo(12.5f, 3f, 11.5f, 3f, 12f, 4f)

                moveTo(12f, 20f)
                curveTo(12.5f, 21f, 11.5f, 21f, 12f, 22f)

                moveTo(4f, 12f)
                curveTo(3f, 11.5f, 3f, 12.5f, 2f, 12f)

                moveTo(20f, 12f)
                curveTo(21f, 11.5f, 21f, 12.5f, 22f, 12f)

                // Diagonal rays
                moveTo(6.34f, 6.34f)
                curveTo(5.5f, 5.5f, 5.5f, 7f, 4.93f, 5.93f)

                moveTo(17.66f, 17.66f)
                curveTo(18.5f, 18.5f, 18.5f, 17f, 19.07f, 18.07f)

                moveTo(6.34f, 17.66f)
                curveTo(5.5f, 18.5f, 5.5f, 17f, 4.93f, 18.07f)

                moveTo(17.66f, 6.34f)
                curveTo(18.5f, 5.5f, 18.5f, 7f, 19.07f, 5.93f)
            }
        }

    /**
     * Medieval lute/harp - for sound/music
     */
    val Lute: ImageVector
        get() = materialIcon(name = "Medieval.Lute") {
            materialPath {
                // Lute body (teardrop shape)
                moveTo(12f, 4f)
                curveTo(8f, 4f, 5f, 7f, 5f, 11f)
                curveTo(5f, 15f, 8f, 18f, 12f, 18f)
                curveTo(16f, 18f, 19f, 15f, 19f, 11f)
                curveTo(19f, 7f, 16f, 4f, 12f, 4f)
                close()

                // Sound hole
                moveTo(12f, 10f)
                curveTo(13f, 10f, 14f, 11f, 14f, 12f)
                curveTo(14f, 13f, 13f, 14f, 12f, 14f)
                curveTo(11f, 14f, 10f, 13f, 10f, 12f)
                curveTo(10f, 11f, 11f, 10f, 12f, 10f)
                close()

                // Neck
                moveTo(12f, 4f)
                lineTo(12f, 2f)

                // Tuning pegs
                moveTo(10f, 2f)
                lineTo(14f, 2f)

                // Strings
                moveTo(9f, 6f)
                lineTo(9f, 16f)
                moveTo(12f, 6f)
                lineTo(12f, 16f)
                moveTo(15f, 6f)
                lineTo(15f, 16f)
            }
        }

    /**
     * Medieval bell - for notifications/vibration
     */
    val Bell: ImageVector
        get() = materialIcon(name = "Medieval.Bell") {
            materialPath {
                // Bell body
                moveTo(12f, 2f)
                curveTo(13f, 2f, 14f, 3f, 14f, 4f)
                lineTo(14f, 5f)
                curveTo(17f, 6f, 19f, 9f, 19f, 12f)
                curveTo(19f, 15f, 19f, 16f, 20f, 17f)
                lineTo(4f, 17f)
                curveTo(5f, 16f, 5f, 15f, 5f, 12f)
                curveTo(5f, 9f, 7f, 6f, 10f, 5f)
                lineTo(10f, 4f)
                curveTo(10f, 3f, 11f, 2f, 12f, 2f)
                close()

                // Bell clapper
                moveTo(12f, 8f)
                curveTo(12.5f, 8f, 13f, 8.5f, 13f, 9f)
                lineTo(13f, 13f)
                curveTo(13f, 13.5f, 12.5f, 14f, 12f, 14f)
                curveTo(11.5f, 14f, 11f, 13.5f, 11f, 13f)
                lineTo(11f, 9f)
                curveTo(11f, 8.5f, 11.5f, 8f, 12f, 8f)
                close()

                // Sound waves
                moveTo(9f, 19f)
                curveTo(9f, 20f, 10f, 21f, 12f, 21f)
                curveTo(14f, 21f, 15f, 20f, 15f, 19f)
            }
        }

    /**
     * Medieval cloud with wind - for backup/cloud storage
     */
    val CloudWind: ImageVector
        get() = materialIcon(name = "Medieval.CloudWind") {
            materialPath {
                // Cloud body
                moveTo(6f, 10f)
                curveTo(4f, 10f, 2f, 12f, 2f, 14f)
                curveTo(2f, 16f, 4f, 18f, 6f, 18f)
                lineTo(18f, 18f)
                curveTo(20f, 18f, 22f, 16f, 22f, 14f)
                curveTo(22f, 12f, 20f, 10f, 18f, 10f)
                curveTo(18f, 8f, 16f, 6f, 14f, 6f)
                curveTo(12f, 6f, 10f, 8f, 10f, 10f)
                curveTo(8f, 10f, 6f, 10f, 6f, 10f)
                close()

                // Wind lines (medieval style - wavy)
                moveTo(2f, 8f)
                curveTo(4f, 7f, 6f, 9f, 8f, 8f)

                moveTo(16f, 4f)
                curveTo(18f, 3f, 20f, 5f, 22f, 4f)

                moveTo(4f, 20f)
                curveTo(6f, 19f, 8f, 21f, 10f, 20f)
            }
        }
}

/**
 * Composable function to draw medieval-style icons
 */
@Composable
fun MedievalIcon(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Canvas(
        modifier = modifier.size(size)
    ) {
        // Add a subtle parchment-like background
        drawCircle(
            color = tint.copy(alpha = 0.1f),
            radius = size.toPx() / 2,
            center = Offset(size.toPx() / 2, size.toPx() / 2)
        )
    }
    icon()
}
