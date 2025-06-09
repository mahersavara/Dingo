package io.sukhuat.dingo.ui.screens.auth

sealed class AuthUiState {
    data object Initial : AuthUiState()

    sealed class Loading : AuthUiState() {
        data object GoogleSignIn : Loading()
        data object EmailSignIn : Loading()
        data object EmailSignUp : Loading()

        // Generic loading state for backward compatibility
        data object Default : Loading()
    }

    data object Success : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}
