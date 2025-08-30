package io.sukhuat.dingo.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.components.DingoCard
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.components.FloatingLoadingDialog
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.ui.screens.auth.components.PasswordStrengthIndicator

@Composable
fun EnhancedRegistrationScreen(
    onRegistrationSuccess: (String) -> Unit, // Pass email for verification screen
    onNavigateToLogin: () -> Unit,
    viewModel: EnhancedAuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val passwordStrengthState by viewModel.passwordStrengthState.collectAsState()
    val currentLanguage = LocalAppLanguage.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }
    var showPasswordMismatchError by remember { mutableStateOf(false) }

    // Real-time password strength validation
    LaunchedEffect(password) {
        if (password.isNotEmpty()) {
            viewModel.validatePasswordStrength(password)
        }
    }

    // Check password confirmation match
    LaunchedEffect(password, confirmPassword) {
        showPasswordMismatchError = confirmPassword.isNotEmpty() && password != confirmPassword
    }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.RegistrationSuccess) {
            onRegistrationSuccess(email)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DingoScaffold(
            title = "Create Account",
            showTopBar = true,
            useGradientBackground = true,
            showUserMenu = false,
            isAuthenticated = false,
            userProfileImageUrl = null,
            currentLanguage = currentLanguage,
            onLanguageChange = { viewModel.changeLanguage(it) },
            onSettingsClick = { }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Title
                        Text(
                            text = "Create Your Account",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Join Dingo to start achieving your goals!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
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
                            isError = run {
                                val currentAuthState = authState
                                currentAuthState is AuthUiState.Error && currentAuthState.errorType == AuthErrorType.VALIDATION &&
                                    currentAuthState.message.contains("email", ignoreCase = true)
                            },
                            supportingText = {
                                val currentAuthState = authState
                                if (currentAuthState is AuthUiState.Error && currentAuthState.message.contains("email", ignoreCase = true)) {
                                    Text(currentAuthState.message, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            isError = run {
                                val currentAuthState = authState
                                currentAuthState is AuthUiState.Error && currentAuthState.message.contains("password", ignoreCase = true) &&
                                    !currentAuthState.message.contains("confirm", ignoreCase = true)
                            }
                        )

                        // Password strength indicator
                        if (password.isNotEmpty()) {
                            PasswordStrengthIndicator(
                                passwordStrengthState = passwordStrengthState,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Confirm password field
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password
                            ),
                            isError = showPasswordMismatchError,
                            supportingText = if (showPasswordMismatchError) {
                                { Text("Passwords don't match", color = MaterialTheme.colorScheme.error) }
                            } else {
                                null
                            }
                        )

                        // Terms and conditions
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = acceptTerms,
                                onCheckedChange = { acceptTerms = it }
                            )
                            Text(
                                text = "I accept the Terms of Service and Privacy Policy",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Create Account button
                        val canCreateAccount = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() &&
                            password == confirmPassword &&
                            passwordStrengthState.passwordStrength.isValid &&
                            acceptTerms

                        Button(
                            onClick = {
                                viewModel.signUp(email, password, confirmPassword)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canCreateAccount,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RusticGold,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Create Account",
                                color = if (canCreateAccount) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Error display
                        val currentAuthState = authState
                        if (currentAuthState is AuthUiState.Error && currentAuthState.errorType == AuthErrorType.GENERAL) {
                            Text(
                                text = currentAuthState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Navigate to login
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Already have an account?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onNavigateToLogin) {
                                Text(
                                    text = "Sign In",
                                    color = RusticGold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Loading dialog
        if (authState is AuthUiState.Loading.EmailSignUp) {
            FloatingLoadingDialog(
                isVisible = true,
                message = "Creating your account...",
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        }
    }
}
