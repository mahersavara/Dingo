package io.sukhuat.dingo.domain.usecase.profile

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

    sealed class PasswordChangeResult {
        object Success : PasswordChangeResult()
        data class ValidationError(val field: String, val message: String) : PasswordChangeResult()
        data class AuthError(val message: String) : PasswordChangeResult()
        data class NetworkError(val message: String) : PasswordChangeResult()
        data class UnknownError(val message: String) : PasswordChangeResult()
    }

    /**
     * Change user password with validation and re-authentication
     * @param currentPassword Current password for verification
     * @param newPassword New password to set
     * @return PasswordChangeResult indicating success or specific error type
     */
    suspend fun changePassword(currentPassword: String, newPassword: String): PasswordChangeResult {
        // Validate current password is not empty
        if (currentPassword.isBlank()) {
            return PasswordChangeResult.ValidationError("currentPassword", "Current password is required")
        }

        // Validate new password meets requirements
        val newPasswordValidation = profileValidator.validatePassword(newPassword)
        if (newPasswordValidation is ProfileValidator.ValidationResult.Invalid) {
            return PasswordChangeResult.ValidationError("newPassword", newPasswordValidation.error.message)
        }

        // Ensure passwords are different
        if (currentPassword == newPassword) {
            return PasswordChangeResult.ValidationError("newPassword", "New password must be different from current password")
        }

        // Check password strength
        val strengthScore = profileValidator.getPasswordStrength(newPassword)
        if (strengthScore < 2) {
            return PasswordChangeResult.ValidationError("newPassword", "Password is too weak. Please choose a stronger password.")
        }

        // Attempt password change through repository
        return try {
            userProfileRepository.changePassword(currentPassword, newPassword)
            PasswordChangeResult.Success
        } catch (e: Exception) {
            // Map repository exceptions to domain errors
            when {
                e.message?.contains("incorrect", ignoreCase = true) == true || e.message?.contains("invalid", ignoreCase = true) == true ||
                    e.message?.contains("wrong-password", ignoreCase = true) == true -> {
                    PasswordChangeResult.ValidationError("currentPassword", "Current password is incorrect")
                }
                e.message?.contains("expired", ignoreCase = true) == true ||
                    e.message?.contains("requires-recent-login", ignoreCase = true) == true -> {
                    PasswordChangeResult.AuthError("Please sign out and sign in again to change your password")
                }
                e.message?.contains("network", ignoreCase = true) == true -> {
                    PasswordChangeResult.NetworkError("Network error. Please check your connection and try again.")
                }
                e.message?.contains("weak-password", ignoreCase = true) == true -> {
                    PasswordChangeResult.ValidationError("newPassword", "Password is too weak")
                }
                else -> {
                    PasswordChangeResult.UnknownError("Password change failed: ${e.message ?: "Unknown error"}")
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
