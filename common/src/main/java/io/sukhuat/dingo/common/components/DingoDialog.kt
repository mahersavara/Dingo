package io.sukhuat.dingo.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import io.sukhuat.dingo.common.theme.ButtonCornerRadius
import io.sukhuat.dingo.common.theme.CardCornerRadius
import io.sukhuat.dingo.common.theme.DingoTheme
import io.sukhuat.dingo.common.theme.SpaceLarge
import io.sukhuat.dingo.common.theme.SpaceMedium
import io.sukhuat.dingo.common.theme.SpaceSmall

/**
 * A reusable dialog component for the Dingo app
 * @param title The title of the dialog
 * @param onDismissRequest Called when the user dismisses the dialog
 * @param confirmButton Text for the confirm button (null to hide)
 * @param dismissButton Text for the dismiss button (null to hide)
 * @param neutralButton Text for the neutral button (null to hide)
 * @param onConfirmClick Called when the confirm button is clicked
 * @param onDismissClick Called when the dismiss button is clicked
 * @param onNeutralClick Called when the neutral button is clicked
 * @param isLoading Whether to show a loading indicator in the dialog
 * @param content The content of the dialog
 */
@Composable
fun DingoDialog(
    title: String,
    onDismissRequest: () -> Unit,
    confirmButton: String? = null,
    dismissButton: String? = null,
    neutralButton: String? = null,
    onConfirmClick: () -> Unit = {},
    onDismissClick: () -> Unit = onDismissRequest,
    onNeutralClick: () -> Unit = {},
    isLoading: Boolean = false,
    content: @Composable () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            shape = RoundedCornerShape(CardCornerRadius),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = SpaceSmall
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SpaceLarge)
            ) {
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(SpaceMedium))

                // Content
                Box(modifier = Modifier.fillMaxWidth()) {
                    content()

                    // Show loading indicator if needed
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SpaceLarge))

                // Buttons
                val buttonCount = listOf(confirmButton, dismissButton, neutralButton).count { it != null }

                when {
                    // No buttons
                    buttonCount == 0 -> { /* No buttons to show */ }

                    // Single button (spans full width)
                    buttonCount == 1 -> {
                        confirmButton?.let {
                            Button(
                                onClick = onConfirmClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ButtonCornerRadius),
                                enabled = !isLoading
                            ) {
                                Text(text = it)
                            }
                        } ?: dismissButton?.let {
                            OutlinedButton(
                                onClick = onDismissClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ButtonCornerRadius),
                                enabled = !isLoading
                            ) {
                                Text(text = it)
                            }
                        } ?: neutralButton?.let {
                            Button(
                                onClick = onNeutralClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(ButtonCornerRadius),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                enabled = !isLoading
                            ) {
                                Text(text = it)
                            }
                        }
                    }

                    // Two or three buttons (side by side)
                    else -> {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Dismiss button
                            dismissButton?.let {
                                OutlinedButton(
                                    onClick = onDismissClick,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(ButtonCornerRadius),
                                    enabled = !isLoading
                                ) {
                                    Text(
                                        text = it,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Add spacing between buttons
                            if (dismissButton != null && (confirmButton != null || neutralButton != null)) {
                                Spacer(modifier = Modifier.width(SpaceSmall))
                            }

                            // Neutral button
                            neutralButton?.let {
                                Button(
                                    onClick = onNeutralClick,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(ButtonCornerRadius),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ),
                                    enabled = !isLoading
                                ) {
                                    Text(
                                        text = it,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Add spacing between buttons
                            if (neutralButton != null && confirmButton != null) {
                                Spacer(modifier = Modifier.width(SpaceSmall))
                            }

                            // Confirm button
                            confirmButton?.let {
                                Button(
                                    onClick = onConfirmClick,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(ButtonCornerRadius),
                                    enabled = !isLoading
                                ) {
                                    Text(
                                        text = it,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoDialogPreview() {
    DingoTheme {
        DingoDialog(
            title = "Dialog Title",
            onDismissRequest = {},
            confirmButton = "Confirm",
            dismissButton = "Cancel",
            content = {
                Text(
                    text = "This is the dialog content. It can contain any composable content.",
                    modifier = Modifier.padding(vertical = SpaceMedium)
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DingoDialogLoadingPreview() {
    DingoTheme {
        DingoDialog(
            title = "Loading Dialog",
            onDismissRequest = {},
            confirmButton = "Confirm",
            dismissButton = "Cancel",
            isLoading = true,
            content = {
                Text(
                    text = "Loading data...",
                    modifier = Modifier.padding(vertical = SpaceMedium)
                )
            }
        )
    }
}
