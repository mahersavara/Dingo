package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import io.sukhuat.dingo.common.components.GeneralItem
import io.sukhuat.dingo.common.components.NavigableGeneralItem
import io.sukhuat.dingo.common.components.TrailingContent
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.usecase.account.FormattedLoginRecord
import io.sukhuat.dingo.domain.usecase.account.LoginSummary

/**
 * Account Security component for managing password, login history, and account deletion
 */
@Composable
fun AccountSecurity(
    userProfile: UserProfile,
    uiState: AccountSecurityUiState,
    actions: AccountSecurityActions,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success message when password is changed
    LaunchedEffect(uiState.passwordChangeState.changePasswordSuccess) {
        if (uiState.passwordChangeState.changePasswordSuccess) {
            snackbarHostState.showSnackbar("Password changed successfully")
        }
    }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            actions.onDismissError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Security Overview Card
            SecurityOverviewCard(
                userProfile = userProfile,
                loginSummary = uiState.loginHistoryState.loginSummary
            )

            // Password Management Section
            if (userProfile.authProvider == AuthProvider.EMAIL_PASSWORD) {
                PasswordManagementSection(
                    passwordState = uiState.passwordChangeState,
                    actions = actions
                )
            }

            // Connected Accounts Section
            ConnectedAccountsSection(
                userProfile = userProfile
            )

            // Login History Section
            LoginHistorySection(
                historyState = uiState.loginHistoryState,
                actions = actions
            )

            // Account Deletion Section
            AccountDeletionSection(
                deletionState = uiState.accountDeletionState,
                actions = actions
            )
        }

        // Password Change Dialog
        if (uiState.passwordChangeState.showPasswordChangeDialog) {
            PasswordChangeDialog(
                passwordState = uiState.passwordChangeState,
                actions = actions
            )
        }

        // Account Deletion Dialog
        if (uiState.accountDeletionState.showDeletionDialog) {
            AccountDeletionDialog(
                deletionState = uiState.accountDeletionState,
                actions = actions
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun SecurityOverviewCard(
    userProfile: UserProfile,
    loginSummary: LoginSummary?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Security Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account Type
            SecurityInfoRow(
                label = "Account Type",
                value = when (userProfile.authProvider) {
                    AuthProvider.EMAIL_PASSWORD -> "Email & Password"
                    AuthProvider.GOOGLE -> "Google Account"
                    AuthProvider.ANONYMOUS -> "Anonymous"
                }
            )

            // Email Verification Status
            SecurityInfoRow(
                label = "Email Verified",
                value = if (userProfile.isEmailVerified) "Yes" else "No",
                valueColor = if (userProfile.isEmailVerified) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            // Login Summary
            loginSummary?.let { summary ->
                SecurityInfoRow(
                    label = "Last Login",
                    value = summary.lastLogin ?: "Never"
                )

                SecurityInfoRow(
                    label = "Total Logins",
                    value = summary.totalLogins.toString()
                )

                if (summary.hasSuspiciousActivity) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Suspicious activity detected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun PasswordManagementSection(
    passwordState: PasswordChangeState,
    actions: AccountSecurityActions
) {
    Column {
        Text(
            text = "Password Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        NavigableGeneralItem(
            title = "Change Password",
            description = "Update your account password",
            leadingIcon = Icons.Default.Lock,
            onClick = actions.onShowPasswordChangeDialog
        )
    }
}

@Composable
private fun ConnectedAccountsSection(
    userProfile: UserProfile
) {
    Column {
        Text(
            text = "Connected Accounts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (userProfile.authProvider) {
            AuthProvider.GOOGLE -> {
                GeneralItem(
                    title = "Google Account",
                    description = "Connected to ${userProfile.email}",
                    leadingIcon = Icons.Default.Security,
                    trailingContent = TrailingContent.Text(
                        text = "Connected",
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
            AuthProvider.EMAIL_PASSWORD -> {
                GeneralItem(
                    title = "Email Account",
                    description = userProfile.email,
                    leadingIcon = Icons.Default.Security,
                    trailingContent = TrailingContent.Text(
                        text = if (userProfile.isEmailVerified) "Verified" else "Unverified",
                        color = if (userProfile.isEmailVerified) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                )
            }
            AuthProvider.ANONYMOUS -> {
                GeneralItem(
                    title = "Anonymous Account",
                    description = "Limited functionality",
                    leadingIcon = Icons.Default.Security,
                    trailingContent = TrailingContent.Text(
                        text = "Anonymous",
                        color = MaterialTheme.colorScheme.outline
                    )
                )
            }
        }
    }
}

@Composable
private fun LoginHistorySection(
    historyState: LoginHistoryState,
    actions: AccountSecurityActions
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Login History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (historyState.isLoadingHistory) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        NavigableGeneralItem(
            title = "View Login History",
            description = "See recent login activity",
            leadingIcon = Icons.Default.History,
            onClick = actions.onLoadLoginHistory
        )

        // Show recent login history if loaded
        if (historyState.loginHistory.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))

            val itemsToShow = if (historyState.showFullHistory) {
                historyState.loginHistory
            } else {
                historyState.loginHistory.take(3)
            }

            itemsToShow.forEach { record ->
                LoginHistoryItem(record = record)
            }

            if (historyState.loginHistory.size > 3 && !historyState.showFullHistory) {
                TextButton(
                    onClick = actions.onToggleFullHistory,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Show All (${historyState.loginHistory.size} total)")
                }
            } else if (historyState.showFullHistory) {
                TextButton(
                    onClick = actions.onToggleFullHistory,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Show Less")
                }
            }
        }
    }
}

@Composable
private fun LoginHistoryItem(
    record: FormattedLoginRecord
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = record.formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = record.relativeTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = record.deviceInfo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = record.location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccountDeletionSection(
    deletionState: AccountDeletionState,
    actions: AccountSecurityActions
) {
    Column {
        Text(
            text = "Danger Zone",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delete Account",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Permanently delete your account and all associated data. This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = actions.onShowAccountDeletion,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Account")
                }
            }
        }
    }
}

@Composable
private fun PasswordChangeDialog(
    passwordState: PasswordChangeState,
    actions: AccountSecurityActions
) {
    AlertDialog(
        onDismissRequest = actions.onHidePasswordChangeDialog,
        title = {
            Text(
                text = "Change Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Current Password Field
                OutlinedTextField(
                    value = passwordState.currentPassword,
                    onValueChange = actions.onCurrentPasswordChange,
                    label = { Text("Current Password") },
                    visualTransformation = if (passwordState.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = actions.onTogglePasswordVisibility) {
                            Icon(
                                imageVector = if (passwordState.isPasswordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (passwordState.isPasswordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passwordState.currentPasswordError != null,
                    supportingText = passwordState.currentPasswordError?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // New Password Field
                OutlinedTextField(
                    value = passwordState.newPassword,
                    onValueChange = actions.onNewPasswordChange,
                    label = { Text("New Password") },
                    visualTransformation = if (passwordState.isNewPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = actions.onToggleNewPasswordVisibility) {
                            Icon(
                                imageVector = if (passwordState.isNewPasswordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (passwordState.isNewPasswordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passwordState.newPasswordError != null,
                    supportingText = passwordState.newPasswordError?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Password Strength Indicator
                if (passwordState.newPassword.isNotEmpty()) {
                    PasswordStrengthIndicator(
                        strength = passwordState.passwordStrength,
                        password = passwordState.newPassword
                    )
                }

                // Confirm Password Field
                OutlinedTextField(
                    value = passwordState.confirmPassword,
                    onValueChange = actions.onConfirmPasswordChange,
                    label = { Text("Confirm New Password") },
                    visualTransformation = if (passwordState.isConfirmPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        IconButton(onClick = actions.onToggleConfirmPasswordVisibility) {
                            Icon(
                                imageVector = if (passwordState.isConfirmPasswordVisible) {
                                    Icons.Default.VisibilityOff
                                } else {
                                    Icons.Default.Visibility
                                },
                                contentDescription = if (passwordState.isConfirmPasswordVisible) {
                                    "Hide password"
                                } else {
                                    "Show password"
                                }
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passwordState.confirmPasswordError != null,
                    supportingText = passwordState.confirmPasswordError?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = actions.onChangePassword,
                enabled = !passwordState.isChangingPassword &&
                    passwordState.currentPassword.isNotEmpty() &&
                    passwordState.newPassword.isNotEmpty() &&
                    passwordState.confirmPassword.isNotEmpty() &&
                    passwordState.currentPasswordError == null &&
                    passwordState.newPasswordError == null &&
                    passwordState.confirmPasswordError == null
            ) {
                if (passwordState.isChangingPassword) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Change Password")
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions.onHidePasswordChangeDialog,
                enabled = !passwordState.isChangingPassword
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PasswordStrengthIndicator(
    strength: PasswordStrength,
    password: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Password Strength:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = strength.label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = when (strength) {
                    PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                    PasswordStrength.FAIR -> MaterialTheme.colorScheme.error
                    PasswordStrength.GOOD -> Color(0xFFFF9800) // Orange
                    PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                    PasswordStrength.VERY_STRONG -> Color(0xFF4CAF50) // Green
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = strength.score / 5f,
            modifier = Modifier.fillMaxWidth(),
            color = when (strength) {
                PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                PasswordStrength.FAIR -> MaterialTheme.colorScheme.error
                PasswordStrength.GOOD -> Color(0xFFFF9800) // Orange
                PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                PasswordStrength.VERY_STRONG -> Color(0xFF4CAF50) // Green
            }
        )

        // Password requirements
        if (password.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            PasswordRequirements(password = password)
        }
    }
}

@Composable
private fun PasswordRequirements(password: String) {
    val requirements = listOf(
        "At least 8 characters" to (password.length >= 8),
        "Contains uppercase letter" to password.any { it.isUpperCase() },
        "Contains lowercase letter" to password.any { it.isLowerCase() },
        "Contains number" to password.any { it.isDigit() },
        "Contains special character" to password.any { !it.isLetterOrDigit() }
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        requirements.forEach { (requirement, met) ->
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (met) "✓" else "○",
                    color = if (met) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = requirement,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (met) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun AccountDeletionDialog(
    deletionState: AccountDeletionState,
    actions: AccountSecurityActions
) {
    AlertDialog(
        onDismissRequest = actions.onHideAccountDeletion,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Delete Account",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "This action will permanently delete your account and all associated data:",
                    style = MaterialTheme.typography.bodyMedium
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val deletionItems = listOf(
                        "• Profile information and settings",
                        "• All goals and progress data",
                        "• Achievement history",
                        "• Profile images and uploads",
                        "• Account preferences"
                    )

                    deletionItems.forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = "This action cannot be undone. To confirm, type DELETE below:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )

                OutlinedTextField(
                    value = deletionState.confirmationText,
                    onValueChange = actions.onConfirmationTextChange,
                    label = { Text("Type DELETE to confirm") },
                    isError = deletionState.deletionError != null,
                    supportingText = deletionState.deletionError?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = actions.onDeleteAccount,
                enabled = !deletionState.isDeletingAccount && deletionState.confirmationText == "DELETE",
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                if (deletionState.isDeletingAccount) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Delete Account")
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions.onHideAccountDeletion,
                enabled = !deletionState.isDeletingAccount
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun AccountSecurityPreview() {
    MountainSunriseTheme {
        val sampleProfile = UserProfile(
            userId = "123",
            displayName = "John Doe",
            email = "john.doe@example.com",
            profileImageUrl = null,
            joinDate = System.currentTimeMillis() - (6 * 30 * 24 * 60 * 60 * 1000L),
            isEmailVerified = true,
            authProvider = AuthProvider.EMAIL_PASSWORD,
            lastLoginDate = System.currentTimeMillis() - (2 * 60 * 60 * 1000L)
        )

        val sampleLoginSummary = LoginSummary(
            totalLogins = 45,
            uniqueDevices = 3,
            uniqueLocations = 2,
            lastLogin = "2 hours ago",
            hasSuspiciousActivity = false,
            recentLoginsCount = 5
        )

        val sampleUiState = AccountSecurityUiState(
            loginHistoryState = LoginHistoryState(
                loginSummary = sampleLoginSummary,
                loginHistory = listOf(
                    FormattedLoginRecord(
                        timestamp = System.currentTimeMillis(),
                        formattedDate = "Dec 15, 2023",
                        formattedTime = "14:30",
                        relativeTime = "2 hours ago",
                        deviceInfo = "iPhone 14 Pro",
                        ipAddress = "192.168.1.1",
                        location = "San Francisco, CA"
                    )
                )
            )
        )

        val sampleActions = AccountSecurityActions(
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleNewPasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onShowPasswordChangeDialog = {},
            onHidePasswordChangeDialog = {},
            onChangePassword = {},
            onLoadLoginHistory = {},
            onToggleFullHistory = {},
            onShowAccountDeletion = {},
            onHideAccountDeletion = {},
            onConfirmationTextChange = {},
            onDeleteAccount = {},
            onDismissError = {}
        )

        AccountSecurity(
            userProfile = sampleProfile,
            uiState = sampleUiState,
            actions = sampleActions
        )
    }
}
