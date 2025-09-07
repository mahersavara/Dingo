package io.sukhuat.dingo.ui.screens.profile.components

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.common.utils.ToastHelper
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.usecase.account.DeleteAccountUseCase
import io.sukhuat.dingo.domain.usecase.account.GetLoginHistoryUseCase
import io.sukhuat.dingo.domain.usecase.profile.ChangePasswordUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for AccountSecurity component
 */
@HiltViewModel
class AccountSecurityViewModel @Inject constructor(
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getLoginHistoryUseCase: GetLoginHistoryUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountSecurityUiState())
    val uiState: StateFlow<AccountSecurityUiState> = _uiState.asStateFlow()

    /**
     * Update current password field
     */
    fun updateCurrentPassword(password: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            passwordChangeState = currentState.passwordChangeState.copy(
                currentPassword = password,
                currentPasswordError = null
            )
        )
    }

    /**
     * Update new password field and calculate strength
     */
    fun updateNewPassword(password: String) {
        val currentState = _uiState.value
        val strength = calculatePasswordStrength(password)

        _uiState.value = currentState.copy(
            passwordChangeState = currentState.passwordChangeState.copy(
                newPassword = password,
                newPasswordError = null,
                passwordStrength = strength
            )
        )
    }

    /**
     * Update confirm password field
     */
    fun updateConfirmPassword(password: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            passwordChangeState = currentState.passwordChangeState.copy(
                confirmPassword = password,
                confirmPasswordError = null
            )
        )
    }

    /**
     * Toggle current password visibility
     */
    fun togglePasswordVisibility() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            passwordChangeState = currentState.passwordChangeState.copy(
                isPasswordVisible = !currentState.passwordChangeState.isPasswordVisible
            )
        )
    }

    /**
     * Toggle new password visibility
     */
    fun toggleNewPasswordVisibility() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            passwordChangeState = currentState.passwordChangeState.copy(
                isNewPasswordVisible = !currentState.passwordChangeState.isNewPasswordVisible
            )
        )
    }

    /**
     * Toggle confirm password visibility
     */
    fun toggleConfirmPasswordVisibility() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            passwordChangeState = currentState.passwordChangeState.copy(
                isConfirmPasswordVisible = !currentState.passwordChangeState.isConfirmPasswordVisible
            )
        )
    }

    /**
     * Show password change dialog
     */
    fun showPasswordChangeDialog() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            passwordChangeState = currentState.passwordChangeState.copy(
                showPasswordChangeDialog = true,
                // Reset form when opening dialog
                currentPassword = "",
                newPassword = "",
                confirmPassword = "",
                currentPasswordError = null,
                newPasswordError = null,
                confirmPasswordError = null,
                isPasswordVisible = false,
                isNewPasswordVisible = false,
                isConfirmPasswordVisible = false,
                passwordStrength = PasswordStrength.WEAK,
                changePasswordSuccess = false
            )
        )
    }

    /**
     * Hide password change dialog
     */
    fun hidePasswordChangeDialog() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            passwordChangeState = currentState.passwordChangeState.copy(
                showPasswordChangeDialog = false
            )
        )
    }

    /**
     * Change user password
     */
    fun changePassword() {
        try {
            viewModelScope.launch {
                try {
                    // Get current state safely
                    val currentState = _uiState.value
                    val passwordState = currentState.passwordChangeState

                    // Basic validation to prevent null/empty crashes
                    if (passwordState.currentPassword.isBlank()) {
                        updateErrorState(currentState, "Current password is required")
                        return@launch
                    }

                    if (passwordState.newPassword.isBlank()) {
                        updateErrorState(currentState, "New password is required")
                        return@launch
                    }

                    if (passwordState.confirmPassword.isBlank()) {
                        updateErrorState(currentState, "Please confirm your new password")
                        return@launch
                    }

                    if (passwordState.newPassword != passwordState.confirmPassword) {
                        updateErrorState(currentState, "Passwords do not match")
                        return@launch
                    }

                    // Set loading state
                    updateLoadingState(currentState, true)

                    // Call use case and handle result
                    Log.d("AccountSecurityVM", "Calling changePasswordUseCase...")
                    val result = changePasswordUseCase.changePassword(
                        currentPassword = passwordState.currentPassword,
                        newPassword = passwordState.newPassword
                    )
                    Log.d("AccountSecurityVM", "ChangePasswordUseCase result: ${result::class.simpleName}")

                    when (result) {
                        is ChangePasswordUseCase.PasswordChangeResult.Success -> {
                            Log.d("AccountSecurityVM", "SUCCESS result received - calling handlePasswordChangeSuccess")
                            handlePasswordChangeSuccess()
                        }
                        is ChangePasswordUseCase.PasswordChangeResult.ValidationError -> {
                            Log.d("AccountSecurityVM", "ValidationError: ${result.field} - ${result.message}")
                            handleValidationError(result.field, result.message)
                        }
                        is ChangePasswordUseCase.PasswordChangeResult.AuthError -> {
                            Log.d("AccountSecurityVM", "AuthError: ${result.message}")
                            handleAuthenticationError(result.message)
                        }
                        is ChangePasswordUseCase.PasswordChangeResult.NetworkError -> {
                            Log.d("AccountSecurityVM", "NetworkError: ${result.message}")
                            handleNetworkError(result.message)
                        }
                        is ChangePasswordUseCase.PasswordChangeResult.UnknownError -> {
                            Log.d("AccountSecurityVM", "UnknownError: ${result.message}")
                            handleUnknownError(result.message)
                        }
                    }
                } catch (coroutineError: Exception) {
                    handleCoroutineError(coroutineError)
                }
            }
        } catch (outerError: Exception) {
            handleOuterError(outerError)
        }
    }

    private fun updateErrorState(currentState: AccountSecurityUiState, errorMessage: String) {
        try {
            _uiState.value = currentState.copy(error = errorMessage)
        } catch (e: Exception) {
            // Even this fails, do nothing to prevent crash
        }
    }

    private fun updateLoadingState(currentState: AccountSecurityUiState, isLoading: Boolean) {
        try {
            _uiState.value = currentState.copy(
                passwordChangeState = currentState.passwordChangeState.copy(
                    isChangingPassword = isLoading,
                    currentPasswordError = null,
                    newPasswordError = null,
                    confirmPasswordError = null
                )
            )
        } catch (e: Exception) {
            // Fail silently to prevent crash
        }
    }

    private fun handlePasswordChangeSuccess() {
        try {
            Log.d("AccountSecurityVM", "handlePasswordChangeSuccess called - showing success toast")
            val successState = _uiState.value
            _uiState.value = successState.copy(
                passwordChangeState = successState.passwordChangeState.copy(
                    isChangingPassword = false,
                    showPasswordChangeDialog = false,
                    changePasswordSuccess = true
                )
            )

            // Show success toast
            Log.d("AccountSecurityVM", "About to show toast: Password changed successfully!")
            try {
                // Try both ToastHelper and direct Toast as fallback
                ToastHelper.showMedium(context, "Password changed successfully!")
                Log.d("AccountSecurityVM", "ToastHelper called successfully")

                // Also try direct toast as backup
                android.widget.Toast.makeText(context, "Password changed successfully! ✅", android.widget.Toast.LENGTH_LONG).show()
                Log.d("AccountSecurityVM", "Direct Toast called successfully")
            } catch (e: Exception) {
                Log.e("AccountSecurityVM", "Error showing toast", e)
            }

            // Reset success flag after delay
            viewModelScope.launch {
                try {
                    delay(3000)
                    val resetState = _uiState.value
                    _uiState.value = resetState.copy(
                        passwordChangeState = resetState.passwordChangeState.copy(
                            changePasswordSuccess = false
                        )
                    )
                } catch (e: Exception) {
                    Log.e("AccountSecurityVM", "Error resetting success flag", e)
                }
            }
        } catch (e: Exception) {
            Log.e("AccountSecurityVM", "Error in handlePasswordChangeSuccess", e)
        }
    }

    private fun handleValidationError(field: String, errorMessage: String) {
        try {
            val currentState = _uiState.value

            when (field) {
                "currentPassword" -> {
                    _uiState.value = currentState.copy(
                        passwordChangeState = currentState.passwordChangeState.copy(
                            isChangingPassword = false,
                            currentPasswordError = errorMessage
                        ),
                        error = errorMessage
                    )
                }
                "newPassword" -> {
                    _uiState.value = currentState.copy(
                        passwordChangeState = currentState.passwordChangeState.copy(
                            isChangingPassword = false,
                            newPasswordError = errorMessage
                        ),
                        error = errorMessage
                    )
                }
                "confirmPassword" -> {
                    _uiState.value = currentState.copy(
                        passwordChangeState = currentState.passwordChangeState.copy(
                            isChangingPassword = false,
                            confirmPasswordError = errorMessage
                        ),
                        error = errorMessage
                    )
                }
                else -> {
                    _uiState.value = currentState.copy(
                        passwordChangeState = currentState.passwordChangeState.copy(
                            isChangingPassword = false
                        ),
                        error = errorMessage
                    )
                }
            }
        } catch (e: Exception) {
            handleGenericError("Validation error handling failed")
        }
    }

    private fun handleAuthenticationError(errorMessage: String) {
        try {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                passwordChangeState = currentState.passwordChangeState.copy(
                    isChangingPassword = false,
                    currentPasswordError = errorMessage
                ),
                error = errorMessage
            )
        } catch (e: Exception) {
            handleGenericError("Authentication error handling failed")
        }
    }

    private fun handleNetworkError(errorMessage: String) {
        try {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                passwordChangeState = currentState.passwordChangeState.copy(
                    isChangingPassword = false
                ),
                error = errorMessage
            )
        } catch (e: Exception) {
            handleGenericError("Network error handling failed")
        }
    }

    private fun handleUnknownError(errorMessage: String) {
        try {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                passwordChangeState = currentState.passwordChangeState.copy(
                    isChangingPassword = false
                ),
                error = errorMessage
            )
        } catch (e: Exception) {
            handleGenericError("Unknown error handling failed")
        }
    }

    private fun handleUseCaseError(error: Exception) {
        try {
            val currentState = _uiState.value
            val errorMessage = error.message ?: "Unknown use case error"
            _uiState.value = currentState.copy(
                passwordChangeState = currentState.passwordChangeState.copy(
                    isChangingPassword = false
                ),
                error = "Password change failed: $errorMessage"
            )
        } catch (e: Exception) {
            handleGenericError("Use case error handling failed")
        }
    }

    private fun handleCoroutineError(error: Exception) {
        handleGenericError("Coroutine error: ${error.message ?: "Unknown coroutine error"}")
    }

    private fun handleOuterError(error: Exception) {
        handleGenericError("Outer error: ${error.message ?: "Unknown outer error"}")
    }

    private fun handleGenericError(message: String) {
        try {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                passwordChangeState = currentState.passwordChangeState.copy(
                    isChangingPassword = false
                ),
                error = message
            )
        } catch (e: Exception) {
            // Ultimate fallback - do nothing to prevent crash
        }
    }

    /**
     * Load login history
     */
    fun loadLoginHistory() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    loginHistoryState = currentState.loginHistoryState.copy(
                        isLoadingHistory = true,
                        error = null
                    )
                )

                val loginHistory = getLoginHistoryUseCase()
                val loginSummary = getLoginHistoryUseCase.getLoginSummary()

                val updatedState = _uiState.value
                _uiState.value = updatedState.copy(
                    loginHistoryState = updatedState.loginHistoryState.copy(
                        isLoadingHistory = false,
                        loginHistory = loginHistory,
                        loginSummary = loginSummary
                    )
                )
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    loginHistoryState = currentState.loginHistoryState.copy(
                        isLoadingHistory = false,
                        error = "Failed to load login history"
                    )
                )
            }
        }
    }

    /**
     * Toggle full history display
     */
    fun toggleFullHistory() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            loginHistoryState = currentState.loginHistoryState.copy(
                showFullHistory = !currentState.loginHistoryState.showFullHistory
            )
        )
    }

    /**
     * Show account deletion dialog
     */
    fun showAccountDeletion() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            accountDeletionState = currentState.accountDeletionState.copy(
                showDeletionDialog = true,
                confirmationText = "",
                deletionError = null
            )
        )
    }

    /**
     * Hide account deletion dialog
     */
    fun hideAccountDeletion() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            accountDeletionState = currentState.accountDeletionState.copy(
                showDeletionDialog = false
            )
        )
    }

    /**
     * Update confirmation text for account deletion
     */
    fun updateConfirmationText(text: String) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            accountDeletionState = currentState.accountDeletionState.copy(
                confirmationText = text,
                deletionError = null
            )
        )
    }

    /**
     * Delete user account
     */
    fun deleteAccount() {
        val deletionState = _uiState.value.accountDeletionState

        if (deletionState.confirmationText != "DELETE") {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                accountDeletionState = currentState.accountDeletionState.copy(
                    deletionError = "Please type DELETE to confirm"
                )
            )
            return
        }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    accountDeletionState = currentState.accountDeletionState.copy(
                        isDeletingAccount = true,
                        deletionError = null
                    )
                )

                deleteAccountUseCase(
                    confirmationText = deletionState.confirmationText,
                    expectedConfirmation = "DELETE"
                )

                // Account deletion successful - this will typically trigger navigation
                // back to the authentication screen
            } catch (error: ProfileError.ValidationError) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    accountDeletionState = currentState.accountDeletionState.copy(
                        isDeletingAccount = false,
                        deletionError = error.message
                    )
                )
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    accountDeletionState = currentState.accountDeletionState.copy(
                        isDeletingAccount = false,
                        deletionError = "Failed to delete account. Please try again."
                    )
                )
            }
        }
    }

    /**
     * Dismiss error message
     */
    fun dismissError() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(error = null)
    }

    /**
     * Get actions for the UI
     */
    fun getActions(): AccountSecurityActions {
        return AccountSecurityActions(
            onCurrentPasswordChange = ::updateCurrentPassword,
            onNewPasswordChange = ::updateNewPassword,
            onConfirmPasswordChange = ::updateConfirmPassword,
            onTogglePasswordVisibility = ::togglePasswordVisibility,
            onToggleNewPasswordVisibility = ::toggleNewPasswordVisibility,
            onToggleConfirmPasswordVisibility = ::toggleConfirmPasswordVisibility,
            onShowPasswordChangeDialog = ::showPasswordChangeDialog,
            onHidePasswordChangeDialog = ::hidePasswordChangeDialog,
            onChangePassword = ::changePassword,
            onLoadLoginHistory = ::loadLoginHistory,
            onToggleFullHistory = ::toggleFullHistory,
            onShowAccountDeletion = ::showAccountDeletion,
            onHideAccountDeletion = ::hideAccountDeletion,
            onConfirmationTextChange = ::updateConfirmationText,
            onDeleteAccount = ::deleteAccount,
            onDismissError = ::dismissError
        )
    }

    /**
     * Calculate password strength based on various criteria
     */
    private fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.isEmpty()) return PasswordStrength.WEAK

        var score = 0

        // Length check
        when {
            password.length >= 12 -> score += 2
            password.length >= 8 -> score += 1
        }

        // Character variety checks
        if (password.any { it.isUpperCase() }) score += 1
        if (password.any { it.isLowerCase() }) score += 1
        if (password.any { it.isDigit() }) score += 1
        if (password.any { !it.isLetterOrDigit() }) score += 1

        // Bonus for very long passwords
        if (password.length >= 16) score += 1

        return when (score) {
            0, 1 -> PasswordStrength.WEAK
            2, 3 -> PasswordStrength.FAIR
            4, 5 -> PasswordStrength.GOOD
            6 -> PasswordStrength.STRONG
            else -> PasswordStrength.VERY_STRONG
        }
    }

    /**
     * Validate password inputs
     */
    private fun validatePasswordInputs(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (currentPassword.isBlank()) {
            errors["currentPassword"] = "Current password is required"
        }

        if (newPassword.isBlank()) {
            errors["newPassword"] = "New password is required"
        } else {
            if (newPassword.length < 8) {
                errors["newPassword"] = "Password must be at least 8 characters long"
            } else if (newPassword == currentPassword) {
                errors["newPassword"] = "New password must be different from current password"
            } else if (!isPasswordStrong(newPassword)) {
                errors["newPassword"] = "Password must contain uppercase, lowercase, number, and special character"
            }
        }

        if (confirmPassword.isBlank()) {
            errors["confirmPassword"] = "Please confirm your new password"
        } else if (newPassword != confirmPassword) {
            errors["confirmPassword"] = "Passwords do not match"
        }

        return errors
    }

    /**
     * Check if password meets strength requirements
     */
    private fun isPasswordStrong(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar && password.length >= 8
    }
}
