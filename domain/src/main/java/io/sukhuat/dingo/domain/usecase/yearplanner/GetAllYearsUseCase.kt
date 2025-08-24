package io.sukhuat.dingo.domain.usecase.yearplanner

import io.sukhuat.dingo.domain.repository.YearPlannerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

/**
 * Use case for getting all available years
 */
class GetAllYearsUseCase @Inject constructor(
    private val repository: YearPlannerRepository
) {

    /**
     * Get all years with year plans, ensuring current year is always included
     * Returns years sorted in descending order (newest first)
     * * @return Flow of year list
     */
    operator fun invoke(): Flow<List<Int>> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        return repository.getAllYears().map { existingYears ->
            val allYears = existingYears.toMutableSet()
            // Always include current year even if no data exists yet
            allYears.add(currentYear)

            // Sort in descending order (newest first)
            allYears.sortedDescending()
        }
    }

    /**
     * Get years with actual content (excluding empty years)
     * * @return Flow of years that have content
     */
    fun getYearsWithContent(): Flow<List<Int>> {
        return repository.getAllYears().map { years ->
            years.sortedDescending()
        }
    }

    /**
     * Get suggested years to show in navigation
     * Includes current year, last year, and next year for planning
     * * @return Flow of suggested years
     */
    fun getSuggestedYears(): Flow<List<Int>> {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val suggestedYears = listOf(
            currentYear + 1, // Next year for planning
            currentYear, // Current year
            currentYear - 1 // Last year for reference
        )

        return repository.getAllYears().map { existingYears ->
            val allYears = existingYears.toMutableSet()
            allYears.addAll(suggestedYears)
            allYears.sortedDescending()
        }
    }
}
