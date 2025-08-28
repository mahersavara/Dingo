package io.sukhuat.dingo.ui.screens.auth

import androidx.compose.ui.graphics.Color
import io.sukhuat.dingo.domain.repository.PasswordStrength

/**
 * UI state for password strength display
 */
data class PasswordStrengthUiState(
    val passwordStrength: PasswordStrength = PasswordStrength(),
    val isVisible: Boolean = false,
    val strengthColor: Color = Color.Gray,
    val strengthText: String = "",
    val progress: Float = 0f
)

/**
 * Extension functions to convert domain models to UI state
 */
fun PasswordStrength.toUiState(): PasswordStrengthUiState {
    val strengthText = when (score) {
        0 -> "Very Weak"
        1 -> "Weak"
        2 -> "Fair"
        3 -> "Strong"
        4 -> "Very Strong"
        else -> "Unknown"
    }

    val strengthColor = when (score) {
        0 -> Color(0xFFE53E3E) // Red
        1 -> Color(0xFFFF9500) // Orange
        2 -> Color(0xFFFFD600) // Amber
        3 -> Color(0xFF38A169) // Green
        4 -> Color(0xFF2F855A) // Dark Green
        else -> Color.Gray
    }

    val progress = (score / 4f).coerceIn(0f, 1f)

    return PasswordStrengthUiState(
        passwordStrength = this,
        isVisible = true,
        strengthColor = strengthColor,
        strengthText = strengthText,
        progress = progress
    )
}
