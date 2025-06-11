package io.sukhuat.dingo.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import io.sukhuat.dingo.common.components.ButtonType
import io.sukhuat.dingo.common.components.DingoButton
import io.sukhuat.dingo.common.components.DingoCard
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.components.DingoTextField
import io.sukhuat.dingo.common.components.FloatingLoadingDialog
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.utils.ToastHelper

private const val TAG = "AuthScreen"

@Composable
private fun EmailPasswordFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isError: Boolean = false,
    errorText: String? = null
) {
    DingoTextField(
        value = email,
        onValueChange = onEmailChange,
        label = "Email",
        modifier = Modifier.fillMaxWidth(),
        isError = isError,
        errorText = errorText
    )

    Spacer(modifier = Modifier.height(16.dp))

    DingoTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = "Password",
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        isError = isError
    )
}

@Composable
private fun AuthButtons(
    isSignUp: Boolean,
    onSignInClick: () -> Unit,
    onToggleAuthMode: () -> Unit
) {
    DingoButton(
        text = if (isSignUp) "Sign Up" else "Sign In",
        onClick = onSignInClick,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    DingoButton(
        text = if (isSignUp) "Already have an account? Sign In" else "Need an account? Sign Up",
        onClick = onToggleAuthMode,
        type = ButtonType.TEXT,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GoogleSignInButton(
    onGoogleSignInClick: () -> Unit
) {
    // Create a custom button with high contrast colors specifically for Google Sign-In
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Add a custom wrapper with padding to make the button more prominent
        DingoButton(
            text = "Sign in with Google",
            onClick = onGoogleSignInClick,
            type = ButtonType.OUTLINED,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            borderColor = RusticGold.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

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
    var isSignUp by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Success) {
            onAuthSuccess()
        }
    }

    // Get the appropriate loading message based on the loading state
    val loadingMessage = when (authState) {
        is AuthUiState.Loading.GoogleSignIn -> "Signing in with Google..."
        is AuthUiState.Loading.EmailSignIn -> "Signing in..."
        is AuthUiState.Loading.EmailSignUp -> "Creating account..."
        is AuthUiState.Loading -> "Loading..." // For backward compatibility
        else -> "Loading..."
    }

    // Show the loading dialog when in any loading state
    FloatingLoadingDialog(
        isVisible = authState is AuthUiState.Loading,
        message = loadingMessage,
        dismissOnBackPress = false,
        dismissOnClickOutside = false
    )

    DingoScaffold(
        title = "TRAVELER'S JOURNEY",
        showTopBar = true,
        useGradientBackground = true
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isSignUp) "Create Account" else "Welcome Back",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Check if there's an error to display
                    val isError = authState is AuthUiState.Error
                    val errorText = if (isError) (authState as AuthUiState.Error).message else null

                    EmailPasswordFields(
                        email = email,
                        onEmailChange = { email = it },
                        password = password,
                        onPasswordChange = { password = it },
                        isError = isError,
                        errorText = errorText
                    )

                    AuthButtons(
                        isSignUp = isSignUp,
                        onSignInClick = {
                            if (isSignUp) {
                                viewModel.signUp(email, password, password)
                            } else {
                                viewModel.signIn(email, password)
                            }
                        },
                        onToggleAuthMode = { isSignUp = !isSignUp }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = RusticGold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GoogleSignInButton(
                        onGoogleSignInClick = { viewModel.initiateGoogleSignIn(launcher) }
                    )
                }
            }
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
