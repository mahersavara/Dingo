package io.sukhuat.dingo.domain.usecase.yearplanner

import io.sukhuat.dingo.domain.model.yearplanner.YearPlan
import io.sukhuat.dingo.domain.repository.YearPlannerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for loading year plan data
 * Follows existing use case patterns in the project
 */
class LoadYearPlanUseCase @Inject constructor(
    private val repository: YearPlannerRepository
) {

    /**
     * Load year plan for specific year
     * Creates empty year plan if it doesn't exist
     * * @param year The year to load
     * @param userId The current user ID
     * @return Flow of YearPlan (never null)
     */
    operator fun invoke(year: Int, userId: String): Flow<YearPlan> {
        return repository.getYearPlan(year).map { yearPlan ->
            yearPlan ?: YearPlan.createEmpty(year, userId)
        }
    }

    /**
     * Load year plan with automatic user ID resolution
     * Note: In actual implementation, user ID would be retrieved from auth
     * * @param year The year to load
     * @return Flow of YearPlan (never null)
     */
    operator fun invoke(year: Int): Flow<YearPlan> {
        // TODO: Get userId from auth service when implementing data layer
        return invoke(year, "current_user")
    }
}
