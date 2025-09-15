package io.sukhuat.dingo.widget

import io.sukhuat.dingo.widget.models.WidgetGoal
import javax.inject.Inject

/**
 * Simplified widget view model following favorite-apps-widget patterns
 *
 * Key improvements:
 * - Simple suspend function interface (like FavoriteAppWidgetViewModel)
 * - Direct dependency injection without complex abstractions
 * - Clean separation of concerns
 * - No complex state management or caching logic
 */
class SimplifiedWidgetViewModel @Inject constructor(
    private val dataLoader: SimplifiedWidgetDataLoader
) {

    /**
     * Get current week goals - primary method following reference pattern
     * Simple suspend function that widgets can call directly
     */
    suspend fun getCurrentWeekGoals(): List<WidgetGoal> {
        return dataLoader.loadCurrentWeekGoals()
    }

    /**
     * Get goals for specific week
     */
    suspend fun getGoalsForWeek(weekOfYear: Int, year: Int): List<WidgetGoal> {
        return dataLoader.loadGoalsForWeek(weekOfYear, year)
    }

    /**
     * Get goals with week offset for navigation
     */
    suspend fun getGoalsWithOffset(weekOffset: Int): List<WidgetGoal> {
        return dataLoader.loadGoalsWithOffset(weekOffset)
    }

    /**
     * Check if we have any goals (for empty state detection)
     */
    suspend fun hasAnyGoals(): Boolean {
        return dataLoader.hasAnyGoals()
    }

    /**
     * Get current week information
     */
    fun getCurrentWeekInfo(): Pair<Int, Int> {
        return dataLoader.getCurrentWeekInfo()
    }

    /**
     * Get total goal count for statistics
     */
    suspend fun getTotalGoalCount(): Int {
        return dataLoader.getTotalGoalCount()
    }
}
