package io.sukhuat.dingo.ui.screens.auth

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sukhuat.dingo.data.auth.GoogleAuthService
import io.sukhuat.dingo.data.model.AuthResult
import io.sukhuat.dingo.usecases.auth.SignInUseCase
import io.sukhuat.dingo.usecases.auth.SignUpWithEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpWithEmailUseCase,
    private val googleAuthService: GoogleAuthService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val authState: StateFlow<AuthUiState> = _authState

    fun initiateGoogleSignIn(launcher: ActivityResultLauncher<Intent>) {
        try {
            launcher.launch(googleAuthService.getSignInIntent())
        } catch (e: Exception) {
            _authState.value = AuthUiState.Error("Failed to start Google Sign-In")
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            signInUseCase.signInWithGoogle(idToken)
                .onEach { result ->
                    _authState.value = when (result) {
                        is AuthResult.Success -> AuthUiState.Success
                        is AuthResult.Error -> AuthUiState.Error(
                            result.message ?: "Google sign-in failed"
                        )
                        is AuthResult.Loading -> AuthUiState.Loading
                    }
                }
                .launchIn(this)
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            signInUseCase(email, password)
                .onEach { result ->
                    _authState.value = when (result) {
                        is AuthResult.Success -> AuthUiState.Success
                        is AuthResult.Error -> AuthUiState.Error(result.message ?: "Sign-in failed")
                        is AuthResult.Loading -> AuthUiState.Loading
                    }
                }
                .launchIn(this)
        }
    }

    fun signUp(email: String, password: String, confirmPassword: String) {
        viewModelScope.launch {
            signUpUseCase(email, password, confirmPassword)
                .onEach { result ->
                    _authState.value = when (result) {
                        is AuthResult.Success -> AuthUiState.Success
                        is AuthResult.Error -> AuthUiState.Error(result.message ?: "Sign-up failed")
                        is AuthResult.Loading -> AuthUiState.Loading
                    }
                }
                .launchIn(this)
        }
    }

    fun isUserAuthenticated(): Boolean = signInUseCase.isUserAuthenticated()

    fun getCurrentUserId(): String? = signInUseCase.getCurrentUserId()
}
