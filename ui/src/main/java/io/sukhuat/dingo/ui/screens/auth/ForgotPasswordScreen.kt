package io.sukhuat.dingo.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.components.DingoCard
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.components.FloatingLoadingDialog
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.theme.RusticGold

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit,
    onResetEmailSent: (String) -> Unit,
    viewModel: EnhancedAuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val currentLanguage = LocalAppLanguage.current

    var email by remember { mutableStateOf("") }
    var isEmailSent by remember { mutableStateOf(false) }

    // Handle reset email sent state
    if (authState is AuthUiState.PasswordResetSent && !isEmailSent) {
        isEmailSent = true
        onResetEmailSent(email)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DingoScaffold(
            title = if (isEmailSent) "Reset Email Sent" else "Reset Password",
            showTopBar = true,
            useGradientBackground = true,
            showUserMenu = false,
            isAuthenticated = false,
            userProfile = null,
            currentLanguage = currentLanguage,
            onLanguageChange = { viewModel.changeLanguage(it) },
            onSettingsClick = { }
        ) { paddingValues ->
            val scrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
            ) {
                DingoCard(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.Center)
                        .padding(16.dp),
                    accentBorder = true,
                    useGradientBackground = false
                ) {
                    if (isEmailSent || authState is AuthUiState.PasswordResetSent) {
                        EmailSentContent(
                            email = email,
                            onBackToLogin = onBackToLogin,
                            onResendClick = { viewModel.sendPasswordReset(email) }
                        )
                    } else {
                        ResetPasswordContent(
                            email = email,
                            onEmailChange = { email = it },
                            onSendResetClick = { viewModel.sendPasswordReset(email) },
                            onBackToLogin = onBackToLogin,
                            isError = authState is AuthUiState.Error,
                            errorMessage = run {
                                val currentAuthState = authState
                                if (currentAuthState is AuthUiState.Error) currentAuthState.message else null
                            }
                        )
                    }
                }
            }
        }

        // Loading dialog
        if (authState is AuthUiState.Loading.PasswordReset) {
            FloatingLoadingDialog(
                isVisible = true,
                message = "Sending reset email...",
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        }
    }
}

@Composable
private fun ResetPasswordContent(
    email: String,
    onEmailChange: (String) -> Unit,
    onSendResetClick: () -> Unit,
    onBackToLogin: () -> Unit,
    isError: Boolean,
    errorMessage: String?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Lock icon
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = RusticGold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Description
        Text(
            text = "Enter your email address and we'll send you a link to reset your password",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email input
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email Address") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            isError = isError,
            supportingText = if (isError && errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else {
                null
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Send reset link button
        Button(
            onClick = onSendResetClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = email.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = RusticGold)
        ) {
            Text(
                text = "Send Reset Link",
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Back to login
        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "← Back to Login",
                color = RusticGold
            )
        }
    }
}

@Composable
private fun EmailSentContent(
    email: String,
    onBackToLogin: () -> Unit,
    onResendClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Email icon
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = RusticGold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "Check Your Email",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        // Description
        Text(
            text = "We sent a password reset link to:",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Email address
        Text(
            text = email,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = RusticGold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Instructions
        Text(
            text = "Click the link in the email to reset your password. The link will expire in 1 hour.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Open email app button (placeholder - would need deep linking)
        Button(
            onClick = { /* TODO: Open default email app */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RusticGold)
        ) {
            Text(
                text = "Open Email App",
                color = Color.White
            )
        }

        // Resend link button
        OutlinedButton(
            onClick = onResendClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = RusticGold
            )
        ) {
            Text("Resend Link")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Back to login
        TextButton(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "← Back to Login",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
