package io.sukhuat.dingo.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.components.DingoCard
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.components.FloatingLoadingDialog
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.utils.ToastHelper

private const val TAG = "AuthScreen"

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToRegistration: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val languageCode by viewModel.languageCode.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage = LocalAppLanguage.current

    // Observe language changes from ViewModel
    LaunchedEffect(languageCode) {
        // This will trigger a recomposition when the language changes
        if (languageCode != null) {
            // Get the activity context
            val activity = context as? android.app.Activity
            if (activity != null) {
                // Force activity recreation with smoother animation
                val intent = activity.intent
                activity.finish()
                activity.startActivity(intent)

                // Use a smoother fade animation
                activity.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    viewModel.signInWithGoogle(token)
                } ?: run {
                    val errorMsg = "Failed to get ID token from Google Sign-In"
                    Log.e(TAG, errorMsg)
                    ToastHelper.showLong(context, errorMsg)
                }
            } catch (e: ApiException) {
                val errorMsg = "Google sign in failed: ${e.statusCode} - ${getGoogleSignInErrorMessage(e.statusCode)}"
                Log.e(TAG, errorMsg, e)
                ToastHelper.showLong(context, errorMsg)
            } catch (e: Exception) {
                val errorMsg = "Unexpected error during Google Sign-In: ${e.message}"
                Log.e(TAG, errorMsg, e)
                ToastHelper.showLong(context, errorMsg)
            }
        } else if (result.resultCode == Activity.RESULT_CANCELED) {
            Log.d(TAG, "Google Sign-In was canceled by user")
        } else {
            val errorMsg = "Google Sign-In failed with result code: ${result.resultCode}"
            Log.e(TAG, errorMsg)
            ToastHelper.showMedium(context, errorMsg)
        }
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onAuthSuccess()
        }
    }

    // Get the appropriate loading message based on the loading state
    val loadingMessage = when (authState) {
        is AuthUiState.Loading.GoogleSignIn -> stringResource(R.string.signing_in_google)
        is AuthUiState.Loading.EmailSignIn -> stringResource(R.string.signing_in)
        is AuthUiState.Loading.EmailSignUp -> stringResource(R.string.creating_account)
        is AuthUiState.Loading -> stringResource(R.string.loading) // For backward compatibility
        else -> stringResource(R.string.loading)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // First layer - main content
        DingoScaffold(
            title = "Welcome to Dingo",
            showTopBar = true,
            useGradientBackground = true,
            showUserMenu = false,
            isAuthenticated = false,
            userProfileImageUrl = null,
            currentLanguage = currentLanguage,
            onLanguageChange = { languageCode ->
                viewModel.changeLanguage(languageCode)
            },
            onSettingsClick = { }
        ) { paddingValues ->
            val scrollState = rememberScrollState()

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
                            text = "Welcome Back",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Sign in to continue your goal journey",
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
                                currentAuthState is AuthUiState.Error && currentAuthState.message.contains("email", ignoreCase = true)
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
                                currentAuthState is AuthUiState.Error && currentAuthState.message.contains("password", ignoreCase = true)
                            },
                            supportingText = {
                                val currentAuthState = authState
                                if (currentAuthState is AuthUiState.Error && currentAuthState.message.contains("password", ignoreCase = true)) {
                                    Text(currentAuthState.message, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        // Forgot Password button (reduced spacing)
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                onClick = onNavigateToForgotPassword,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(y = (-4).dp)
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    color = RusticGold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Sign In button
                        val canSignIn = email.isNotBlank() && password.isNotBlank()

                        Button(
                            onClick = {
                                viewModel.signIn(email, password)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = canSignIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RusticGold,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "Sign In",
                                color = if (canSignIn) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Create Account button
                        OutlinedButton(
                            onClick = onNavigateToRegistration,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = RusticGold
                            )
                        ) {
                            Text("Create Account")
                        }

                        Text(
                            text = "OR",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Google Sign-In button
                        OutlinedButton(
                            onClick = { viewModel.initiateGoogleSignIn(launcher) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Continue with Google")
                        }

                        // Error display
                        val currentAuthState = authState
                        if (currentAuthState is AuthUiState.Error && !currentAuthState.message.contains("email", ignoreCase = true) &&
                            !currentAuthState.message.contains("password", ignoreCase = true)
                        ) {
                            Text(
                                text = currentAuthState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // Second layer - loading dialog
        if (authState is AuthUiState.Loading) {
            FloatingLoadingDialog(
                isVisible = true,
                message = loadingMessage,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        }
    }
}

/**
 * Get a human-readable error message for Google Sign-In error codes
 */
private fun getGoogleSignInErrorMessage(statusCode: Int): String {
    return when (statusCode) {
        12500 -> "Play Services out of date or missing"
        12501 -> "User canceled the sign-in flow"
        12502 -> "Sign-in attempt failed"
        else -> "Unknown error code: $statusCode"
    }
}
