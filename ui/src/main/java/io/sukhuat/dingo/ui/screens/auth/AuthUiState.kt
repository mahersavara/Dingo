package io.sukhuat.dingo.ui.screens.auth

sealed class AuthUiState {
    data object Initial : AuthUiState()

    sealed class Loading : AuthUiState() {
        data object GoogleSignIn : Loading()
        data object EmailSignIn : Loading()
        data object EmailSignUp : Loading()
        data object EmailVerification : Loading()
        data object PasswordReset : Loading()

        // Generic loading state for backward compatibility
        data object Default : Loading()
    }

    data object Success : AuthUiState()

    // Registration success but requires email verification
    data object RegistrationSuccess : AuthUiState()

    // Email verification states
    data object EmailVerificationSent : AuthUiState()
    data class EmailVerificationResent(val remainingCooldown: Int) : AuthUiState()

    // Password reset states
    data object PasswordResetSent : AuthUiState()

    // Error with enhanced context
    data class Error(
        val message: String,
        val errorType: AuthErrorType = AuthErrorType.GENERAL
    ) : AuthUiState()
}

/**
 * Types of authentication errors for better UI handling
 */
enum class AuthErrorType {
    GENERAL,
    NETWORK,
    VALIDATION,
    EMAIL_VERIFICATION,
    PASSWORD_RESET,
    RATE_LIMITED
}
