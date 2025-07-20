package io.sukhuat.dingo.domain.usecase.account

import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case for changing user password with current password verification
 */
class ChangePasswordUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * Change user password after verifying current password
     * * @param currentPassword Current password for verification
     * @param newPassword New password to set
     * @param confirmPassword Confirmation of new password
     * @throws ProfileError.ValidationError if passwords don't meet requirements
     * @throws ProfileError.AuthenticationExpired if user is not authenticated
     */
    suspend operator fun invoke(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        // Validate inputs
        validatePasswordInputs(currentPassword, newPassword, confirmPassword)

        // Change password through repository
        userProfileRepository.changePassword(currentPassword, newPassword)
    }

    private fun validatePasswordInputs(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        when {
            currentPassword.isBlank() -> {
                throw ProfileError.ValidationError("currentPassword", "Current password is required")
            }
            newPassword.isBlank() -> {
                throw ProfileError.ValidationError("newPassword", "New password is required")
            }
            newPassword.length < 8 -> {
                throw ProfileError.ValidationError("newPassword", "Password must be at least 8 characters long")
            }
            newPassword == currentPassword -> {
                throw ProfileError.ValidationError("newPassword", "New password must be different from current password")
            }
            newPassword != confirmPassword -> {
                throw ProfileError.ValidationError("confirmPassword", "Passwords do not match")
            }
            !isPasswordStrong(newPassword) -> {
                throw ProfileError.ValidationError(
                    "newPassword",
                    "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character"
                )
            }
        }
    }

    private fun isPasswordStrong(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar && password.length >= 8
    }
}
