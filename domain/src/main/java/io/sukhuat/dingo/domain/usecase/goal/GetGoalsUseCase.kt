package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for retrieving all goals
 */
class GetGoalsUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    operator fun invoke(): Flow<List<Goal>> {
        return goalRepository.getAllGoals()
    }

    /**
     * Get goals for a specific week offset relative to current week
     * @param weekOffset 0 = current week, -1 = previous week, etc.
     */
    fun getGoalsForWeek(weekOffset: Int): Flow<List<Goal>> {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.WEEK_OF_YEAR, weekOffset)
        }
        val targetWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val targetYear = calendar.get(Calendar.YEAR)

        return goalRepository.getAllGoals().map { goals ->
            goals.filter { goal ->
                goal.weekOfYear == targetWeek && goal.yearCreated == targetYear
            }
        }
    }

    /**
     * Get all weeks that have goals
     * Returns a list of week offsets relative to current week
     */
    fun getWeeksWithGoals(): Flow<List<Int>> {
        val currentCalendar = Calendar.getInstance()
        val currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = currentCalendar.get(Calendar.YEAR)

        return goalRepository.getAllGoals().map { goals ->
            goals.map { goal ->
                // Calculate week offset relative to current week
                val goalCalendar = Calendar.getInstance().apply {
                    set(Calendar.WEEK_OF_YEAR, goal.weekOfYear)
                    set(Calendar.YEAR, goal.yearCreated)
                }
                val currentCalendarClone = Calendar.getInstance().apply {
                    set(Calendar.WEEK_OF_YEAR, currentWeek)
                    set(Calendar.YEAR, currentYear)
                }

                val weeksDiff = ((goalCalendar.timeInMillis - currentCalendarClone.timeInMillis) / (1000 * 60 * 60 * 24 * 7)).toInt()
                weeksDiff
            }.distinct().sorted()
        }
    }

    /**
     * Clears all goals from the local database
     * This should be called when a user logs out or switches accounts
     */
    suspend fun clearAllGoals() {
        goalRepository.clearAllGoals()
    }
}
