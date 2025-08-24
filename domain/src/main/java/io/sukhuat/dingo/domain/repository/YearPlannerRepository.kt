package io.sukhuat.dingo.domain.repository

import io.sukhuat.dingo.domain.model.yearplanner.YearPlan
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Year Planner data operations
 * Follows the existing repository pattern in the project
 */
interface YearPlannerRepository {

    /**
     * Get year plan for specific year
     * Returns null if year plan doesn't exist
     */
    fun getYearPlan(year: Int): Flow<YearPlan?>

    /**
     * Get all available years that have year plans
     * Returns list sorted in descending order (newest first)
     */
    fun getAllYears(): Flow<List<Int>>

    /**
     * Create or update a complete year plan
     * @param yearPlan The year plan to save
     * @return Success/failure boolean
     */
    suspend fun saveYearPlan(yearPlan: YearPlan): Boolean

    /**
     * Update content for a specific month
     * @param year The year
     * @param monthIndex Month index (1-12)
     * @param content The new content
     * @return Success/failure boolean
     */
    suspend fun updateMonthContent(year: Int, monthIndex: Int, content: String): Boolean

    /**
     * Delete year plan completely
     * @param year The year to delete
     * @return Success/failure boolean
     */
    suspend fun deleteYearPlan(year: Int): Boolean

    /**
     * Check if year plan exists
     * @param year The year to check
     * @return Boolean indicating existence
     */
    suspend fun yearPlanExists(year: Int): Boolean

    /**
     * Get the most recently accessed year
     * @return Year number or null if no year plans exist
     */
    suspend fun getLastAccessedYear(): Int?

    /**
     * Update last accessed year
     * @param year The year that was accessed
     */
    suspend fun updateLastAccessedYear(year: Int)
}
