package io.sukhuat.dingo.domain.model

/**
 * Domain model representing user profile statistics and achievements
 */
data class ProfileStatistics(
    val totalGoalsCreated: Int = 0,
    val completedGoals: Int = 0,
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val monthlyStats: Map<String, MonthlyStats> = emptyMap(),
    val achievements: List<Achievement> = emptyList()
)

/**
 * Monthly statistics breakdown
 */
data class MonthlyStats(
    val month: String,
    val goalsCreated: Int = 0,
    val goalsCompleted: Int = 0,
    val completionRate: Float = 0f
)

/**
 * Achievement model for gamification
 */
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconResId: Int,
    val unlockedDate: Long? = null, // timestamp in milliseconds
    val isUnlocked: Boolean = false
)
