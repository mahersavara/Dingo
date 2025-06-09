package io.sukhuat.dingo.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.common.utils.ToastHelper
import io.sukhuat.dingo.data.auth.GoogleAuthService
import io.sukhuat.dingo.domain.repository.AuthResult
import io.sukhuat.dingo.usecases.auth.SignInUseCase
import io.sukhuat.dingo.usecases.auth.SignUpWithEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AuthViewModel"

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpWithEmailUseCase,
    private val googleAuthService: GoogleAuthService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val authState: StateFlow<AuthUiState> = _authState

    fun initiateGoogleSignIn(launcher: ActivityResultLauncher<Intent>) {
        try {
            if (!googleAuthService.isInitialized()) {
                val errorMsg = "Google Sign-In is not properly initialized"
                _authState.value = AuthUiState.Error(errorMsg)
                Log.e(TAG, errorMsg)
                ToastHelper.showLong(context, errorMsg)
                return
            }

            _authState.value = AuthUiState.Loading.GoogleSignIn
            launcher.launch(googleAuthService.getSignInIntent())
        } catch (e: Exception) {
            val errorMsg = "Failed to start Google Sign-In: ${e.message ?: "Unknown error"}"
            Log.e(TAG, errorMsg, e)
            _authState.value = AuthUiState.Error(errorMsg)
            ToastHelper.showLong(context, errorMsg)
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading.GoogleSignIn

            signInUseCase.signInWithGoogle(idToken)
                .onEach { result ->
                    _authState.value = when (result) {
                        is AuthResult.Success -> AuthUiState.Success
                        is AuthResult.Error -> {
                            val errorMsg = result.message
                            Log.e(TAG, "Google sign-in failed: $errorMsg")
                            ToastHelper.showMedium(context, errorMsg)
                            AuthUiState.Error(errorMsg)
                        }
                        is AuthResult.Loading -> AuthUiState.Loading.GoogleSignIn
                    }
                }
                .launchIn(this)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading.EmailSignIn

            signInUseCase(email, password)
                .onEach { result ->
                    _authState.value = when (result) {
                        is AuthResult.Success -> AuthUiState.Success
                        is AuthResult.Error -> {
                            val errorMsg = result.message
                            Log.e(TAG, "Sign-in failed: $errorMsg")
                            ToastHelper.showMedium(context, errorMsg)
                            AuthUiState.Error(errorMsg)
                        }
                        is AuthResult.Loading -> AuthUiState.Loading.EmailSignIn
                    }
                }
                .launchIn(this)
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading.EmailSignUp

            signUpUseCase(email, password, confirmPassword)
                .onEach { result ->
                    _authState.value = when (result) {
                        is AuthResult.Success -> AuthUiState.Success
                        is AuthResult.Error -> {
                            val errorMsg = result.message
                            Log.e(TAG, "Sign-up failed: $errorMsg")
                            ToastHelper.showMedium(context, errorMsg)
                            AuthUiState.Error(errorMsg)
                        }
                        is AuthResult.Loading -> AuthUiState.Loading.EmailSignUp
                    }
                }
                .launchIn(this)
        }
    }

    fun isUserAuthenticated(): Boolean = signInUseCase.isUserAuthenticated()

    fun getCurrentUserId(): String? = signInUseCase.getCurrentUserId()
}
