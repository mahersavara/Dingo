package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting user profile with Flow-based data streaming
 */
class GetUserProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    /**
     * Get user profile as a Flow for real-time updates
     * @return Flow of UserProfile that emits updates when profile changes
     */
    suspend operator fun invoke(): Flow<UserProfile> {
        return userProfileRepository.getUserProfile()
    }
}
