package io.sukhuat.dingo.domain.usecase.goal

import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
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
     * Clears all goals from the local database
     * This should be called when a user logs out or switches accounts
     */
    suspend fun clearAllGoals() {
        goalRepository.clearAllGoals()
    }
} 