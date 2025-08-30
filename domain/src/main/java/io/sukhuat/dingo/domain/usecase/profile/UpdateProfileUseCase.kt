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
    suspend fun updateDisplayName(displayName: String) {
        println("UpdateProfileUseCase: updateDisplayName called with: '$displayName'")

        // Validate display name
        println("UpdateProfileUseCase: Starting validation")
        try {
            validateDisplayName(displayName)
            println("UpdateProfileUseCase: Validation passed")
        } catch (e: Exception) {
            println("UpdateProfileUseCase: Validation failed: ${e.message}")
            throw e
        }

        // Update display name through repository
        println("UpdateProfileUseCase: Calling userProfileRepository.updateDisplayName")
        try {
            userProfileRepository.updateDisplayName(displayName)
            println("UpdateProfileUseCase: Repository update successful")
        } catch (e: Exception) {
            println("UpdateProfileUseCase: Repository update failed: ${e.message}")
            throw e
        }
    }

    private fun validateDisplayName(displayName: String) {
        println("UpdateProfileUseCase: validateDisplayName called with: '$displayName' (length: ${displayName.length})")

        when {
            displayName.isBlank() -> {
                println("UpdateProfileUseCase: Validation failed - display name is blank")
                throw ProfileError.ValidationError("displayName", "Display name cannot be empty")
            }
            displayName.length < 2 -> {
                println("UpdateProfileUseCase: Validation failed - display name too short (${displayName.length} < 2)")
                throw ProfileError.ValidationError("displayName", "Display name must be at least 2 characters")
            }
            displayName.length > 50 -> {
                println("UpdateProfileUseCase: Validation failed - display name too long (${displayName.length} > 50)")
                throw ProfileError.ValidationError("displayName", "Display name cannot exceed 50 characters")
            }
            !displayName.matches(Regex("^[\\p{L}\\p{N}\\s._-]+$")) -> {
                println("UpdateProfileUseCase: Validation failed - display name contains invalid characters")
                throw ProfileError.ValidationError("displayName", "Display name contains invalid characters")
            }
        }
        println("UpdateProfileUseCase: validateDisplayName - all checks passed")
    }
}
