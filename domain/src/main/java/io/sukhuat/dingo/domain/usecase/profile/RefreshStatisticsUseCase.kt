package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import javax.inject.Inject

/**
 * Use case for manually refreshing profile statistics
 */
class RefreshStatisticsUseCase @Inject constructor(
    private val profileStatisticsRepository: ProfileStatisticsRepository
) {
    /**
     * Manually refresh profile statistics by recalculating from current goal data
     * This will update statistics, check for new achievements, and trigger real-time updates
     * * @throws ProfileError.AuthenticationExpired if user is not authenticated
     * @throws ProfileError.UnknownError if refresh fails
     */
    suspend operator fun invoke() {
        try {
            profileStatisticsRepository.refreshStatistics()
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    /**
     * Check if statistics need refreshing based on last update time
     * This can be used to determine if automatic refresh is needed
     * * @return true if statistics should be refreshed
     */
    fun shouldRefreshStatistics(): Boolean {
        // For now, always allow manual refresh
        // In the future, this could check last update timestamp
        return true
    }
}
