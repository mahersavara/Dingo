package io.sukhuat.dingo.data.mapper

import io.sukhuat.dingo.data.model.FirebaseMonthPlan
import io.sukhuat.dingo.data.model.FirebaseYearPlan
import io.sukhuat.dingo.data.model.YearPlanMetadata
import io.sukhuat.dingo.domain.model.yearplanner.MonthData
import io.sukhuat.dingo.domain.model.yearplanner.SyncStatus
import io.sukhuat.dingo.domain.model.yearplanner.YearPlan

/**
 * Mapper between Firebase models and domain models for Year Planner
 * Follows existing mapper patterns in the project
 */
object YearPlannerMapper {

    /**
     * Convert Firebase year plan to domain model
     */
    fun FirebaseYearPlan.toDomain(): YearPlan {
        // Convert month map to sorted list
        val monthsList = (1..12).map { monthIndex ->
            val monthKey = monthIndex.toString()
            val firebaseMonth = months[monthKey] ?: FirebaseMonthPlan(
                monthIndex = monthIndex,
                content = "",
                lastModified = System.currentTimeMillis(),
                wordCount = 0
            )
            firebaseMonth.toDomain()
        }

        return YearPlan(
            year = year,
            months = monthsList,
            syncStatus = SyncStatus.SYNCED, // From Firebase = synced
            lastSynced = updatedAt,
            userId = userId,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    /**
     * Convert domain year plan to Firebase model
     */
    fun YearPlan.toFirebase(): FirebaseYearPlan {
        val monthsMap = months.associate { month ->
            month.index.toString() to month.toFirebase()
        }

        val stats = getStatistics()
        val metadata = YearPlanMetadata(
            totalEntries = stats.monthsWithContent,
            lastAccessedMonth = months.maxByOrNull { it.lastModified }?.index ?: 1,
            theme = "vintage"
        )

        return FirebaseYearPlan(
            userId = userId,
            year = year,
            createdAt = createdAt,
            updatedAt = updatedAt,
            months = monthsMap,
            metadata = metadata
        )
    }

    /**
     * Convert Firebase month plan to domain model
     */
    private fun FirebaseMonthPlan.toDomain(): MonthData {
        val monthName = getMonthName(monthIndex)
        return MonthData(
            index = monthIndex,
            name = monthName,
            content = content,
            wordCount = wordCount,
            lastModified = lastModified,
            isPendingSync = false // From Firebase = not pending
        )
    }

    /**
     * Convert domain month to Firebase model
     */
    private fun MonthData.toFirebase(): FirebaseMonthPlan {
        return FirebaseMonthPlan(
            monthIndex = index,
            content = content,
            lastModified = lastModified,
            wordCount = wordCount
        )
    }

    /**
     * Get month name for given index (1-12)
     */
    private fun getMonthName(monthIndex: Int): String {
        return when (monthIndex) {
            1 -> "January"
            2 -> "February"
            3 -> "March"
            4 -> "April"
            5 -> "May"
            6 -> "June"
            7 -> "July"
            8 -> "August"
            9 -> "September"
            10 -> "October"
            11 -> "November"
            12 -> "December"
            else -> "Unknown"
        }
    }

    /**
     * Create empty Firebase year plan
     */
    fun createEmptyFirebaseYearPlan(year: Int, userId: String): FirebaseYearPlan {
        val currentTime = System.currentTimeMillis()
        val emptyMonths = (1..12).associate { monthIndex ->
            monthIndex.toString() to FirebaseMonthPlan(
                monthIndex = monthIndex,
                content = "",
                lastModified = currentTime,
                wordCount = 0
            )
        }

        return FirebaseYearPlan(
            userId = userId,
            year = year,
            createdAt = currentTime,
            updatedAt = currentTime,
            months = emptyMonths,
            metadata = YearPlanMetadata(
                totalEntries = 0,
                lastAccessedMonth = 1,
                theme = "vintage"
            )
        )
    }
}
