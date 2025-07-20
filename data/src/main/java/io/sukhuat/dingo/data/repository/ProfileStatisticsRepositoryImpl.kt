package io.sukhuat.dingo.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import io.sukhuat.dingo.data.mapper.ProfileStatisticsMapper
import io.sukhuat.dingo.data.model.FirebaseProfileStatistics
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of ProfileStatisticsRepository using Firebase services
 * Enhanced with proper goal data integration, achievement tracking, and caching
 */
@Singleton
class ProfileStatisticsRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val goalRepository: GoalRepository
) : ProfileStatisticsRepository {

    companion object {
        private const val TAG = "ProfileStatisticsRepo"
        private const val USERS_COLLECTION = "users"
        private const val STATISTICS_COLLECTION = "statistics"
        private const val ACHIEVEMENTS_COLLECTION = "achievements"
    }

    // Predefined achievements with comprehensive tracking
    private val predefinedAchievements = listOf(
        Achievement(
            id = "first_goal",
            title = "First Steps",
            description = "Create your first goal",
            iconResId = android.R.drawable.star_on,
            isUnlocked = false
        ),
        Achievement(
            id = "goal_master",
            title = "Goal Master",
            description = "Complete 10 goals",
            iconResId = android.R.drawable.star_on,
            isUnlocked = false
        ),
        Achievement(
            id = "streak_warrior",
            title = "Streak Warrior",
            description = "Maintain a 7-day streak",
            iconResId = android.R.drawable.star_on,
            isUnlocked = false
        ),
        Achievement(
            id = "perfectionist",
            title = "Perfectionist",
            description = "Achieve 100% completion rate with at least 5 goals",
            iconResId = android.R.drawable.star_on,
            isUnlocked = false
        ),
        Achievement(
            id = "prolific_creator",
            title = "Prolific Creator",
            description = "Create 25 goals",
            iconResId = android.R.drawable.star_on,
            isUnlocked = false
        ),
        Achievement(
            id = "month_champion",
            title = "Month Champion",
            description = "Complete 5 goals in a single month",
            iconResId = android.R.drawable.star_on,
            isUnlocked = false
        ),
        Achievement(
            id = "consistency_king",
            title = "Consistency King",
            description = "Maintain a 30-day streak",
            iconResId = android.R.drawable.star_on,
            isUnlocked = false
        )
    )

    override suspend fun getProfileStatistics(): Flow<ProfileStatistics> = callbackFlow {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            val statisticsRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(STATISTICS_COLLECTION)
                .document("data")

            val listener: ListenerRegistration = statisticsRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to statistics", error)
                    close(ProfileError.UnknownError(error))
                    return@addSnapshotListener
                }

                try {
                    if (snapshot != null && snapshot.exists()) {
                        val firebaseStats = snapshot.toObject(FirebaseProfileStatistics::class.java)
                        if (firebaseStats != null) {
                            val domainStats = ProfileStatisticsMapper.toDomain(firebaseStats)
                            trySend(domainStats)
                        }
                    } else {
                        // Create initial statistics if they don't exist
                        val initialStats = ProfileStatistics()
                        trySend(initialStats)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing statistics snapshot", e)
                    close(ProfileError.UnknownError(e))
                }
            }

            awaitClose { listener.remove() }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up statistics listener", e)
            close(ProfileError.UnknownError(e))
        }
    }

    override suspend fun refreshStatistics() {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            Log.d(TAG, "Refreshing statistics for user: $userId")

            // Get goals from GoalRepository for accurate data
            val goals = goalRepository.getAllGoals().first()

            // Calculate comprehensive statistics from goal data
            val totalGoals = goals.size
            val completedGoals = goals.count { it.status == GoalStatus.COMPLETED }

            val completionRate = if (totalGoals > 0) {
                (completedGoals.toFloat() / totalGoals.toFloat()) * 100f
            } else {
                0f
            }

            // Calculate streaks based on actual completion dates
            val streakData = calculateStreakData(goals)
            val currentStreak = streakData.first
            val longestStreak = streakData.second

            // Calculate detailed monthly statistics
            val monthlyStats = calculateDetailedMonthlyStats(goals)

            // Get current achievements
            val currentAchievements = getCurrentAchievements(userId)

            // Create updated statistics
            val updatedStats = ProfileStatistics(
                totalGoalsCreated = totalGoals,
                completedGoals = completedGoals,
                completionRate = completionRate,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                monthlyStats = monthlyStats,
                achievements = currentAchievements
            )

            // Save to Firestore
            val firebaseStats = ProfileStatisticsMapper.toFirebase(updatedStats)
            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(STATISTICS_COLLECTION)
                .document("data")
                .set(firebaseStats)
                .await()

            // Check and unlock new achievements
            checkAndUnlockAchievements()

            Log.d(TAG, "Statistics refreshed successfully: $updatedStats")
        } catch (e: ProfileError) {
            Log.e(TAG, "ProfileError during statistics refresh", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Unknown error during statistics refresh", e)
            throw ProfileError.UnknownError(e)
        }
    }

    override suspend fun getAchievements(): List<Achievement> {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            return getCurrentAchievements(userId)
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    override suspend fun unlockAchievement(achievementId: String) {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            Log.d(TAG, "Unlocking achievement: $achievementId")

            val achievementRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACHIEVEMENTS_COLLECTION)
                .document(achievementId)

            val achievementData = mapOf(
                "id" to achievementId,
                "unlocked_date" to com.google.firebase.Timestamp.now(),
                "is_unlocked" to true
            )

            achievementRef.set(achievementData).await()
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    override suspend fun checkAndUnlockAchievements() {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            // Get current statistics
            val statisticsSnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(STATISTICS_COLLECTION)
                .document("data")
                .get()
                .await()

            val stats = statisticsSnapshot.toObject(FirebaseProfileStatistics::class.java)
                ?: return

            // Check each achievement condition
            val achievementsToUnlock = mutableListOf<String>()

            // First goal achievement
            if (stats.totalGoalsCreated >= 1 && !isAchievementUnlocked(userId, "first_goal")) {
                achievementsToUnlock.add("first_goal")
            }

            // Goal master achievement
            if (stats.completedGoals >= 10 && !isAchievementUnlocked(userId, "goal_master")) {
                achievementsToUnlock.add("goal_master")
            }

            // Streak warrior achievement
            if (stats.currentStreak >= 7 && !isAchievementUnlocked(userId, "streak_warrior")) {
                achievementsToUnlock.add("streak_warrior")
            }

            // Perfectionist achievement
            if (stats.completionRate >= 100f && stats.totalGoalsCreated >= 5 && !isAchievementUnlocked(userId, "perfectionist")) {
                achievementsToUnlock.add("perfectionist")
            }

            // Prolific creator achievement
            if (stats.totalGoalsCreated >= 25 && !isAchievementUnlocked(userId, "prolific_creator")) {
                achievementsToUnlock.add("prolific_creator")
            }

            // Month champion achievement - check if any month has 5+ completed goals
            val hasMonthWith5Completions = stats.monthlyStats.values.any { it.goalsCompleted >= 5 }
            if (hasMonthWith5Completions && !isAchievementUnlocked(userId, "month_champion")) {
                achievementsToUnlock.add("month_champion")
            }

            // Consistency king achievement
            if (stats.currentStreak >= 30 && !isAchievementUnlocked(userId, "consistency_king")) {
                achievementsToUnlock.add("consistency_king")
            }

            // Unlock achievements
            if (achievementsToUnlock.isNotEmpty()) {
                achievementsToUnlock.forEach { achievementId ->
                    unlockAchievement(achievementId)
                }

                Log.d(TAG, "Unlocked ${achievementsToUnlock.size} achievements: $achievementsToUnlock")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking achievements", e)
            // Don't throw errors for achievement checking to avoid breaking other operations
        }
    }

    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    /**
     * Calculate streak data based on goal completion dates
     * Returns Pair<currentStreak, longestStreak>
     */
    private fun calculateStreakData(goals: List<Goal>): Pair<Int, Int> {
        val completedGoals = goals
            .filter { it.status == GoalStatus.COMPLETED }
            .sortedBy { it.createdAt }

        if (completedGoals.isEmpty()) {
            return Pair(0, 0)
        }

        // Convert timestamps to LocalDateTime for easier date manipulation
        val completionDates = completedGoals.map { goal ->
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(goal.createdAt),
                ZoneId.systemDefault()
            ).toLocalDate()
        }.distinct().sorted()

        if (completionDates.isEmpty()) {
            return Pair(0, 0)
        }

        // Calculate current streak (consecutive days from today backwards)
        val today = LocalDateTime.now().toLocalDate()
        var currentStreak = 0
        var checkDate = today

        // Check if there's a completion today or yesterday to start the streak
        if (completionDates.contains(today)) {
            currentStreak = 1
            checkDate = today.minusDays(1)
        } else if (completionDates.contains(today.minusDays(1))) {
            currentStreak = 1
            checkDate = today.minusDays(2)
        }

        // Continue counting backwards for consecutive days
        while (completionDates.contains(checkDate)) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        // Calculate longest streak
        var longestStreak = 0
        var tempStreak = 1

        for (i in 1 until completionDates.size) {
            val daysDiff = ChronoUnit.DAYS.between(completionDates[i - 1], completionDates[i])
            if (daysDiff == 1L) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
        }
        longestStreak = maxOf(longestStreak, tempStreak)

        return Pair(currentStreak, longestStreak)
    }

    /**
     * Calculate detailed monthly statistics from goal data
     */
    private fun calculateDetailedMonthlyStats(goals: List<Goal>): Map<String, MonthlyStats> {
        val monthlyStatsMap = mutableMapOf<String, MonthlyStats>()

        // Group goals by creation month
        val goalsByMonth = goals.groupBy { goal ->
            val date = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(goal.createdAt),
                ZoneId.systemDefault()
            )
            date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
        }

        // Calculate stats for each month
        goalsByMonth.forEach { (month, monthGoals) ->
            val totalGoals = monthGoals.size
            val completedGoals = monthGoals.count { it.status == GoalStatus.COMPLETED }
            val completionRate = if (totalGoals > 0) {
                (completedGoals.toFloat() / totalGoals.toFloat()) * 100f
            } else {
                0f
            }

            monthlyStatsMap[month] = MonthlyStats(
                month = month,
                goalsCreated = totalGoals,
                goalsCompleted = completedGoals,
                completionRate = completionRate
            )
        }

        // Ensure current month is included even if no goals
        val currentMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
        if (!monthlyStatsMap.containsKey(currentMonth)) {
            monthlyStatsMap[currentMonth] = MonthlyStats(
                month = currentMonth,
                goalsCreated = 0,
                goalsCompleted = 0,
                completionRate = 0f
            )
        }

        return monthlyStatsMap
    }

    private suspend fun getCurrentAchievements(userId: String): List<Achievement> {
        return try {
            val achievementsSnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACHIEVEMENTS_COLLECTION)
                .get()
                .await()

            val unlockedAchievementsMap = achievementsSnapshot.documents.associate { doc ->
                doc.id to doc.data
            }

            predefinedAchievements.map { achievement ->
                val unlockedData = unlockedAchievementsMap[achievement.id]
                if (unlockedData != null && unlockedData["is_unlocked"] == true) {
                    val unlockedTimestamp = unlockedData["unlocked_date"] as? com.google.firebase.Timestamp
                    val unlockedDate = unlockedTimestamp?.toDate()?.time

                    achievement.copy(
                        isUnlocked = true,
                        unlockedDate = unlockedDate
                    )
                } else {
                    achievement
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching achievements", e)
            predefinedAchievements
        }
    }

    private suspend fun isAchievementUnlocked(userId: String, achievementId: String): Boolean {
        return try {
            val achievementDoc = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(ACHIEVEMENTS_COLLECTION)
                .document(achievementId)
                .get()
                .await()

            achievementDoc.exists() && achievementDoc.getBoolean("is_unlocked") == true
        } catch (e: Exception) {
            false
        }
    }
}
