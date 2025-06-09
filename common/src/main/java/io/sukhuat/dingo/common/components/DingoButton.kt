package io.sukhuat.dingo.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.BorderThickness
import io.sukhuat.dingo.common.theme.ButtonCornerRadius
import io.sukhuat.dingo.common.theme.ButtonHeight
import io.sukhuat.dingo.common.theme.DeepIndigo
import io.sukhuat.dingo.common.theme.DeepPurple
import io.sukhuat.dingo.common.theme.IconSizeSmall
import io.sukhuat.dingo.common.theme.MountainShadow
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.theme.SpaceSmall
import io.sukhuat.dingo.common.theme.White

/**
 * Button types supported by DingoButton
 */
enum class ButtonType {
    FILLED, OUTLINED, TEXT
}

/**
 * A reusable button component with Mountain Sunrise design system
 * @param text Text to display on the button
 * @param onClick Called when the button is clicked
 * @param modifier Modifier to be applied to the button
 * @param type Type of button (FILLED, OUTLINED, TEXT)
 * @param isLoading Whether to show a loading indicator
 * @param isEnabled Whether the button is enabled
 * @param leadingIcon Optional icon to display before the text
 * @param trailingIcon Optional icon to display after the text
 * @param contentPadding Padding values for the button content
 */
@Composable
fun DingoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    type: ButtonType = ButtonType.FILLED,
    isLoading: Boolean = false,
    isEnabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
) {
    val buttonModifier = modifier
        .height(ButtonHeight)

    // Define colors based on our Mountain Sunrise palette
    val containerColor = when (type) {
        ButtonType.FILLED -> RusticGold
        ButtonType.OUTLINED -> Color.Transparent
        ButtonType.TEXT -> Color.Transparent
    }

    val contentColor = when (type) {
        ButtonType.FILLED -> White
        ButtonType.OUTLINED -> DeepIndigo
        ButtonType.TEXT -> DeepPurple
    }

    val disabledContainerColor = when (type) {
        ButtonType.FILLED -> RusticGold.copy(alpha = 0.5f)
        ButtonType.OUTLINED -> Color.Transparent
        ButtonType.TEXT -> Color.Transparent
    }

    val disabledContentColor = when (type) {
        ButtonType.FILLED -> White.copy(alpha = 0.5f)
        ButtonType.OUTLINED -> DeepIndigo.copy(alpha = 0.5f)
        ButtonType.TEXT -> DeepPurple.copy(alpha = 0.5f)
    }

    val border = when (type) {
        ButtonType.OUTLINED -> BorderStroke(BorderThickness, MountainShadow)
        else -> null
    }

    val buttonContent: @Composable () -> Unit = {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
            } else {
                // Show button content
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading icon
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(IconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(SpaceSmall))
                    }

                    // Text
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center
                    )

                    // Trailing icon
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(SpaceSmall))
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            modifier = Modifier.size(IconSizeSmall)
                        )
                    }
                }
            }
        }
    }

    // Sharp corners for all buttons (0dp radius)
    val shape = RoundedCornerShape(ButtonCornerRadius)

    when (type) {
        ButtonType.FILLED -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled && !isLoading,
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = disabledContainerColor,
                    disabledContentColor = disabledContentColor
                ),
                border = border
            ) {
                buttonContent()
            }
        }
        ButtonType.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled && !isLoading,
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = disabledContentColor
                ),
                border = border
            ) {
                buttonContent()
            }
        }
        ButtonType.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled && !isLoading,
                shape = shape,
                contentPadding = contentPadding,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = contentColor,
                    disabledContentColor = disabledContentColor
                )
            ) {
                buttonContent()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoButtonPreview() {
    MountainSunriseTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DingoButton(
                text = "Primary Button",
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoButtonOutlinedPreview() {
    MountainSunriseTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DingoButton(
                text = "Outlined Button",
                onClick = {},
                type = ButtonType.OUTLINED,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoButtonTextPreview() {
    MountainSunriseTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DingoButton(
                text = "Text Button",
                onClick = {},
                type = ButtonType.TEXT,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoButtonLoadingPreview() {
    MountainSunriseTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            DingoButton(
                text = "Loading Button",
                onClick = {},
                isLoading = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
