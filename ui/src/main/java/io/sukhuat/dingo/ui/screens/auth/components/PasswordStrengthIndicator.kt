package io.sukhuat.dingo.ui.screens.auth.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.domain.repository.PasswordRequirements
import io.sukhuat.dingo.ui.screens.auth.PasswordStrengthUiState

@Composable
fun PasswordStrengthIndicator(
    passwordStrengthState: PasswordStrengthUiState,
    modifier: Modifier = Modifier,
    showRequirements: Boolean = true
) {
    if (!passwordStrengthState.isVisible) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Strength bar and label
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(io.sukhuat.dingo.common.R.string.password_strength),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = passwordStrengthState.strengthText,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = passwordStrengthState.strengthColor
            )
        }

        // Animated progress bar
        PasswordStrengthBar(
            progress = passwordStrengthState.progress,
            color = passwordStrengthState.strengthColor
        )

        // Requirements checklist
        if (showRequirements) {
            PasswordRequirementsList(
                requirements = passwordStrengthState.passwordStrength.requirements,
                feedback = passwordStrengthState.passwordStrength.feedback
            )
        }
    }
}

@Composable
private fun PasswordStrengthBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(300),
        label = "password_strength_progress"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
    ) {
        drawPasswordStrengthBar(
            progress = animatedProgress,
            color = color,
            backgroundColor = Color.Gray.copy(alpha = 0.3f)
        )
    }
}

@Composable
private fun PasswordRequirementsList(
    requirements: PasswordRequirements,
    feedback: List<String>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RequirementItem(
            text = stringResource(io.sukhuat.dingo.common.R.string.password_req_length),
            isMet = requirements.minLength
        )
        RequirementItem(
            text = stringResource(io.sukhuat.dingo.common.R.string.password_req_uppercase),
            isMet = requirements.hasUppercase
        )
        RequirementItem(
            text = stringResource(io.sukhuat.dingo.common.R.string.password_req_lowercase),
            isMet = requirements.hasLowercase
        )
        RequirementItem(
            text = stringResource(io.sukhuat.dingo.common.R.string.password_req_number),
            isMet = requirements.hasNumber
        )
        RequirementItem(
            text = stringResource(io.sukhuat.dingo.common.R.string.password_req_special),
            isMet = requirements.hasSpecialChar
        )
        RequirementItem(
            text = stringResource(io.sukhuat.dingo.common.R.string.password_req_not_common),
            isMet = requirements.noCommonPatterns
        )
    }
}

@Composable
private fun RequirementItem(
    text: String,
    isMet: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (isMet) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (isMet) "Requirement met" else "Requirement not met",
            modifier = Modifier.size(16.dp),
            tint = if (isMet) Color(0xFF4CAF50) else Color(0xFFF44336)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (isMet) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

private fun DrawScope.drawPasswordStrengthBar(
    progress: Float,
    color: Color,
    backgroundColor: Color
) {
    val barHeight = size.height
    val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)

    // Draw background
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset.Zero,
        size = Size(size.width, barHeight),
        cornerRadius = cornerRadius
    )

    // Draw progress
    if (progress > 0f) {
        val progressWidth = size.width * progress.coerceIn(0f, 1f)
        drawRoundRect(
            color = color,
            topLeft = Offset.Zero,
            size = Size(progressWidth, barHeight),
            cornerRadius = cornerRadius
        )
    }
}
