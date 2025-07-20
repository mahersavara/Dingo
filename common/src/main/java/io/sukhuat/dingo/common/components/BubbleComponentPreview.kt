package io.sukhuat.dingo.common.components

import android.net.Uri
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.theme.MountainSunriseTheme

@Preview(showBackground = true)
@Composable
fun BubbleComponentEditablePreview() {
    MountainSunriseTheme {
        Surface {
            // Create a goal that was created recently (editable)
            BubbleComponent(
                id = 100,
                text = "Read 30 minutes daily",
                imageResId = R.drawable.ic_goal_book,
                createdAt = System.currentTimeMillis() - 5 * 60 * 1000, // 5 minutes ago
                position = Pair(100f, 100f),
                onDismiss = {},
                onTextChange = {},
                onMediaUpload = { _, _ -> },
                onArchive = {},
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BubbleComponentReadOnlyPreview() {
    MountainSunriseTheme {
        Surface {
            // Create a goal that was created more than 30 minutes ago (read-only)
            BubbleComponent(
                id = 101,
                text = "Meditate for 10 minutes",
                imageResId = R.drawable.ic_goal_learn,
                createdAt = System.currentTimeMillis() - 40 * 60 * 1000, // 40 minutes ago
                position = Pair(100f, 100f),
                onDismiss = {},
                onTextChange = {},
                onMediaUpload = { _, _ -> },
                onArchive = {},
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BubbleComponentWithCustomImagePreview() {
    MountainSunriseTheme {
        Surface {
            // Create a goal with a custom image
            BubbleComponent(
                id = 102,
                text = "Travel to Japan",
                customImage = "content://media/external/images/1",
                createdAt = System.currentTimeMillis() - 15 * 60 * 1000, // 15 minutes ago
                position = Pair(100f, 100f),
                onDismiss = {},
                onTextChange = {},
                onMediaUpload = { _, _ -> },
                onArchive = {},
                onDelete = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BubbleComponentWithStickerPreview() {
    MountainSunriseTheme {
        Surface {
            // Create a goal with a sticker
            val stickerUri = Uri.parse("android.resource://io.sukhuat.dingo/" + R.drawable.ic_sticker_happy)
            BubbleComponent(
                id = 103,
                text = "Be happy today",
                customImage = stickerUri.toString(),
                createdAt = System.currentTimeMillis() - 5 * 60 * 1000, // 5 minutes ago
                position = Pair(100f, 100f),
                onDismiss = {},
                onTextChange = {},
                onMediaUpload = { _, _ -> },
                onArchive = {},
                onDelete = {}
            )
        }
    }
}
