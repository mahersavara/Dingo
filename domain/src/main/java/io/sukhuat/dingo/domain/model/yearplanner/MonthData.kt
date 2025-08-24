package io.sukhuat.dingo.domain.model.yearplanner

/**
 * Domain model representing month data in a year planner
 */
data class MonthData(
    val index: Int, // 1-12 (January = 1, December = 12)
    val name: String, // "January", "February", etc.
    val content: String,
    val wordCount: Int,
    val lastModified: Long,
    val isPendingSync: Boolean = false
) {
    companion object {
        /**
         * Create empty month data for given index
         */
        fun createEmpty(monthIndex: Int): MonthData {
            val monthName = getMonthName(monthIndex)
            return MonthData(
                index = monthIndex,
                name = monthName,
                content = "",
                wordCount = 0,
                lastModified = System.currentTimeMillis(),
                isPendingSync = false
            )
        }

        /**
         * Get month name for given index (1-12)
         */
        private fun getMonthName(monthIndex: Int): String {
            return when (monthIndex) {
                1 -> "January"
                2 -> "February" 3 -> "March"
                4 -> "April"
                5 -> "May"
                6 -> "June"
                7 -> "July"
                8 -> "August"
                9 -> "September"
                10 -> "October"
                11 -> "November"
                12 -> "December"
                else -> throw IllegalArgumentException("Invalid month index: $monthIndex. Must be 1-12.")
            }
        }

        /**
         * Get all 12 months for a year with empty content
         */
        fun createEmptyYear(): List<MonthData> {
            return (1..12).map { monthIndex ->
                createEmpty(monthIndex)
            }
        }
    }

    /**
     * Update content and recalculate word count
     */
    fun updateContent(newContent: String): MonthData {
        return copy(
            content = newContent,
            wordCount = calculateWordCount(newContent),
            lastModified = System.currentTimeMillis(),
            isPendingSync = true
        )
    }

    /**
     * Mark as synced (remove pending sync flag)
     */
    fun markAsSynced(): MonthData {
        return copy(isPendingSync = false)
    }

    private fun calculateWordCount(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }
            .size
    }
}
