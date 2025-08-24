package io.sukhuat.dingo.domain.validation

import io.sukhuat.dingo.domain.usecase.yearplanner.ValidationResult
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validator for Year Planner data and operations
 */
@Singleton
class YearPlannerValidator @Inject constructor() {

    companion object {
        // Content limits based on PRD performance requirements
        const val MAX_CONTENT_LENGTH = 50_000 // 50K characters per month
        const val MIN_YEAR = 1900 // Reasonable minimum year
        const val MAX_YEAR = 2200 // Far future planning
        const val MIN_MONTH_INDEX = 1
        const val MAX_MONTH_INDEX = 12
    }

    /**
     * Validate year value
     */
    fun validateYear(year: Int): ValidationResult {
        return when {
            year < MIN_YEAR -> ValidationResult.invalid(
                "Year must be at least $MIN_YEAR"
            )
            year > MAX_YEAR -> ValidationResult.invalid(
                "Year must be at most $MAX_YEAR"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Validate month index (1-12)
     */
    fun validateMonthIndex(monthIndex: Int): ValidationResult {
        return when {
            monthIndex < MIN_MONTH_INDEX -> ValidationResult.invalid(
                "Month index must be at least $MIN_MONTH_INDEX"
            )
            monthIndex > MAX_MONTH_INDEX -> ValidationResult.invalid(
                "Month index must be at most $MAX_MONTH_INDEX"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Validate month content
     */
    fun validateMonthContent(year: Int, monthIndex: Int, content: String): ValidationResult {
        // Validate year
        val yearValidation = validateYear(year)
        if (!yearValidation.isValid) return yearValidation

        // Validate month index
        val monthValidation = validateMonthIndex(monthIndex)
        if (!monthValidation.isValid) return monthValidation

        // Validate content length
        if (content.length > MAX_CONTENT_LENGTH) {
            return ValidationResult.invalid(
                "Content is too long. Maximum allowed: $MAX_CONTENT_LENGTH characters"
            )
        }

        // Content validation passed
        return ValidationResult.valid()
    }

    /**
     * Validate if year is reasonable for planning
     */
    fun validateYearForPlanning(year: Int): ValidationResult {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val yearValidation = validateYear(year)
        if (!yearValidation.isValid) return yearValidation

        return when {
            year < currentYear - 10 -> ValidationResult.invalid(
                "Year is too far in the past for active planning"
            )
            year > currentYear + 5 -> ValidationResult.invalid(
                "Year is too far in the future for detailed planning"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Validate user ID
     */
    fun validateUserId(userId: String): ValidationResult {
        return when {
            userId.isBlank() -> ValidationResult.invalid(
                "User ID cannot be empty"
            )
            userId.length < 3 -> ValidationResult.invalid(
                "User ID is too short"
            )
            userId.length > 128 -> ValidationResult.invalid(
                "User ID is too long"
            )
            else -> ValidationResult.valid()
        }
    }

    /**
     * Check if content has meaningful data (not just whitespace)
     */
    fun hasValidContent(content: String): Boolean {
        return content.trim().isNotEmpty()
    }

    /**
     * Estimate if content is getting close to limits (for UI warnings)
     */
    fun getContentLengthWarning(content: String): String? {
        val length = content.length
        val warningThreshold = (MAX_CONTENT_LENGTH * 0.8).toInt() // 80% of limit
        val criticalThreshold = (MAX_CONTENT_LENGTH * 0.95).toInt() // 95% of limit

        return when {
            length >= criticalThreshold -> "Content is nearly at maximum length"
            length >= warningThreshold -> "Content is getting long"
            else -> null
        }
    }
}
