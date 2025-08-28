package io.sukhuat.dingo.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.AccentBorderThickness
import io.sukhuat.dingo.common.theme.BorderThickness
import io.sukhuat.dingo.common.theme.CardCornerRadius
import io.sukhuat.dingo.common.theme.DeepIndigo
import io.sukhuat.dingo.common.theme.DeepPurple
import io.sukhuat.dingo.common.theme.ElevationMedium
import io.sukhuat.dingo.common.theme.MountainShadow
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.theme.SpaceMedium
import io.sukhuat.dingo.common.theme.White

/**
 * A reusable card component with Mountain Sunrise design system
 * @param modifier Modifier to be applied to the card
 * @param title Optional title for the card
 * @param onClick Optional click handler for the card
 * @param shape Shape of the card
 * @param useGradientBackground Whether to use a gradient background
 * @param contentColor Content color of the card
 * @param border Optional border for the card
 * @param accentBorder Optional accent border at the top of the card
 * @param elevation Elevation of the card
 * @param isLoading Whether to show a loading indicator
 * @param content Content to display in the card
 */
@Composable
fun DingoCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    onClick: (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(CardCornerRadius),
    useGradientBackground: Boolean = false,
    contentColor: Color? = null, // Make contentColor nullable to use theme defaults
    border: BorderStroke? = BorderStroke(BorderThickness, MountainShadow),
    accentBorder: Boolean = false,
    elevation: Dp = ElevationMedium,
    isLoading: Boolean = false,
    content: @Composable () -> Unit
) {
    val extendedColors = MountainSunriseTheme.extendedColors
    val isDarkTheme = isSystemInDarkTheme()

    // Determine content color based on theme if not explicitly provided
    val actualContentColor = contentColor ?: if (isDarkTheme) {
        White // Use white text in dark mode for better contrast
    } else {
        DeepIndigo // Use dark text in light mode
    }

    // Create a modifier for the accent border if needed
    val cardModifier = if (accentBorder) {
        modifier.padding(top = AccentBorderThickness)
    } else {
        modifier
    }

    // Determine background color or gradient
    val backgroundColor = if (!useGradientBackground) {
        extendedColors.cardBackground
    } else {
        // We'll use a gradient background in the Card content
        Color.Transparent
    }

    // Create a gradient border effect for cards with accent borders
    val borderModifier = if (accentBorder) {
        Modifier.drawBehind {
            // Draw a gradient stroke at the top of the card
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        RusticGold.copy(alpha = 0.7f),
                        DeepPurple,
                        RusticGold
                    )
                ),
                topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width, AccentBorderThickness.toPx())
            )
        }
    } else {
        Modifier
    }

    // Use clickable Card only if onClick is provided, otherwise use non-clickable Card
    if (onClick != null) {
        Card(
            modifier = cardModifier.then(borderModifier),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = actualContentColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation
            ),
            onClick = onClick
        ) {
            CardContent(
                title = title,
                useGradientBackground = useGradientBackground,
                isLoading = isLoading,
                contentColor = actualContentColor,
                content = content
            )
        }
    } else {
        Card(
            modifier = cardModifier.then(borderModifier),
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor,
                contentColor = actualContentColor
            ),
            border = border,
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation
            )
        ) {
            CardContent(
                title = title,
                useGradientBackground = useGradientBackground,
                isLoading = isLoading,
                contentColor = actualContentColor,
                content = content
            )
        }
    }
}

@Composable
private fun CardContent(
    title: String?,
    useGradientBackground: Boolean,
    isLoading: Boolean,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    val extendedColors = MountainSunriseTheme.extendedColors

    // Background gradient if enabled
    val contentModifier = if (useGradientBackground) {
        Modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    extendedColors.surfaceGradientStart,
                    extendedColors.surfaceGradientMiddle,
                    extendedColors.surfaceGradientEnd
                )
            )
        )
    } else {
        Modifier
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(contentModifier)
            .padding(SpaceMedium)
    ) {
        // Title
        if (title != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = SpaceMedium)
            ) {
                // Apply a gradient to uppercase titles
                if (title.uppercase() == title) {
                    // Create a gradient background behind the title
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        DeepPurple.copy(alpha = 0.2f),
                                        Color.Transparent,
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = RusticGold
                        )
                    }
                } else {
                    // Regular title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = contentColor
                    )
                }
            }
        }

        // Content
        Box(modifier = Modifier.fillMaxWidth()) {
            content()

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoCardPreview() {
    MountainSunriseTheme {
        DingoCard(
            title = "Card Title",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "This is the content of the card. It can contain any composable content.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoCardWithAccentPreview() {
    MountainSunriseTheme {
        DingoCard(
            title = "FEATURED JOURNEY",
            accentBorder = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "This card has an accent border at the top.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoCardGradientPreview() {
    MountainSunriseTheme {
        DingoCard(
            title = "GRADIENT CARD",
            useGradientBackground = true,
            accentBorder = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "This card has a gradient background.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoCardLoadingPreview() {
    MountainSunriseTheme {
        DingoCard(
            title = "Loading Card",
            isLoading = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Loading content...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
