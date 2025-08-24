package io.sukhuat.dingo.domain.usecase.yearplanner

import io.sukhuat.dingo.domain.repository.YearPlannerRepository
import io.sukhuat.dingo.domain.validation.YearPlannerValidator
import javax.inject.Inject

/**
 * Use case for saving month content with validation
 */
class SaveMonthContentUseCase @Inject constructor(
    private val repository: YearPlannerRepository,
    private val validator: YearPlannerValidator
) {

    /**
     * Save content for a specific month
     * * @param year The year
     * @param monthIndex Month index (1-12)
     * @param content The content to save
     * @return Result with success/failure and error message
     */
    suspend operator fun invoke(
        year: Int,
        monthIndex: Int,
        content: String
    ): Result<Unit> {
        return try {
            // Validate inputs
            val validationResult = validator.validateMonthContent(year, monthIndex, content)
            if (!validationResult.isValid) {
                return Result.failure(
                    IllegalArgumentException(validationResult.errorMessage)
                )
            }

            // Save to repository
            val success = repository.updateMonthContent(year, monthIndex, content)

            if (success) {
                // Update last accessed year
                repository.updateLastAccessedYear(year)
                Result.success(Unit)
            } else {
                Result.failure(RuntimeException("Failed to save month content"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Result wrapper for validation and operation results
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String = ""
) {
    companion object {
        fun valid() = ValidationResult(true)
        fun invalid(message: String) = ValidationResult(false, message)
    }
}
