package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import javax.inject.Inject

/**
 * Use case for getting achievements with unlock animations support
 */
class GetAchievementsUseCase @Inject constructor(
    private val profileStatisticsRepository: ProfileStatisticsRepository
) {
    /**
     * Get all achievements (both locked and unlocked)
     * * @return List of achievements with unlock status and dates
     * @throws ProfileError.AuthenticationExpired if user is not authenticated
     * @throws ProfileError.UnknownError if retrieval fails
     */
    suspend operator fun invoke(): List<Achievement> {
        return try {
            profileStatisticsRepository.getAchievements()
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    /**
     * Get only unlocked achievements
     * Useful for displaying user's accomplishments
     * * @return List of unlocked achievements sorted by unlock date
     */
    suspend fun getUnlockedAchievements(): List<Achievement> {
        return invoke()
            .filter { it.isUnlocked }
            .sortedByDescending { it.unlockedDate }
    }

    /**
     * Get only locked achievements
     * Useful for showing what users can still achieve
     * * @return List of locked achievements
     */
    suspend fun getLockedAchievements(): List<Achievement> {
        return invoke()
            .filter { !it.isUnlocked }
    }

    /**
     * Get recently unlocked achievements (within last 7 days)
     * Useful for showing unlock animations or notifications
     * * @return List of recently unlocked achievements
     */
    suspend fun getRecentlyUnlockedAchievements(): List<Achievement> {
        val sevenDaysInMillis = 7 * 24 * 60 * 60 * 1000L
        val sevenDaysAgo = System.currentTimeMillis() - sevenDaysInMillis

        return getUnlockedAchievements()
            .filter { achievement ->
                achievement.unlockedDate != null && achievement.unlockedDate!! > sevenDaysAgo
            }
    }

    /**
     * Get achievement statistics summary
     * * @return Summary of achievement progress
     */
    suspend fun getAchievementSummary(): AchievementSummary {
        val achievements = invoke()
        val totalAchievements = achievements.size
        val unlockedAchievements = achievements.count { it.isUnlocked }
        val completionPercentage = if (totalAchievements > 0) {
            (unlockedAchievements.toFloat() / totalAchievements.toFloat()) * 100f
        } else {
            0f
        }

        return AchievementSummary(
            totalAchievements = totalAchievements,
            unlockedAchievements = unlockedAchievements,
            lockedAchievements = totalAchievements - unlockedAchievements,
            completionPercentage = completionPercentage
        )
    }
}

/**
 * Summary of achievement progress
 */
data class AchievementSummary(
    val totalAchievements: Int,
    val unlockedAchievements: Int,
    val lockedAchievements: Int,
    val completionPercentage: Float
)
