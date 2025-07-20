package io.sukhuat.dingo.ui.screens.profile.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.usecase.account.ChangePasswordUseCase
import io.sukhuat.dingo.domain.usecase.account.DeleteAccountUseCase
import io.sukhuat.dingo.domain.usecase.account.GetLoginHistoryUseCase
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
    private val deleteAccountUseCase: DeleteAccountUseCase
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
        val passwordState = _uiState.value.passwordChangeState

        // Validate inputs first
        val validationErrors = validatePasswordInputs(
            passwordState.currentPassword,
            passwordState.newPassword,
            passwordState.confirmPassword
        )

        if (validationErrors.isNotEmpty()) {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                passwordChangeState = currentState.passwordChangeState.copy(
                    currentPasswordError = validationErrors["currentPassword"],
                    newPasswordError = validationErrors["newPassword"],
                    confirmPasswordError = validationErrors["confirmPassword"]
                )
            )
            return
        }

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    passwordChangeState = currentState.passwordChangeState.copy(
                        isChangingPassword = true
                    )
                )

                changePasswordUseCase(
                    currentPassword = passwordState.currentPassword,
                    newPassword = passwordState.newPassword,
                    confirmPassword = passwordState.confirmPassword
                )

                // Success - close dialog and show success message
                val successState = _uiState.value
                _uiState.value = successState.copy(
                    passwordChangeState = successState.passwordChangeState.copy(
                        isChangingPassword = false,
                        showPasswordChangeDialog = false,
                        changePasswordSuccess = true
                    )
                )

                // Reset success flag after a delay
                kotlinx.coroutines.delay(3000)
                val resetState = _uiState.value
                _uiState.value = resetState.copy(
                    passwordChangeState = resetState.passwordChangeState.copy(
                        changePasswordSuccess = false
                    )
                )
            } catch (error: ProfileError.ValidationError) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    passwordChangeState = currentState.passwordChangeState.copy(
                        isChangingPassword = false,
                        newPasswordError = error.message
                    )
                )
            } catch (error: ProfileError.AuthenticationExpired) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    passwordChangeState = currentState.passwordChangeState.copy(
                        isChangingPassword = false,
                        currentPasswordError = "Current password is incorrect"
                    )
                )
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    passwordChangeState = currentState.passwordChangeState.copy(
                        isChangingPassword = false
                    ),
                    error = "Failed to change password. Please try again."
                )
            }
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
