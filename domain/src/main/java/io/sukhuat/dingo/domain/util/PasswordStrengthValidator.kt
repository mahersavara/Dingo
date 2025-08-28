package io.sukhuat.dingo.domain.util

import io.sukhuat.dingo.domain.repository.PasswordRequirements
import io.sukhuat.dingo.domain.repository.PasswordStrength

/**
 * Utility class for validating password strength with real-time feedback
 * Implements the requirements from the PRD specification
 */
object PasswordStrengthValidator {

    private const val MIN_LENGTH = 8
    private const val STRONG_LENGTH = 12
    private const val VERY_STRONG_LENGTH = 16

    // Common passwords to detect (can be expanded)
    private val COMMON_PASSWORDS = setOf(
        "password", "123456", "password123", "admin", "qwerty", "matkhau", "123456789", "12345678", "letmein", "welcome",
        "monkey", "1234567890", "dragon", "123123", "football",
        "iloveyou", "admin123", "welcome123", "sunshine", "princess"
    )

    // Sequential patterns to detect
    private val SEQUENTIAL_PATTERNS = listOf(
        "123", "234", "345", "456", "567", "678", "789", "890",
        "abc", "bcd", "cde", "def", "efg", "fgh", "ghi", "hij",
        "ijk", "jkl", "klm", "lmn", "mno", "nop", "opq", "pqr",
        "qrs", "rst", "stu", "tuv", "uvw", "vwx", "wxy", "xyz"
    )

    /**
     * Validates password strength and returns comprehensive assessment
     * @param password Password to validate
     * @return PasswordStrength object with score, feedback, and requirements
     */
    fun validatePassword(password: String): PasswordStrength {
        val requirements = checkRequirements(password)
        val score = calculateScore(requirements, password)
        val feedback = generateFeedback(requirements, password)
        val isValid = score >= 3 && requirements.minLength

        return PasswordStrength(
            score = score,
            feedback = feedback,
            isValid = isValid,
            requirements = requirements
        )
    }

    /**
     * Checks individual password requirements
     */
    private fun checkRequirements(password: String): PasswordRequirements {
        return PasswordRequirements(
            minLength = password.length >= MIN_LENGTH,
            hasUppercase = password.any { it.isUpperCase() },
            hasLowercase = password.any { it.isLowerCase() },
            hasNumber = password.any { it.isDigit() },
            hasSpecialChar = password.any { !it.isLetterOrDigit() },
            noCommonPatterns = !hasCommonPatterns(password)
        )
    }

    /**
     * Calculates password strength score (0-4)
     */
    private fun calculateScore(requirements: PasswordRequirements, password: String): Int {
        var score = 0

        // Basic requirements (1 point each)
        if (requirements.minLength) score++
        if (requirements.hasUppercase && requirements.hasLowercase) score++
        if (requirements.hasNumber) score++
        if (requirements.hasSpecialChar) score++

        // Bonus points for length and avoiding common patterns
        if (password.length >= STRONG_LENGTH) score++
        if (password.length >= VERY_STRONG_LENGTH) score++
        if (requirements.noCommonPatterns) score++

        // Cap at 4 and apply minimum requirements
        score = minOf(4, score)

        // If basic requirements not met, cap score
        val basicRequirementsMet = listOf(
            requirements.minLength,
            requirements.hasUppercase || requirements.hasLowercase,
            requirements.hasNumber || requirements.hasSpecialChar
        ).count { it }

        if (basicRequirementsMet < 2) score = minOf(1, score)
        if (!requirements.minLength) score = 0

        return score
    }

    /**
     * Generates user-friendly feedback messages
     */
    private fun generateFeedback(requirements: PasswordRequirements, password: String): List<String> {
        val feedback = mutableListOf<String>()

        if (!requirements.minLength) {
            feedback.add("Use at least $MIN_LENGTH characters")
        }

        if (!requirements.hasUppercase) {
            feedback.add("Add uppercase letters (A-Z)")
        }

        if (!requirements.hasLowercase) {
            feedback.add("Add lowercase letters (a-z)")
        }

        if (!requirements.hasNumber) {
            feedback.add("Add numbers (0-9)")
        }

        if (!requirements.hasSpecialChar) {
            feedback.add("Add special characters (!@#$%^&*)")
        }

        if (!requirements.noCommonPatterns) {
            feedback.add("Avoid common passwords and patterns")
        }

        // Positive feedback for strong passwords
        if (feedback.isEmpty()) {
            when (calculateScore(requirements, password)) {
                4 -> feedback.add("Excellent password strength!")
                3 -> feedback.add("Good password strength")
                else -> feedback.add("Password meets basic requirements")
            }
        }

        return feedback
    }

    /**
     * Checks for common patterns in password
     */
    private fun hasCommonPatterns(password: String): Boolean {
        val lowercasePassword = password.lowercase()

        // Check against common passwords
        if (COMMON_PASSWORDS.contains(lowercasePassword)) {
            return true
        }

        // Check for sequential patterns
        if (SEQUENTIAL_PATTERNS.any { lowercasePassword.contains(it) }) {
            return true
        }

        // Check for repeated characters (more than 3 in a row)
        if (password.windowed(4).any { window ->
            window.all { it == window.first() }
        }
        ) {
            return true
        }

        // Check for keyboard patterns (basic)
        val keyboardPatterns = listOf("qwerty", "asdf", "zxcv", "1234", "abcd")
        if (keyboardPatterns.any { lowercasePassword.contains(it) }) {
            return true
        }

        return false
    }

    /**
     * Gets password strength level as readable string
     */
    fun getStrengthLevel(score: Int): String {
        return when (score) {
            0 -> "Very Weak"
            1 -> "Weak" 2 -> "Fair"
            3 -> "Strong"
            4 -> "Very Strong"
            else -> "Unknown"
        }
    }

    /**
     * Gets color recommendation for UI display
     */
    fun getStrengthColor(score: Int): String {
        return when (score) {
            0 -> "#F44336" // Red
            1 -> "#FF9800" // Orange
            2 -> "#FFC107" // Amber
            3 -> "#8BC34A" // Light Green
            4 -> "#4CAF50" // Green
            else -> "#9E9E9E" // Grey
        }
    }
}
