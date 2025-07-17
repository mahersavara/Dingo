package io.sukhuat.dingo.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.sukhuat.dingo.common.theme.MountainSunriseTheme

/**
 * Reusable general item component for settings and lists
 * Supports various configurations: toggle, arrow, radio, custom trailing content
 */
@Composable
fun GeneralItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: Any? = null, // Can be ImageVector, Painter, or Int (drawable resource)
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    trailingContent: TrailingContent = TrailingContent.None,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null && enabled) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Row(
        modifier = clickableModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Leading icon
        leadingIcon?.let { icon ->
            when (icon) {
                is ImageVector -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (enabled) leadingIconTint else leadingIconTint.copy(alpha = 0.38f)
                    )
                }
                is Painter -> {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (enabled) leadingIconTint else leadingIconTint.copy(alpha = 0.38f)
                    )
                }
                is Int -> {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (enabled) leadingIconTint else leadingIconTint.copy(alpha = 0.38f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
        }

        // Title and description
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            description?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Trailing content
        when (trailingContent) {
            is TrailingContent.Switch -> {
                Switch(
                    checked = trailingContent.checked,
                    onCheckedChange = trailingContent.onCheckedChange,
                    enabled = enabled
                )
            }
            is TrailingContent.RadioButton -> {
                RadioButton(
                    selected = trailingContent.selected,
                    onClick = trailingContent.onClick,
                    enabled = enabled
                )
            }
            is TrailingContent.Checkbox -> {
                Checkbox(
                    checked = trailingContent.checked,
                    onCheckedChange = trailingContent.onCheckedChange,
                    enabled = enabled
                )
            }
            is TrailingContent.Arrow -> {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                )
            }
            is TrailingContent.Text -> {
                Text(
                    text = trailingContent.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) {
                        trailingContent.color ?: MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        (trailingContent.color ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.38f)
                    }
                )
            }
            is TrailingContent.Badge -> {
                Box(
                    modifier = Modifier
                        .background(
                            color = trailingContent.backgroundColor ?: MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = trailingContent.text,
                        style = MaterialTheme.typography.labelSmall,
                        color = trailingContent.contentColor ?: MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            is TrailingContent.Icon -> {
                Icon(
                    imageVector = trailingContent.icon,
                    contentDescription = null,
                    tint = if (enabled) {
                        trailingContent.tint ?: MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        (trailingContent.tint ?: MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.38f)
                    },
                    modifier = if (trailingContent.onClick != null && enabled) {
                        Modifier
                            .clip(CircleShape)
                            .clickable { trailingContent.onClick.invoke() }
                            .padding(4.dp)
                    } else {
                        Modifier
                    }
                )
            }
            is TrailingContent.Custom -> {
                trailingContent.content()
            }
            TrailingContent.None -> {
                // No trailing content
            }
        }
    }
}

/**
 * Sealed class representing different types of trailing content
 */
sealed class TrailingContent {
    object None : TrailingContent()
    object Arrow : TrailingContent()

    data class Switch(
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : TrailingContent()

    data class RadioButton(
        val selected: Boolean,
        val onClick: (() -> Unit)?
    ) : TrailingContent()

    data class Checkbox(
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : TrailingContent()

    data class Text(
        val text: String,
        val color: Color? = null
    ) : TrailingContent()

    data class Badge(
        val text: String,
        val backgroundColor: Color? = null,
        val contentColor: Color? = null
    ) : TrailingContent()

    data class Icon(
        val icon: ImageVector,
        val tint: Color? = null,
        val onClick: (() -> Unit)? = null
    ) : TrailingContent()

    data class Custom(
        val content: @Composable () -> Unit
    ) : TrailingContent()
}

/**
 * Convenience function for creating a selectable item (like radio button behavior)
 */
@Composable
fun SelectableGeneralItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: Any? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {
    GeneralItem(
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        leadingIconTint = leadingIconTint,
        trailingContent = TrailingContent.RadioButton(
            selected = selected,
            onClick = if (enabled) onClick else null
        ),
        enabled = enabled,
        modifier = modifier.selectable(
            selected = selected,
            onClick = onClick,
            enabled = enabled
        )
    )
}

/**
 * Convenience function for creating a toggle item (switch behavior)
 */
@Composable
fun ToggleGeneralItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: Any? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {
    GeneralItem(
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        leadingIconTint = leadingIconTint,
        trailingContent = TrailingContent.Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        ),
        enabled = enabled,
        modifier = modifier
    )
}

/**
 * Convenience function for creating a navigable item (arrow behavior)
 */
@Composable
fun NavigableGeneralItem(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    leadingIcon: Any? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {
    GeneralItem(
        title = title,
        description = description,
        leadingIcon = leadingIcon,
        leadingIconTint = leadingIconTint,
        trailingContent = TrailingContent.Arrow,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GeneralItemPreview() {
    MountainSunriseTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Switch example
            ToggleGeneralItem(
                title = "Sound Effects",
                description = "Play sounds for interactions",
                leadingIcon = Icons.Default.KeyboardArrowRight,
                checked = true,
                onCheckedChange = {}
            )

            // Arrow example
            NavigableGeneralItem(
                title = "Language",
                description = "Current: English",
                leadingIcon = Icons.Default.KeyboardArrowRight,
                onClick = {}
            )

            // Badge example
            GeneralItem(
                title = "App Version",
                description = "Latest version available",
                leadingIcon = Icons.Default.KeyboardArrowRight,
                trailingContent = TrailingContent.Badge(
                    text = "NEW",
                    backgroundColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            )

            // Radio button example
            SelectableGeneralItem(
                title = "Dark Mode",
                description = "Use dark theme",
                leadingIcon = Icons.Default.KeyboardArrowRight,
                selected = false,
                onClick = {}
            )
        }
    }
}
