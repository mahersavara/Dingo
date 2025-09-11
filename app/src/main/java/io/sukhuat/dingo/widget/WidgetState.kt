package io.sukhuat.dingo.widget

import androidx.datastore.preferences.core.intPreferencesKey

/**
 * Widget state management for week navigation
 */
object WidgetState {
    val WIDGET_WEEK_OFFSET = intPreferencesKey("widget_week_offset")

    /**
     * Get the target week based on current week and offset
     * Offset 0 = current week, -1 = 1 week ago, etc.
     */
    fun getTargetWeek(weekOffset: Int): Pair<Int, Int> {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.WEEK_OF_YEAR, weekOffset)

        return Pair(
            calendar.get(java.util.Calendar.WEEK_OF_YEAR),
            calendar.get(java.util.Calendar.YEAR)
        )
    }
}
