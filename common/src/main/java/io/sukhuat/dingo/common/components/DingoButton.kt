package io.sukhuat.dingo.common.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.ButtonCornerRadius
import io.sukhuat.dingo.common.theme.ButtonHeight
import io.sukhuat.dingo.common.theme.DingoTheme
import io.sukhuat.dingo.common.theme.IconSizeSmall
import io.sukhuat.dingo.common.theme.SpaceSmall

/**
 * Button types supported by DingoButton
 */
enum class ButtonType {
    FILLED, OUTLINED, TEXT
}

/**
 * A reusable button component for the Dingo app
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

    val contentColor = when (type) {
        ButtonType.FILLED -> MaterialTheme.colorScheme.onPrimary
        ButtonType.OUTLINED -> MaterialTheme.colorScheme.primary
        ButtonType.TEXT -> MaterialTheme.colorScheme.primary
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

    when (type) {
        ButtonType.FILLED -> {
            Button(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled && !isLoading,
                shape = RoundedCornerShape(ButtonCornerRadius),
                contentPadding = contentPadding,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                )
            ) {
                buttonContent()
            }
        }
        ButtonType.OUTLINED -> {
            OutlinedButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled && !isLoading,
                shape = RoundedCornerShape(ButtonCornerRadius),
                contentPadding = contentPadding,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                buttonContent()
            }
        }
        ButtonType.TEXT -> {
            TextButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = isEnabled && !isLoading,
                shape = RoundedCornerShape(ButtonCornerRadius),
                contentPadding = contentPadding,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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
    DingoTheme {
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
    DingoTheme {
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
    DingoTheme {
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
    DingoTheme {
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
