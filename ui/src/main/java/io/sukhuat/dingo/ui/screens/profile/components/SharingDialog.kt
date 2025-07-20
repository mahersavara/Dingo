package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.ShareableContent
import io.sukhuat.dingo.domain.model.SocialPlatform

/**
 * Dialog for sharing content with platform selection
 */
@Composable
fun SharingDialog(
    dialogState: SharingDialogState,
    onSelectPlatform: (SocialPlatform) -> Unit,
    onConfirmShare: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!dialogState.isVisible) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dialogState.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Preview
                if (dialogState.content != null) {
                    ContentPreview(
                        content = dialogState.content,
                        selectedPlatform = dialogState.selectedPlatform
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Platform Selection
                Text(
                    text = "Choose Platform",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dialogState.availablePlatforms) { platform ->
                        PlatformButton(
                            platform = platform,
                            isSelected = platform == dialogState.selectedPlatform,
                            onClick = { onSelectPlatform(platform) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onConfirmShare,
                        enabled = dialogState.selectedPlatform != null && !dialogState.isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (dialogState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Share")
                    }
                }
            }
        }
    }
}

/**
 * Content preview section
 */
@Composable
private fun ContentPreview(
    content: ShareableContent,
    selectedPlatform: SocialPlatform?
) {
    var showFullText by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Row {
                    IconButton(
                        onClick = { /* Copy to clipboard */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy text",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main text
            val displayText = if (showFullText || content.text.length <= 150) {
                content.text
            } else {
                content.text.take(150) + "..."
            }

            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (content.text.length > 150) {
                TextButton(
                    onClick = { showFullText = !showFullText },
                    modifier = Modifier.padding(0.dp)
                ) {
                    Text(
                        text = if (showFullText) "Show less" else "Show more",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Hashtags
            if (content.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = content.hashtags.joinToString(" "),
                    style = MaterialTheme.typography.bodySmall,
                    color = RusticGold
                )
            }

            // Platform-specific note
            if (selectedPlatform != null) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Content will be optimized for ${selectedPlatform.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Platform selection button
 */
@Composable
private fun PlatformButton(
    platform: SocialPlatform,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val platformInfo = getPlatformInfo(platform)

    Surface(
        modifier = Modifier
            .size(64.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = if (isSelected) {
            RusticGold.copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, RusticGold)
        } else {
            null
        }
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = platformInfo.iconRes),
                    contentDescription = platformInfo.name,
                    modifier = Modifier.size(24.dp),
                    tint = if (isSelected) RusticGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = platformInfo.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) RusticGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .background(RusticGold, CircleShape)
                        .padding(2.dp),
                    tint = Color.White
                )
            }
        }
    }
}

/**
 * Platform information data class
 */
private data class PlatformInfo(
    val name: String,
    val iconRes: Int
)

/**
 * Get platform display information
 */
private fun getPlatformInfo(platform: SocialPlatform): PlatformInfo {
    return when (platform) {
        SocialPlatform.TWITTER -> PlatformInfo("Twitter", android.R.drawable.ic_menu_share)
        SocialPlatform.FACEBOOK -> PlatformInfo("Facebook", android.R.drawable.ic_menu_share)
        SocialPlatform.INSTAGRAM -> PlatformInfo("Instagram", android.R.drawable.ic_menu_share)
        SocialPlatform.LINKEDIN -> PlatformInfo("LinkedIn", android.R.drawable.ic_menu_share)
        SocialPlatform.WHATSAPP -> PlatformInfo("WhatsApp", android.R.drawable.ic_menu_share)
        SocialPlatform.GENERIC -> PlatformInfo("More", android.R.drawable.ic_menu_share)
    }
}

@Preview(showBackground = true)
@Composable
fun SharingDialogPreview() {
    MountainSunriseTheme {
        SharingDialog(
            dialogState = SharingDialogState(
                isVisible = true,
                title = "Share Achievement",
                content = ShareableContent(
                    text = "🎉 Just unlocked the 'First Goal' achievement! Created your first goal and started the journey towards personal growth.",
                    hashtags = listOf("#GoalSetting", "#Achievement", "#PersonalGrowth", "#DingoApp"),
                    achievementTitle = "First Goal",
                    achievementDescription = "Created your first goal"
                ),
                availablePlatforms = listOf(
                    SocialPlatform.TWITTER,
                    SocialPlatform.FACEBOOK,
                    SocialPlatform.INSTAGRAM,
                    SocialPlatform.LINKEDIN,
                    SocialPlatform.WHATSAPP,
                    SocialPlatform.GENERIC
                ),
                selectedPlatform = SocialPlatform.TWITTER
            ),
            onSelectPlatform = {},
            onConfirmShare = {},
            onDismiss = {}
        )
    }
}
