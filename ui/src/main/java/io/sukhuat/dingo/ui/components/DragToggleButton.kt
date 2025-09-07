package io.sukhuat.dingo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.DeepIndigo
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.theme.White

/**
 * Drag Mode Toggle Button with Mountain Sunrise theme
 * Allows users to toggle drag mode for goal reordering
 */
@Composable
fun DragToggleButton(
    isDragModeActive: Boolean,
    isSaving: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    // Animated properties
    val scale by animateFloatAsState(
        targetValue = if (isDragModeActive) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "button_scale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSaving -> MaterialTheme.colorScheme.secondary
            isDragModeActive -> RusticGold
            else -> RusticGold.copy(alpha = 0.6f)
        },
        animationSpec = spring(dampingRatio = 0.8f),
        label = "background_color"
    )

    val contentDescription = when {
        isSaving -> "Saving positions..."
        isDragModeActive -> "Exit edit mode"
        else -> "Enter edit mode"
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .scale(scale)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                enabled = !isSaving,
                role = Role.Button,
                onClickLabel = contentDescription
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            }
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        when {
            isSaving -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = White
                )
            }
            isDragModeActive -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Save Status Indicator Component
 * Shows drag mode save status with Mountain Sunrise theme
 */
@Composable
fun DragModeSaveIndicator(
    isSaving: Boolean,
    lastSyncTime: Long?,
    error: String?,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            error != null -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
            isSaving -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
            lastSyncTime != null -> RusticGold.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        label = "indicator_background"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            error != null -> MaterialTheme.colorScheme.error
            isSaving -> MaterialTheme.colorScheme.secondary
            lastSyncTime != null -> RusticGold
            else -> DeepIndigo
        },
        label = "indicator_text_color"
    )

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            error != null -> {
                androidx.compose.material3.Text(
                    text = "Save failed",
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            isSaving -> {
                androidx.compose.material3.Text(
                    text = "Saving...",
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            lastSyncTime != null -> {
                androidx.compose.material3.Text(
                    text = "Saved",
                    color = textColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
