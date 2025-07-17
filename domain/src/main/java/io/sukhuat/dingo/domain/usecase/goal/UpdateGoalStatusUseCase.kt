package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.model.GoalStatus
import io.sukhuat.dingo.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for updating the status of a goal
 */
class UpdateGoalStatusUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, status: GoalStatus): Result<Boolean> {
        return try {
            val result = goalRepository.updateGoalStatus(goalId, status)
            if (result) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update goal status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}