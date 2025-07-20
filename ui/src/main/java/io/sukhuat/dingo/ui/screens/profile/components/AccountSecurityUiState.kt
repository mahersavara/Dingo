package io.sukhuat.dingo.ui.screens.profile.components

import io.sukhuat.dingo.domain.usecase.account.FormattedLoginRecord
import io.sukhuat.dingo.domain.usecase.account.LoginSummary

/**
 * UI state for account security management
 */
data class AccountSecurityUiState(
    val isLoading: Boolean = false,
    val passwordChangeState: PasswordChangeState = PasswordChangeState(),
    val loginHistoryState: LoginHistoryState = LoginHistoryState(),
    val accountDeletionState: AccountDeletionState = AccountDeletionState(),
    val error: String? = null
)

/**
 * State for password change operations
 */
data class PasswordChangeState(
    val isChangingPassword: Boolean = false,
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val currentPasswordError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val passwordStrength: PasswordStrength = PasswordStrength.WEAK,
    val showPasswordChangeDialog: Boolean = false,
    val changePasswordSuccess: Boolean = false
)

/**
 * State for login history display
 */
data class LoginHistoryState(
    val isLoadingHistory: Boolean = false,
    val loginHistory: List<FormattedLoginRecord> = emptyList(),
    val loginSummary: LoginSummary? = null,
    val showFullHistory: Boolean = false,
    val error: String? = null
)

/**
 * State for account deletion flow
 */
data class AccountDeletionState(
    val showDeletionDialog: Boolean = false,
    val confirmationText: String = "",
    val isDeletingAccount: Boolean = false,
    val deletionError: String? = null
)

/**
 * Password strength levels
 */
enum class PasswordStrength(val label: String, val score: Int) {
    WEAK("Weak", 1),
    FAIR("Fair", 2),
    GOOD("Good", 3),
    STRONG("Strong", 4),
    VERY_STRONG("Very Strong", 5)
}

/**
 * Actions available in account security
 */
data class AccountSecurityActions(
    val onCurrentPasswordChange: (String) -> Unit,
    val onNewPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onTogglePasswordVisibility: () -> Unit,
    val onToggleNewPasswordVisibility: () -> Unit,
    val onToggleConfirmPasswordVisibility: () -> Unit,
    val onShowPasswordChangeDialog: () -> Unit,
    val onHidePasswordChangeDialog: () -> Unit,
    val onChangePassword: () -> Unit,
    val onLoadLoginHistory: () -> Unit,
    val onToggleFullHistory: () -> Unit,
    val onShowAccountDeletion: () -> Unit,
    val onHideAccountDeletion: () -> Unit,
    val onConfirmationTextChange: (String) -> Unit,
    val onDeleteAccount: () -> Unit,
    val onDismissError: () -> Unit
)
