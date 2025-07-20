package io.sukhuat.dingo.domain.validation

import android.net.Uri
import io.sukhuat.dingo.domain.model.ProfileError
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive validation utility for profile-related inputs
 */
@Singleton
class ProfileValidator @Inject constructor() {

    companion object {
        private const val MIN_DISPLAY_NAME_LENGTH = 1
        private const val MAX_DISPLAY_NAME_LENGTH = 50
        private const val MIN_PASSWORD_LENGTH = 8
        private const val MAX_PASSWORD_LENGTH = 128
        private const val MAX_IMAGE_SIZE_MB = 5
        private const val MAX_IMAGE_SIZE_BYTES = MAX_IMAGE_SIZE_MB * 1024 * 1024

        private val EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
        )

        private val DISPLAY_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-\\_\\.]+$")

        private val SUPPORTED_IMAGE_TYPES = setOf(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/webp"
        )
    }

    /**
     * Validation result wrapper
     */
    sealed class ValidationResult {
        object Valid : ValidationResult()
        data class Invalid(val error: ProfileError.ValidationError) : ValidationResult()
    }

    /**
     * Validate display name
     */
    fun validateDisplayName(displayName: String): ValidationResult {
        return when {
            displayName.isBlank() -> ValidationResult.Invalid(
                ProfileError.ValidationError("displayName", "Display name cannot be empty")
            )
            displayName.length < MIN_DISPLAY_NAME_LENGTH -> ValidationResult.Invalid(
                ProfileError.ValidationError("displayName", "Display name is too short")
            )
            displayName.length > MAX_DISPLAY_NAME_LENGTH -> ValidationResult.Invalid(
                ProfileError.ValidationError("displayName", "Display name is too long (max $MAX_DISPLAY_NAME_LENGTH characters)")
            )
            !DISPLAY_NAME_PATTERN.matcher(displayName).matches() -> ValidationResult.Invalid(
                ProfileError.ValidationError("displayName", "Display name contains invalid characters")
            )
            containsProfanity(displayName) -> ValidationResult.Invalid(
                ProfileError.ValidationError("displayName", "Display name contains inappropriate content")
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate email address
     */
    fun validateEmail(email: String): ValidationResult {
        return when {
            email.isBlank() -> ValidationResult.Invalid(
                ProfileError.ValidationError("email", "Email cannot be empty")
            )
            !EMAIL_PATTERN.matcher(email).matches() -> ValidationResult.Invalid(
                ProfileError.ValidationError("email", "Please enter a valid email address")
            )
            email.length > 254 -> ValidationResult.Invalid(
                ProfileError.ValidationError("email", "Email address is too long")
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate password strength
     */
    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult.Invalid(
                ProfileError.ValidationError("password", "Password cannot be empty")
            )
            password.length < MIN_PASSWORD_LENGTH -> ValidationResult.Invalid(
                ProfileError.ValidationError("password", "Password must be at least $MIN_PASSWORD_LENGTH characters")
            )
            password.length > MAX_PASSWORD_LENGTH -> ValidationResult.Invalid(
                ProfileError.ValidationError("password", "Password is too long (max $MAX_PASSWORD_LENGTH characters)")
            )
            !hasUpperCase(password) -> ValidationResult.Invalid(
                ProfileError.ValidationError("password", "Password must contain at least one uppercase letter")
            )
            !hasLowerCase(password) -> ValidationResult.Invalid(
                ProfileError.ValidationError("password", "Password must contain at least one lowercase letter")
            )
            !hasDigit(password) -> ValidationResult.Invalid(
                ProfileError.ValidationError("password", "Password must contain at least one number")
            )
            !hasSpecialChar(password) -> ValidationResult.Invalid(
                ProfileError.ValidationError("password", "Password must contain at least one special character")
            )
            isCommonPassword(password) -> ValidationResult.Invalid(
                ProfileError.ValidationError("password", "This password is too common. Please choose a stronger password")
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate password confirmation
     */
    fun validatePasswordConfirmation(password: String, confirmation: String): ValidationResult {
        return when {
            confirmation.isBlank() -> ValidationResult.Invalid(
                ProfileError.ValidationError("passwordConfirmation", "Please confirm your password")
            )
            password != confirmation -> ValidationResult.Invalid(
                ProfileError.ValidationError("passwordConfirmation", "Passwords do not match")
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate profile image
     */
    fun validateProfileImage(uri: Uri?, mimeType: String?, sizeBytes: Long?): ValidationResult {
        return when {
            uri == null -> ValidationResult.Invalid(
                ProfileError.ValidationError("profileImage", "Please select an image")
            )
            mimeType == null || !SUPPORTED_IMAGE_TYPES.contains(mimeType.lowercase()) -> ValidationResult.Invalid(
                ProfileError.ValidationError("profileImage", "Unsupported image format. Please use JPEG, PNG, or WebP")
            )
            sizeBytes != null && sizeBytes > MAX_IMAGE_SIZE_BYTES -> ValidationResult.Invalid(
                ProfileError.ValidationError("profileImage", "Image is too large (max ${MAX_IMAGE_SIZE_MB}MB)")
            )
            sizeBytes != null && sizeBytes <= 0 -> ValidationResult.Invalid(
                ProfileError.ValidationError("profileImage", "Invalid image file")
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate export data request
     */
    fun validateExportRequest(userId: String): ValidationResult {
        return when {
            userId.isBlank() -> ValidationResult.Invalid(
                ProfileError.ValidationError("userId", "User ID is required for export")
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate account deletion confirmation
     */
    fun validateAccountDeletionConfirmation(confirmationText: String, expectedText: String = "DELETE"): ValidationResult {
        return when {
            confirmationText.isBlank() -> ValidationResult.Invalid(
                ProfileError.ValidationError("confirmation", "Please type '$expectedText' to confirm")
            )
            confirmationText != expectedText -> ValidationResult.Invalid(
                ProfileError.ValidationError("confirmation", "Confirmation text does not match. Please type '$expectedText'")
            )
            else -> ValidationResult.Valid
        }
    }

    /**
     * Validate batch operations
     */
    fun validateBatchOperation(operations: List<Any>): ValidationResult {
        return when {
            operations.isEmpty() -> ValidationResult.Invalid(
                ProfileError.ValidationError("batch", "No operations to perform")
            )
            operations.size > 100 -> ValidationResult.Invalid(
                ProfileError.ValidationError("batch", "Too many operations in batch (max 100)")
            )
            else -> ValidationResult.Valid
        }
    }

    // Helper methods
    private fun hasUpperCase(password: String): Boolean = password.any { it.isUpperCase() }
    private fun hasLowerCase(password: String): Boolean = password.any { it.isLowerCase() }
    private fun hasDigit(password: String): Boolean = password.any { it.isDigit() }
    private fun hasSpecialChar(password: String): Boolean = password.any { !it.isLetterOrDigit() }

    private fun isCommonPassword(password: String): Boolean {
        val commonPasswords = setOf(
            "password", "123456", "123456789", "12345678", "12345",
            "1234567", "password123", "admin", "qwerty", "abc123",
            "letmein", "welcome", "monkey", "dragon", "master"
        )
        return commonPasswords.contains(password.lowercase())
    }

    private fun containsProfanity(text: String): Boolean {
        // Basic profanity filter - in a real app, you'd use a more comprehensive solution
        val profanityWords = setOf(
            // Add basic inappropriate words here
            "spam",
            "test_profanity"
        )
        val lowerText = text.lowercase()
        return profanityWords.any { lowerText.contains(it) }
    }

    /**
     * Get password strength score (0-4)
     */
    fun getPasswordStrength(password: String): Int {
        var score = 0

        if (password.length >= MIN_PASSWORD_LENGTH) score++
        if (hasUpperCase(password) && hasLowerCase(password)) score++
        if (hasDigit(password)) score++
        if (hasSpecialChar(password)) score++
        if (password.length >= 12) score++ // Bonus for longer passwords

        return minOf(score, 4)
    }

    /**
     * Get password strength description
     */
    fun getPasswordStrengthDescription(score: Int): String {
        return when (score) {
            0, 1 -> "Very Weak"
            2 -> "Weak"
            3 -> "Good"
            4 -> "Strong"
            else -> "Unknown"
        }
    }
}
