package io.sukhuat.dingo.data.mapper

import com.google.firebase.Timestamp
import io.sukhuat.dingo.data.model.FirebaseAchievement
import io.sukhuat.dingo.data.model.FirebaseMonthlyStats
import io.sukhuat.dingo.data.model.FirebaseProfileStatistics
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileStatistics
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Mapper for converting between domain ProfileStatistics and Firebase data models
 */
object ProfileStatisticsMapper {

    /**
     * Convert Firebase model to domain model
     */
    fun toDomain(firebaseStats: FirebaseProfileStatistics): ProfileStatistics {
        return ProfileStatistics(
            totalGoalsCreated = firebaseStats.totalGoalsCreated,
            completedGoals = firebaseStats.completedGoals,
            completionRate = firebaseStats.completionRate,
            currentStreak = firebaseStats.currentStreak,
            longestStreak = firebaseStats.longestStreak,
            monthlyStats = firebaseStats.monthlyStats.mapValues { (_, stats) ->
                stats.toDomain()
            },
            achievements = firebaseStats.achievements.values.map { it.toDomain() }
        )
    }

    /**
     * Convert domain model to Firebase model
     */
    fun toFirebase(profileStats: ProfileStatistics): FirebaseProfileStatistics {
        return FirebaseProfileStatistics(
            totalGoalsCreated = profileStats.totalGoalsCreated,
            completedGoals = profileStats.completedGoals,
            completionRate = profileStats.completionRate,
            currentStreak = profileStats.currentStreak,
            longestStreak = profileStats.longestStreak,
            monthlyStats = profileStats.monthlyStats.mapValues { (_, stats) ->
                stats.toFirebase()
            },
            achievements = profileStats.achievements.associateBy(
                keySelector = { it.id },
                valueTransform = { it.toFirebase() }
            )
        )
    }

    /**
     * Convert Firebase monthly stats to domain model
     */
    private fun FirebaseMonthlyStats.toDomain(): MonthlyStats {
        return MonthlyStats(
            month = this.month,
            goalsCreated = this.goalsCreated,
            goalsCompleted = this.goalsCompleted,
            completionRate = this.completionRate
        )
    }

    /**
     * Convert domain monthly stats to Firebase model
     */
    private fun MonthlyStats.toFirebase(): FirebaseMonthlyStats {
        return FirebaseMonthlyStats(
            month = this.month,
            goalsCreated = this.goalsCreated,
            goalsCompleted = this.goalsCompleted,
            completionRate = this.completionRate
        )
    }

    /**
     * Convert Firebase achievement to domain model
     */
    private fun FirebaseAchievement.toDomain(): Achievement {
        return Achievement(
            id = this.id,
            title = this.title,
            description = this.description,
            iconResId = this.iconResId,
            unlockedDate = this.unlockedDate?.toDate()?.time,
            isUnlocked = this.isUnlocked
        )
    }

    /**
     * Convert domain achievement to Firebase model
     */
    private fun Achievement.toFirebase(): FirebaseAchievement {
        return FirebaseAchievement(
            id = this.id,
            title = this.title,
            description = this.description,
            iconResId = this.iconResId,
            unlockedDate = this.unlockedDate?.let { Timestamp(java.util.Date(it)) },
            isUnlocked = this.isUnlocked
        )
    }

    /**
     * Convert Timestamp to LocalDateTime
     */
    private fun Timestamp.toLocalDateTime(): LocalDateTime {
        return LocalDateTime.ofInstant(
            this.toDate().toInstant(),
            ZoneId.systemDefault()
        )
    }

    /**
     * Convert LocalDateTime to Timestamp
     */
    private fun LocalDateTime.toTimestamp(): Timestamp {
        return Timestamp(
            java.util.Date.from(
                this.atZone(ZoneId.systemDefault()).toInstant()
            )
        )
    }
}
