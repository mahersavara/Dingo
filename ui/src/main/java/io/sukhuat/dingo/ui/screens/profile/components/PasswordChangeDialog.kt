package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.MountainSunriseTheme

/**
 * Password change dialog with validation, strength indicator, and error handling
 */
@Composable
fun PasswordChangeDialog(
    onDismiss: () -> Unit,
    onPasswordChange: (currentPassword: String, newPassword: String, confirmPassword: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current password field
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = { Text("Current Password") },
                    visualTransformation = if (showPasswords) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showPasswords = !showPasswords }) {
                            Icon(
                                if (showPasswords) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = "Toggle password visibility"
                            )
                        }
                    },
                    enabled = !isLoading
                )

                // New password field with strength indicator
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("New Password") },
                    visualTransformation = if (showPasswords) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    supportingText = {
                        if (newPassword.isNotEmpty()) {
                            PasswordStrengthIndicator(password = newPassword)
                        }
                    }
                )

                // Confirm password field
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (showPasswords) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    isError = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword,
                    supportingText = {
                        if (newPassword != confirmPassword && confirmPassword.isNotEmpty()) {
                            Text(
                                text = "Passwords do not match",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )

                // Error message
                errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPasswordChange(currentPassword, newPassword, confirmPassword)
                },
                enabled = !isLoading && currentPassword.isNotEmpty() && newPassword.isNotEmpty() && newPassword == confirmPassword &&
                    isPasswordValid(newPassword),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Changing...")
                    }
                } else {
                    Text("Change Password")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Password strength indicator component
 */
@Composable
fun PasswordStrengthIndicator(password: String) {
    val strength = remember(password) { calculatePasswordStrength(password) }

    if (password.isNotEmpty()) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                LinearProgressIndicator(
                    progress = strength.score / 5f,
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = strength.color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strength.feedback,
                    style = MaterialTheme.typography.bodySmall,
                    color = strength.color,
                    fontWeight = FontWeight.Medium
                )
            }

            // Password requirements
            if (strength.score < 4) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Password should be at least 8 characters with uppercase, lowercase, numbers, and symbols",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

/**
 * Extension properties for PasswordStrength enum
 */
val PasswordStrength.color: Color
    @Composable get() = when (this) {
        PasswordStrength.WEAK -> Color(0xFFD32F2F)
        PasswordStrength.FAIR -> Color(0xFFFF6B00)
        PasswordStrength.GOOD -> Color(0xFFFFD700)
        PasswordStrength.STRONG -> Color(0xFF90EE90)
        PasswordStrength.VERY_STRONG -> Color(0xFF4CAF50)
    }

val PasswordStrength.feedback: String
    get() = when (this) {
        PasswordStrength.WEAK -> "Very weak"
        PasswordStrength.FAIR -> "Fair"
        PasswordStrength.GOOD -> "Good"
        PasswordStrength.STRONG -> "Strong"
        PasswordStrength.VERY_STRONG -> "Very Strong"
    }

/**
 * Calculate password strength score and feedback
 */
fun calculatePasswordStrength(password: String): PasswordStrength {
    var score = 0

    // Length check
    when {
        password.length >= 12 -> score += 2
        password.length >= 8 -> score += 1
    }

    // Character variety
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) score++

    return when (score) {
        0, 1 -> PasswordStrength.WEAK
        2, 3 -> PasswordStrength.FAIR
        4, 5 -> PasswordStrength.GOOD
        6 -> PasswordStrength.STRONG
        else -> PasswordStrength.VERY_STRONG
    }
}

/**
 * Check if password meets minimum requirements
 */
fun isPasswordValid(password: String): Boolean {
    return password.length >= 8 &&
        password.any { it.isUpperCase() } &&
        password.any { it.isLowerCase() } &&
        password.any { it.isDigit() }
}

@Preview
@Composable
fun PasswordChangeDialogPreview() {
    MountainSunriseTheme {
        PasswordChangeDialog(
            onDismiss = {},
            onPasswordChange = { _, _, _ -> },
            isLoading = false
        )
    }
}

@Preview
@Composable
fun PasswordChangeDialogLoadingPreview() {
    MountainSunriseTheme {
        PasswordChangeDialog(
            onDismiss = {},
            onPasswordChange = { _, _, _ -> },
            isLoading = true
        )
    }
}

@Preview
@Composable
fun PasswordChangeDialogErrorPreview() {
    MountainSunriseTheme {
        PasswordChangeDialog(
            onDismiss = {},
            onPasswordChange = { _, _, _ -> },
            isLoading = false,
            errorMessage = "Current password is incorrect. Please try again."
        )
    }
}
