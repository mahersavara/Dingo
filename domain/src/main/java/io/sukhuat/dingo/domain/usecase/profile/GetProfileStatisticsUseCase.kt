package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting profile statistics with real-time updates
 */
class GetProfileStatisticsUseCase @Inject constructor(
    private val profileStatisticsRepository: ProfileStatisticsRepository
) {
    /**
     * Get profile statistics as a Flow for real-time updates
     * @return Flow of ProfileStatistics that emits updates when statistics change
     */
    suspend operator fun invoke(): Flow<ProfileStatistics> {
        return profileStatisticsRepository.getProfileStatistics()
    }
}
