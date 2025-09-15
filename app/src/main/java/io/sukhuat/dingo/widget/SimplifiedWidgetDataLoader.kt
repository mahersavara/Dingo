package io.sukhuat.dingo.widget

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.widget.models.WidgetGoal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simplified widget data loader following favorite-apps-widget patterns
 *
 * Key improvements based on reference implementation:
 * - Direct repository access without complex caching layers
 * - Simple suspend functions for data loading
 * - Clean error handling without circuit breakers
 * - Room database as single source of truth
 */
@Singleton
class SimplifiedWidgetDataLoader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val goalRepository: GoalRepository
) {

    /**
     * Load goals for specific week - following favorite-apps-widget pattern
     * Simple, direct data access without complex caching
     */
    suspend fun loadGoalsForWeek(
        weekOfYear: Int,
        year: Int
    ): List<WidgetGoal> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SimplifiedWidgetDataLoader", "📦 Loading goals for week $weekOfYear/$year")

            // Direct repository access - Room database is our cache
            val allGoals = goalRepository.getAllGoalsSync()
            val goals = allGoals
                .filter { goal ->
                    goal.weekOfYear == weekOfYear && goal.yearCreated == year
                }
                .map { goal ->
                    WidgetGoal(
                        id = goal.id,
                        text = goal.text,
                        imageResId = goal.imageResId,
                        customImage = goal.customImage,
                        status = goal.status,
                        weekOfYear = goal.weekOfYear,
                        yearCreated = goal.yearCreated,
                        position = goal.position
                    )
                }
                .sortedBy { it.position }

            android.util.Log.d("SimplifiedWidgetDataLoader", "✅ Loaded ${goals.size} goals for week $weekOfYear/$year")
            goals
        } catch (e: Exception) {
            android.util.Log.e("SimplifiedWidgetDataLoader", "❌ Error loading goals for week $weekOfYear/$year", e)
            // Return empty list on error - Room database will handle offline scenarios
            emptyList()
        }
    }

    /**
     * Load current week goals - simple and direct
     */
    suspend fun loadCurrentWeekGoals(): List<WidgetGoal> {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        return loadGoalsForWeek(currentWeek, currentYear)
    }

    /**
     * Load goals with week offset (for navigation)
     */
    suspend fun loadGoalsWithOffset(weekOffset: Int): List<WidgetGoal> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)

        val targetWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        return loadGoalsForWeek(targetWeek, targetYear)
    }

    /**
     * Get current week information
     */
    fun getCurrentWeekInfo(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(
            calendar.get(Calendar.WEEK_OF_YEAR),
            calendar.get(Calendar.YEAR)
        )
    }

    /**
     * Check if we have any goals data (for empty state detection)
     */
    suspend fun hasAnyGoals(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Quick check if we have any goals at all
            val allGoals = goalRepository.getAllGoalsSync()
            allGoals.isNotEmpty()
        } catch (e: Exception) {
            android.util.Log.e("SimplifiedWidgetDataLoader", "❌ Error checking for goals", e)
            false
        }
    }

    /**
     * Get total goal count for statistics
     */
    suspend fun getTotalGoalCount(): Int = withContext(Dispatchers.IO) {
        try {
            goalRepository.getAllGoalsSync().size
        } catch (e: Exception) {
            android.util.Log.e("SimplifiedWidgetDataLoader", "❌ Error getting goal count", e)
            0
        }
    }
}
