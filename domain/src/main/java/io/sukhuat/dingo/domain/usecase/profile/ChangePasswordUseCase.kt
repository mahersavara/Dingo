package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import io.sukhuat.dingo.domain.validation.ProfileValidator
import javax.inject.Inject

/**
 * Use case for changing user password with comprehensive validation and security checks
 */
class ChangePasswordUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val profileValidator: ProfileValidator
) {
    /**
     * Change user password with validation and re-authentication
     * @param currentPassword Current password for verification
     * @param newPassword New password to set
     * @throws ProfileError.ValidationError if passwords don't meet requirements
     * @throws ProfileError.AuthenticationExpired if re-authentication fails
     * @throws ProfileError.NetworkError if password change fails
     */
    suspend fun changePassword(currentPassword: String, newPassword: String) {
        // Validate current password is not empty
        if (currentPassword.isBlank()) {
            throw ProfileError.ValidationError("currentPassword", "Current password is required")
        }

        // Validate new password meets requirements
        val newPasswordValidation = profileValidator.validatePassword(newPassword)
        if (newPasswordValidation is ProfileValidator.ValidationResult.Invalid) {
            throw newPasswordValidation.error
        }

        // Ensure passwords are different
        if (currentPassword == newPassword) {
            throw ProfileError.ValidationError("newPassword", "New password must be different from current password")
        }

        // Check password strength
        val strengthScore = profileValidator.getPasswordStrength(newPassword)
        if (strengthScore < 2) {
            throw ProfileError.ValidationError("newPassword", "Password is too weak. Please choose a stronger password.")
        }

        // Attempt password change through repository
        try {
            userProfileRepository.changePassword(currentPassword, newPassword)
        } catch (e: Exception) {
            // Map repository exceptions to domain errors
            when {
                e.message?.contains("auth", ignoreCase = true) == true -> {
                    throw ProfileError.AuthenticationExpired
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    throw ProfileError.NetworkUnavailable
                }
                e.message?.contains("weak", ignoreCase = true) == true -> {
                    throw ProfileError.ValidationError("newPassword", "Password does not meet security requirements")
                }
                else -> {
                    throw ProfileError.UnknownError(e)
                }
            }
        }
    }

    /**
     * Validate password strength and return feedback
     * @param password Password to validate
     * @return Pair of strength score (0-4) and feedback message
     */
    fun validatePasswordStrength(password: String): Pair<Int, String> {
        val strengthScore = profileValidator.getPasswordStrength(password)
        val strengthDescription = profileValidator.getPasswordStrengthDescription(strengthScore)
        return Pair(strengthScore, strengthDescription)
    }

    /**
     * Check if current user can change password (requires email/password auth)
     * @return true if user can change password, false otherwise
     */
    suspend fun canChangePassword(): Boolean {
        return try {
            val authCapabilities = userProfileRepository.getAuthCapabilities()
            authCapabilities.canChangePassword
        } catch (e: Exception) {
            false
        }
    }
}
