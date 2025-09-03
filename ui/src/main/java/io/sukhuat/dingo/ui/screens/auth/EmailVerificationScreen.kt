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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.components.DingoCard
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.components.FloatingLoadingDialog
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.theme.RusticGold

@Composable
fun EmailVerificationScreen(
    userEmail: String,
    onVerificationComplete: () -> Unit,
    onChangeEmail: () -> Unit,
    onSkipForNow: () -> Unit,
    viewModel: EnhancedAuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val currentLanguage = LocalAppLanguage.current

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onVerificationComplete()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DingoScaffold(
            title = stringResource(io.sukhuat.dingo.common.R.string.email_verification_title),
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
                            text = stringResource(io.sukhuat.dingo.common.R.string.email_verification_title),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        // Description
                        Text(
                            text = stringResource(io.sukhuat.dingo.common.R.string.email_verification_message),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Email address
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            color = RusticGold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Instruction
                        Text(
                            text = stringResource(io.sukhuat.dingo.common.R.string.check_email_instructions),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Resend button
                        ResendButton(
                            authState = authState,
                            onResendClick = { viewModel.resendEmailVerification() }
                        )

                        // Check verification button
                        Button(
                            onClick = { viewModel.checkEmailVerification() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = RusticGold)
                        ) {
                            Text(
                                text = stringResource(io.sukhuat.dingo.common.R.string.verify_email),
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Change email button
                        OutlinedButton(
                            onClick = onChangeEmail,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RusticGold
                            )
                        ) {
                            Text(stringResource(io.sukhuat.dingo.common.R.string.change_email))
                        }

                        // Skip for now button
                        OutlinedButton(
                            onClick = onSkipForNow,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(stringResource(io.sukhuat.dingo.common.R.string.skip_for_now))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Note about limited features
                        Text(
                            text = "Note: Some features will be limited until you verify your email",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Loading dialog
        if (authState is AuthUiState.Loading) {
            val loadingMessage = when (authState) {
                is AuthUiState.Loading.EmailVerification -> stringResource(io.sukhuat.dingo.common.R.string.verification_email_sent)
                else -> stringResource(io.sukhuat.dingo.common.R.string.loading)
            }

            FloatingLoadingDialog(
                isVisible = true,
                message = loadingMessage,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        }
    }
}

@Composable
private fun ResendButton(
    authState: AuthUiState,
    onResendClick: () -> Unit
) {
    when (authState) {
        is AuthUiState.EmailVerificationResent -> {
            Button(
                onClick = { }, // Disabled during countdown
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = stringResource(io.sukhuat.dingo.common.R.string.resend_in_seconds, authState.remainingCooldown),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        is AuthUiState.Loading.EmailVerification -> {
            Button(
                onClick = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = stringResource(io.sukhuat.dingo.common.R.string.loading),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> {
            Button(
                onClick = onResendClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RusticGold)
            ) {
                Text(
                    text = stringResource(io.sukhuat.dingo.common.R.string.resend_verification),
                    color = Color.White
                )
            }
        }
    }
}
