package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.repository.GoalRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case to migrate old goals that don't have proper weekOfYear and yearCreated
 */
class MigrateGoalWeekDataUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    /**
     * Migrate all goals that have weekOfYear = -1 or yearCreated = -1
     * to have proper week/year based on their createdAt timestamp
     */
    suspend operator fun invoke(): Result<Int> {
        return try {
            val allGoals = goalRepository.getAllGoals().first()
            var migratedCount = 0

            allGoals.forEach { goal ->
                if (goal.weekOfYear == -1 || goal.yearCreated == -1) {
                    // Calculate correct week/year from createdAt
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = goal.createdAt
                    }

                    val correctWeekOfYear = calendar.get(Calendar.WEEK_OF_YEAR)
                    val correctYearCreated = calendar.get(Calendar.YEAR)

                    // Update the goal with correct week/year
                    val updatedGoal = goal.copy(
                        weekOfYear = correctWeekOfYear,
                        yearCreated = correctYearCreated
                    )

                    goalRepository.updateGoal(updatedGoal)
                    migratedCount++
                }
            }

            Result.success(migratedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
