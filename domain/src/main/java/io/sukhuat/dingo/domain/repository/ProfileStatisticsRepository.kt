package io.sukhuat.dingo.domain.repository

import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.ProfileStatistics
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing profile statistics and achievements
 */
interface ProfileStatisticsRepository {
    /**
     * Get profile statistics as a Flow for real-time updates
     */
    suspend fun getProfileStatistics(): Flow<ProfileStatistics>

    /**
     * Refresh statistics by recalculating from goal data
     */
    suspend fun refreshStatistics()

    /**
     * Get all available achievements
     */
    suspend fun getAchievements(): List<Achievement>

    /**
     * Unlock a specific achievement for the user
     * @param achievementId ID of the achievement to unlock
     */
    suspend fun unlockAchievement(achievementId: String)

    /**
     * Check and unlock achievements based on current statistics
     */
    suspend fun checkAndUnlockAchievements()
}
