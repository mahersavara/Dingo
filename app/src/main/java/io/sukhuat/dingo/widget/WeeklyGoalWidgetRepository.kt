package io.sukhuat.dingo.widget

import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.widget.models.WidgetGoal
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject

/**
 * Repository for widget-specific goal data operations
 */
class WeeklyGoalWidgetRepository @Inject constructor(
    private val goalRepository: GoalRepository
) {

    /**
     * Get goals for a specific week and year
     */
    suspend fun getGoalsForWeek(weekOfYear: Int, year: Int): List<WidgetGoal> {
        return try {
            android.util.Log.d("WeeklyGoalWidgetRepository", "📦 Loading goals for week $weekOfYear/$year using sync method")

            val allGoals = goalRepository.getAllGoalsSync()
            val filteredGoals = allGoals.filter { goal ->
                goal.weekOfYear == weekOfYear && goal.yearCreated == year
            }.sortedBy { it.position }
                .take(6) // Max 6 goals for widget
                .map { goal ->
                    goal.toWidgetGoal()
                }

            android.util.Log.d("WeeklyGoalWidgetRepository", "✅ Loaded ${filteredGoals.size} goals for week $weekOfYear/$year")
            filteredGoals
        } catch (e: Exception) {
            android.util.Log.e("WeeklyGoalWidgetRepository", "❌ Error loading goals for week $weekOfYear/$year", e)
            emptyList()
        }
    }

    /**
     * Get goals for current week
     */
    suspend fun getCurrentWeekGoals(): List<WidgetGoal> {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        return getGoalsForWeek(currentWeek, currentYear)
    }

    /**
     * Get goals for a previous week (up to 4 weeks back)
     */
    suspend fun getPreviousWeekGoals(weeksBack: Int): List<WidgetGoal> {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.WEEK_OF_YEAR, -weeksBack)

        val targetWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        return getGoalsForWeek(targetWeek, targetYear)
    }

    /**
     * Get current week and year information
     */
    fun getCurrentWeekInfo(): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        return Pair(
            calendar.get(Calendar.WEEK_OF_YEAR),
            calendar.get(Calendar.YEAR)
        )
    }
}

/**
 * Convert Domain Goal to WidgetGoal
 */
private fun Goal.toWidgetGoal(): WidgetGoal {
    return WidgetGoal(
        id = this.id,
        text = this.text,
        imageResId = this.imageResId,
        customImage = this.customImage,
        status = this.status,
        weekOfYear = this.weekOfYear,
        yearCreated = this.yearCreated,
        position = this.position
    )
}
