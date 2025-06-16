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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.components.ButtonType
import io.sukhuat.dingo.common.components.DingoButton
import io.sukhuat.dingo.common.components.DingoCard
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.components.DingoTextField
import io.sukhuat.dingo.common.components.FloatingLoadingDialog
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.localization.changeAppLanguage
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.utils.ToastHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

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
        label = stringResource(R.string.email),
        modifier = Modifier.fillMaxWidth(),
        isError = isError,
        errorText = errorText
    )

    Spacer(modifier = Modifier.height(16.dp))

    DingoTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = stringResource(R.string.password),
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
        text = if (isSignUp) stringResource(R.string.sign_up) else stringResource(R.string.sign_in),
        onClick = onSignInClick,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    DingoButton(
        text = if (isSignUp) stringResource(R.string.already_have_account) else stringResource(R.string.need_account),
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
            text = stringResource(R.string.sign_in_with_google),
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
    var isSignUp by remember { mutableStateOf(false) }

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
            title = stringResource(R.string.app_name),
            showTopBar = true,
            useGradientBackground = true,
            // Add language selection menu
            showUserMenu = true,
            isAuthenticated = false, // Not authenticated on auth screen
            currentLanguage = currentLanguage,
            onLanguageChange = { languageCode ->
                // Use the ViewModel to change the language
                viewModel.changeLanguage(languageCode)
            },
            onSettingsClick = {
                // No settings on auth screen
            }
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
                            text = if (isSignUp) stringResource(R.string.create_account) else stringResource(R.string.welcome_back),
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
                            text = stringResource(R.string.or),
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
