package io.sukhuat.dingo.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

/**
 * Firebase data model for profile statistics
 */
data class FirebaseProfileStatistics(
    @PropertyName("total_goals_created")
    val totalGoalsCreated: Int = 0,

    @PropertyName("completed_goals")
    val completedGoals: Int = 0,

    @PropertyName("completion_rate")
    val completionRate: Float = 0f,

    @PropertyName("current_streak")
    val currentStreak: Int = 0,

    @PropertyName("longest_streak")
    val longestStreak: Int = 0,

    @PropertyName("monthly_stats")
    val monthlyStats: Map<String, FirebaseMonthlyStats> = emptyMap(),

    @PropertyName("achievements")
    val achievements: Map<String, FirebaseAchievement> = emptyMap()
)

/**
 * Firebase data model for monthly statistics
 */
data class FirebaseMonthlyStats(
    @PropertyName("month")
    val month: String = "",

    @PropertyName("goals_created")
    val goalsCreated: Int = 0,

    @PropertyName("goals_completed")
    val goalsCompleted: Int = 0,

    @PropertyName("completion_rate")
    val completionRate: Float = 0f
)

/**
 * Firebase data model for achievements
 */
data class FirebaseAchievement(
    @PropertyName("id")
    val id: String = "",

    @PropertyName("title")
    val title: String = "",

    @PropertyName("description")
    val description: String = "",

    @PropertyName("icon_res_id")
    val iconResId: Int = 0,

    @PropertyName("unlocked_date")
    val unlockedDate: Timestamp? = null,

    @PropertyName("is_unlocked")
    val isUnlocked: Boolean = false
)
