package io.sukhuat.dingo.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.sukhuat.dingo.common.theme.CardCornerRadius
import io.sukhuat.dingo.common.theme.DingoTheme
import io.sukhuat.dingo.common.theme.ProgressIndicatorSize
import io.sukhuat.dingo.common.theme.SpaceMedium
import io.sukhuat.dingo.common.theme.SpaceSmall

/**
 * A floating loading dialog that can be displayed during operations like Google Sign-In
 * @param isVisible Whether the dialog is visible
 * @param message Message to display in the dialog
 * @param dismissOnBackPress Whether the dialog can be dismissed by pressing back
 * @param dismissOnClickOutside Whether the dialog can be dismissed by clicking outside
 */
@Composable
fun FloatingLoadingDialog(
    isVisible: Boolean,
    message: String = "Loading...",
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = dismissOnBackPress,
                dismissOnClickOutside = dismissOnClickOutside
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(CardCornerRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = SpaceMedium
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(SpaceMedium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Loading indicator
                        LoadingIndicator(
                            modifier = Modifier.size(ProgressIndicatorSize)
                        )

                        Spacer(modifier = Modifier.height(SpaceSmall))

                        // Message
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FloatingLoadingDialogPreview() {
    DingoTheme {
        FloatingLoadingDialog(
            isVisible = true,
            message = "Signing in with Google..."
        )
    }
}
