package io.sukhuat.dingo.domain.repository

/**
 * Represents the result of an authentication operation
 * @param T The type of data contained in the result
 */
sealed class AuthResult<out T> {
    /**
     * Represents a successful authentication operation
     * @param data The data returned from the operation
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * Represents a failed authentication operation
     * @param message Error message
     * @param exception Optional exception that caused the failure
     */
    data class Error(val message: String, val exception: Exception? = null) : AuthResult<Nothing>()

    /**
     * Represents an authentication operation in progress
     */
    object Loading : AuthResult<Nothing>()
}

/**
 * Represents an authenticated user
 */
data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSignInAt: Long = System.currentTimeMillis()
)

/**
 * Represents the overall authentication state of the application
 */
data class AuthenticationState(
    val isAuthenticated: Boolean = false,
    val isEmailVerified: Boolean = false,
    val user: User? = null,
    val authMethod: AuthMethod = AuthMethod.NONE,
    val requiresAction: AuthAction? = null
)

/**
 * Authentication methods supported by the app
 */
enum class AuthMethod {
    NONE,
    EMAIL_PASSWORD,
    GOOGLE
}

/**
 * Actions that may be required from the user after authentication
 */
enum class AuthAction {
    VERIFY_EMAIL,
    COMPLETE_PROFILE,
    CHANGE_PASSWORD
}

/**
 * Password strength assessment result
 */
data class PasswordStrength(
    val score: Int = 0, // 0-4 (Very Weak to Very Strong)
    val feedback: List<String> = emptyList(),
    val isValid: Boolean = false,
    val requirements: PasswordRequirements = PasswordRequirements()
)

/**
 * Individual password requirement checks
 */
data class PasswordRequirements(
    val minLength: Boolean = false, // 8+ characters
    val hasUppercase: Boolean = false,
    val hasLowercase: Boolean = false,
    val hasNumber: Boolean = false,
    val hasSpecialChar: Boolean = false,
    val noCommonPatterns: Boolean = false
)

/**
 * Authentication error types for better error handling
 */
sealed class AuthError {
    object WeakPassword : AuthError()
    object EmailAlreadyExists : AuthError()
    object InvalidEmail : AuthError()
    object NetworkError : AuthError()
    object ExpiredToken : AuthError()
    object RateLimited : AuthError()
    object EmailNotVerified : AuthError()
    object PasswordMismatch : AuthError()
    object UnknownError : AuthError()
}
