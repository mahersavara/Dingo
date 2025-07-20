package io.sukhuat.dingo.domain.usecase.account

import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case for deleting user account with confirmation flow
 */
class DeleteAccountUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * Delete user account after confirmation
     * This is a destructive operation that cannot be undone
     * * @param confirmationText User must type specific text to confirm deletion
     * @param expectedConfirmation The expected confirmation text (usually "DELETE")
     * @throws ProfileError.ValidationError if confirmation doesn't match
     * @throws ProfileError.AuthenticationExpired if user is not authenticated
     * @throws ProfileError.UnknownError if deletion fails
     */
    suspend operator fun invoke(
        confirmationText: String,
        expectedConfirmation: String = "DELETE"
    ) {
        // Validate confirmation
        if (confirmationText != expectedConfirmation) {
            throw ProfileError.ValidationError(
                "confirmation",
                "Please type '$expectedConfirmation' to confirm account deletion"
            )
        }

        try {
            // Delete account through repository
            userProfileRepository.deleteUserAccount()
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    /**
     * Get information about what will be deleted
     * This helps users understand the consequences of account deletion
     */
    fun getDeletionInfo(): AccountDeletionInfo {
        return AccountDeletionInfo(
            willDeleteProfile = true,
            willDeleteGoals = true,
            willDeletePreferences = true,
            willDeleteStatistics = true,
            willDeleteAchievements = true,
            willDeleteProfileImages = true,
            isReversible = false,
            confirmationRequired = true
        )
    }
}

/**
 * Information about account deletion consequences
 */
data class AccountDeletionInfo(
    val willDeleteProfile: Boolean,
    val willDeleteGoals: Boolean,
    val willDeletePreferences: Boolean,
    val willDeleteStatistics: Boolean,
    val willDeleteAchievements: Boolean,
    val willDeleteProfileImages: Boolean,
    val isReversible: Boolean,
    val confirmationRequired: Boolean
)
