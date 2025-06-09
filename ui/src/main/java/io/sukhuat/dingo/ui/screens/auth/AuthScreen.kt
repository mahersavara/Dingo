package io.sukhuat.dingo.ui.screens.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import io.sukhuat.dingo.common.components.FloatingLoadingDialog
import io.sukhuat.dingo.common.utils.ToastHelper

private const val TAG = "AuthScreen"

@Composable
private fun EmailPasswordFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit
) {
    TextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    TextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AuthButtons(
    isSignUp: Boolean,
    onSignInClick: () -> Unit,
    onToggleAuthMode: () -> Unit
) {
    Button(
        onClick = onSignInClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (isSignUp) "Sign Up" else "Sign In")
    }

    TextButton(
        onClick = onToggleAuthMode,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(if (isSignUp) "Already have an account? Sign In" else "Need an account? Sign Up")
    }
}

@Composable
private fun GoogleSignInButton(
    onGoogleSignInClick: () -> Unit
) {
    Button(
        onClick = onGoogleSignInClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Sign in with Google")
    }
}

@Composable
private fun AuthStateIndicators(
    authState: AuthUiState
) {
    if (authState is AuthUiState.Error) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = (authState as AuthUiState.Error).message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmailPasswordFields(
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        GoogleSignInButton(
            onGoogleSignInClick = { viewModel.initiateGoogleSignIn(launcher) }
        )

        AuthStateIndicators(authState = authState)
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
