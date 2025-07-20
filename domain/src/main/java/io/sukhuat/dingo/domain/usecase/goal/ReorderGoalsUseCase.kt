package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for reordering goals
 */
class ReorderGoalsUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(goalIds: List<String>): Result<Boolean> {
        return try {
            val result = goalRepository.reorderGoals(goalIds)
            if (result) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to reorder goals"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
