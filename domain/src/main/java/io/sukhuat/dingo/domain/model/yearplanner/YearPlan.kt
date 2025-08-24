package io.sukhuat.dingo.domain.model.yearplanner

import java.util.Calendar

/**
 * Domain model representing a complete year plan with all 12 months
 */
data class YearPlan(
    val year: Int,
    val months: List<MonthData>,
    val syncStatus: SyncStatus,
    val lastSynced: Long,
    val userId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create a new empty year plan
         */
        fun createEmpty(year: Int, userId: String): YearPlan {
            val currentTime = System.currentTimeMillis()
            return YearPlan(
                year = year,
                months = MonthData.createEmptyYear(),
                syncStatus = SyncStatus.PENDING,
                lastSynced = 0L,
                userId = userId,
                createdAt = currentTime,
                updatedAt = currentTime
            )
        }

        /**
         * Create for current year
         */
        fun createForCurrentYear(userId: String): YearPlan {
            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
            return createEmpty(currentYear, userId)
        }
    }

    /**
     * Update content for a specific month
     */
    fun updateMonth(monthIndex: Int, content: String): YearPlan {
        require(monthIndex in 1..12) { "Month index must be between 1 and 12" }

        val updatedMonths = months.map { month ->
            if (month.index == monthIndex) {
                month.updateContent(content)
            } else {
                month
            }
        }

        return copy(
            months = updatedMonths,
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )
    }

    /**
     * Get month by index (1-12)
     */
    fun getMonth(monthIndex: Int): MonthData? {
        return months.find { it.index == monthIndex }
    }

    /**
     * Mark as synced with Firebase
     */
    fun markAsSynced(): YearPlan {
        val syncedMonths = months.map { it.markAsSynced() }
        return copy(
            months = syncedMonths,
            syncStatus = SyncStatus.SYNCED,
            lastSynced = System.currentTimeMillis()
        )
    }

    /**
     * Mark sync as failed
     */
    fun markSyncError(): YearPlan {
        return copy(syncStatus = SyncStatus.ERROR)
    }

    /**
     * Mark as offline (no connection)
     */
    fun markOffline(): YearPlan {
        return copy(syncStatus = SyncStatus.OFFLINE)
    }

    /**
     * Calculate total statistics for the year
     */
    fun getStatistics(): YearPlanStatistics {
        val totalWords = months.sumOf { it.wordCount }
        val monthsWithContent = months.count { it.content.isNotBlank() }
        val pendingSync = months.count { it.isPendingSync }

        return YearPlanStatistics(
            totalWords = totalWords,
            monthsWithContent = monthsWithContent,
            totalMonths = 12,
            pendingSync = pendingSync,
            lastActivity = months.maxOfOrNull { it.lastModified } ?: updatedAt
        )
    }
}

/**
 * Statistics for a year plan
 */
data class YearPlanStatistics(
    val totalWords: Int,
    val monthsWithContent: Int,
    val totalMonths: Int,
    val pendingSync: Int,
    val lastActivity: Long
) {
    val completionPercentage: Float
        get() = if (totalMonths > 0) (monthsWithContent.toFloat() / totalMonths) * 100f else 0f
}
