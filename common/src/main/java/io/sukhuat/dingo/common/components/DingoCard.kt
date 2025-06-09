package io.sukhuat.dingo.common.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.CardCornerRadius
import io.sukhuat.dingo.common.theme.DingoTheme
import io.sukhuat.dingo.common.theme.ElevationMedium
import io.sukhuat.dingo.common.theme.SpaceMedium

/**
 * A reusable card component for the Dingo app
 * @param modifier Modifier to be applied to the card
 * @param title Optional title for the card
 * @param onClick Optional click handler for the card
 * @param shape Shape of the card
 * @param backgroundColor Background color of the card
 * @param contentColor Content color of the card
 * @param border Optional border for the card
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
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    border: BorderStroke? = null,
    elevation: Dp = ElevationMedium,
    isLoading: Boolean = false,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = border,
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(SpaceMedium)
        ) {
            // Title
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = contentColor,
                    modifier = Modifier.padding(bottom = SpaceMedium)
                )
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
}

@Preview(showBackground = true)
@Composable
fun DingoCardPreview() {
    DingoTheme {
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
fun DingoCardLoadingPreview() {
    DingoTheme {
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
