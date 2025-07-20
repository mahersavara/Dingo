package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for deleting a goal
 */
class DeleteGoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String): Result<Boolean> {
        return try {
            val result = goalRepository.deleteGoal(goalId)
            if (result) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete goal"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
