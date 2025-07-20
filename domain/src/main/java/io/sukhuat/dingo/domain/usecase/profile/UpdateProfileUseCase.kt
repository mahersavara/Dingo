package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case for updating user profile with validation and error handling
 */
class UpdateProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    /**
     * Update user's display name with validation
     * @param displayName New display name to set
     * @throws ProfileError.ValidationError if display name is invalid
     * @throws ProfileError.AuthenticationExpired if user is not authenticated
     * @throws ProfileError.UnknownError for other errors
     */
    suspend fun updateDisplayName(displayName: String) {
        // Validate display name
        validateDisplayName(displayName)

        // Update display name through repository
        userProfileRepository.updateDisplayName(displayName)
    }

    /**
     * Validate display name according to business rules
     * @param displayName Display name to validate
     * @throws ProfileError.ValidationError if validation fails
     */
    private fun validateDisplayName(displayName: String) {
        when {
            displayName.isBlank() -> {
                throw ProfileError.ValidationError("displayName", "Display name cannot be empty")
            }
            displayName.length < 2 -> {
                throw ProfileError.ValidationError("displayName", "Display name must be at least 2 characters")
            }
            displayName.length > 50 -> {
                throw ProfileError.ValidationError("displayName", "Display name cannot exceed 50 characters")
            }
            !displayName.matches(Regex("^[a-zA-Z0-9\\s._-]+$")) -> {
                throw ProfileError.ValidationError("displayName", "Display name contains invalid characters")
            }
        }
    }
}
