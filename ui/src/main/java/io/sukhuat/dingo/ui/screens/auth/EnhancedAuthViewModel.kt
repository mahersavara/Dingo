package io.sukhuat.dingo.ui.screens.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.common.localization.LanguagePreferences
import io.sukhuat.dingo.common.localization.LocaleHelper
import io.sukhuat.dingo.common.utils.ToastHelper
import io.sukhuat.dingo.data.auth.GoogleAuthService
import io.sukhuat.dingo.domain.repository.AuthResult
import io.sukhuat.dingo.usecases.auth.CheckEmailVerificationUseCase
import io.sukhuat.dingo.usecases.auth.ResendEmailVerificationUseCase
import io.sukhuat.dingo.usecases.auth.SendEmailVerificationUseCase
import io.sukhuat.dingo.usecases.auth.SendPasswordResetUseCase
import io.sukhuat.dingo.usecases.auth.SignInUseCase
import io.sukhuat.dingo.usecases.auth.SignUpWithEmailUseCase
import io.sukhuat.dingo.usecases.auth.ValidatePasswordStrengthUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "EnhancedAuthViewModel"

@HiltViewModel
class EnhancedAuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpWithEmailUseCase,
    private val googleAuthService: GoogleAuthService,
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val checkEmailVerificationUseCase: CheckEmailVerificationUseCase,
    private val resendEmailVerificationUseCase: ResendEmailVerificationUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    private val validatePasswordStrengthUseCase: ValidatePasswordStrengthUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // Auth state
    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    // Password strength state
    private val _passwordStrengthState = MutableStateFlow(PasswordStrengthUiState())
    val passwordStrengthState: StateFlow<PasswordStrengthUiState> = _passwordStrengthState.asStateFlow()

    // Language change state
    private val _languageCode = MutableStateFlow<String?>(null)
    val languageCode: StateFlow<String?> = _languageCode.asStateFlow()

    // Email verification timer
    private var emailResendJob: Job? = null

    // Non-composable function to change language
    fun changeLanguage(languageCode: String) {
        viewModelScope.launch {
            // Check if this is already the current language
            val languagePreferences = LanguagePreferences(context)
            val currentLanguageCode = languagePreferences.languageCodeFlow.first()

            // Only proceed if the language is actually changing
            if (currentLanguageCode != languageCode) {
                // Save the language preference
                withContext(Dispatchers.IO) {
                    languagePreferences.setLanguageCode(languageCode)
                }

                // Apply the new locale
                LocaleHelper.setLocale(context, languageCode)

                // Update the state to trigger UI updates
                _languageCode.value = languageCode
            }
        }
    }

    // Google Sign-In
    fun initiateGoogleSignIn(launcher: ActivityResultLauncher<Intent>) {
        try {
            if (!googleAuthService.isInitialized()) {
                val errorMsg = "Google Sign-In is not properly initialized"
                _authState.value = AuthUiState.Error(errorMsg, AuthErrorType.GENERAL)
                Log.e(TAG, errorMsg)
                ToastHelper.showLong(context, errorMsg)
                return
            }

            val signInIntent = googleAuthService.getSignInIntent()
            launcher.launch(signInIntent)
        } catch (e: Exception) {
            val errorMsg = "Failed to start Google Sign-In: ${e.message ?: "Unknown error"}"
            Log.e(TAG, errorMsg, e)
            _authState.value = AuthUiState.Error(errorMsg, AuthErrorType.NETWORK)
            ToastHelper.showLong(context, errorMsg)
        }
    }

    fun handleGoogleSignInResult(result: ActivityResult) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                signInWithGoogle(idToken)
            } else {
                val errorMsg = "No ID token found in Google Sign-In result"
                _authState.value = AuthUiState.Error(errorMsg, AuthErrorType.GENERAL)
                Log.e(TAG, errorMsg)
                ToastHelper.showLong(context, errorMsg)
            }
        } catch (e: Exception) {
            val errorMsg = "Failed to process Google Sign-In result: ${e.message ?: "Unknown error"}"
            _authState.value = AuthUiState.Error(errorMsg, AuthErrorType.NETWORK)
            Log.e(TAG, errorMsg, e)
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
                            AuthUiState.Error(errorMsg, AuthErrorType.NETWORK)
                        }
                        is AuthResult.Loading -> AuthUiState.Loading.GoogleSignIn
                    }
                }
                .launchIn(this)
        }
    }

    // Email/Password Authentication
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
                            AuthUiState.Error(errorMsg, AuthErrorType.VALIDATION)
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
                        is AuthResult.Success -> {
                            // After successful signup, automatically send verification email
                            sendEmailVerification()
                            // Return RegistrationSuccess instead of Success to prevent auto-login
                            AuthUiState.RegistrationSuccess
                        }
                        is AuthResult.Error -> {
                            val errorMsg = result.message
                            Log.e(TAG, "Sign-up failed: $errorMsg")
                            ToastHelper.showMedium(context, errorMsg)
                            AuthUiState.Error(errorMsg, AuthErrorType.VALIDATION)
                        }
                        is AuthResult.Loading -> AuthUiState.Loading.EmailSignUp
                    }
                }
                .launchIn(this)
        }
    }

    // Password Strength Validation
    fun validatePasswordStrength(password: String) {
        val strength = validatePasswordStrengthUseCase(password)
        _passwordStrengthState.value = strength.toUiState()
    }

    // Email Verification
    fun sendEmailVerification() {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading.EmailVerification

            val result = sendEmailVerificationUseCase()
            _authState.value = when (result) {
                is AuthResult.Success -> {
                    ToastHelper.showMedium(context, "Verification email sent! Check your inbox.")
                    AuthUiState.EmailVerificationSent
                }
                is AuthResult.Error -> {
                    val errorMsg = result.message
                    Log.e(TAG, "Failed to send verification email: $errorMsg")
                    ToastHelper.showMedium(context, errorMsg)
                    AuthUiState.Error(errorMsg, AuthErrorType.EMAIL_VERIFICATION)
                }
                is AuthResult.Loading -> AuthUiState.Loading.EmailVerification
            }
        }
    }

    fun resendEmailVerification() {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading.EmailVerification

            val result = resendEmailVerificationUseCase()
            _authState.value = when (result) {
                is AuthResult.Success -> {
                    ToastHelper.showMedium(context, "Verification email sent again!")
                    // Start countdown timer for resend button
                    startResendCountdown()
                    AuthUiState.EmailVerificationResent(60)
                }
                is AuthResult.Error -> {
                    val errorMsg = result.message
                    Log.e(TAG, "Failed to resend verification email: $errorMsg")
                    ToastHelper.showMedium(context, errorMsg)
                    AuthUiState.Error(errorMsg, AuthErrorType.RATE_LIMITED)
                }
                is AuthResult.Loading -> AuthUiState.Loading.EmailVerification
            }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            val result = checkEmailVerificationUseCase()
            when (result) {
                is AuthResult.Success -> {
                    if (result.data) {
                        ToastHelper.showShort(context, "Email verified successfully!")
                        _authState.value = AuthUiState.Success
                    } else {
                        ToastHelper.showShort(context, "Email not yet verified. Please check your inbox.")
                    }
                }
                is AuthResult.Error -> {
                    val errorMsg = result.message
                    Log.e(TAG, "Failed to check verification status: $errorMsg")
                    ToastHelper.showMedium(context, errorMsg)
                }
                is AuthResult.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    // Password Reset
    fun sendPasswordReset(email: String) {
        Log.d(TAG, "sendPasswordReset called with email: $email")
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading.PasswordReset

            sendPasswordResetUseCase(email)
                .onEach { result ->
                    Log.d(TAG, "Password reset result: ${result.javaClass.simpleName}")
                    _authState.value = when (result) {
                        is AuthResult.Success -> {
                            Log.d(TAG, "Password reset success - showing toast and updating state")
                            ToastHelper.showMedium(context, "Password reset email sent! Check your inbox.")
                            AuthUiState.PasswordResetSent
                        }
                        is AuthResult.Error -> {
                            val errorMsg = result.message
                            Log.e(TAG, "Failed to send password reset: $errorMsg", result.exception)
                            ToastHelper.showMedium(context, errorMsg)
                            AuthUiState.Error(errorMsg, AuthErrorType.PASSWORD_RESET)
                        }
                        is AuthResult.Loading -> AuthUiState.Loading.PasswordReset
                    }
                }
                .launchIn(this)
        }
    }

    // Utility functions
    fun isUserAuthenticated(): Boolean = signInUseCase.isUserAuthenticated()

    fun getCurrentUserId(): String? = signInUseCase.getCurrentUserId()

    private fun startResendCountdown() {
        emailResendJob?.cancel()
        emailResendJob = viewModelScope.launch {
            for (i in 59 downTo 0) {
                kotlinx.coroutines.delay(1000)
                if (_authState.value is AuthUiState.EmailVerificationResent) {
                    _authState.value = AuthUiState.EmailVerificationResent(i)
                }
            }
            // Reset to EmailVerificationSent after countdown
            if (_authState.value is AuthUiState.EmailVerificationResent) {
                _authState.value = AuthUiState.EmailVerificationSent
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        emailResendJob?.cancel()
    }
}
