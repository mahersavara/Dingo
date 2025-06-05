package io.sukhuat.dingo.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

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
            color = MaterialTheme.colorScheme.error
        )
    }

    if (authState is AuthUiState.Loading) {
        Spacer(modifier = Modifier.height(8.dp))
        CircularProgressIndicator()
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
                    Toast.makeText(context, "Failed to get ID token", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(
                    context,
                    "Google sign in failed: ${e.statusCode}",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
