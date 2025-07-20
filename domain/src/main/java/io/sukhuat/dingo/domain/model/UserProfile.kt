package io.sukhuat.dingo.domain.model

/**
 * Domain model representing user profile information
 */
data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
    val profileImageUrl: String? = null,
    val joinDate: Long, // timestamp in milliseconds
    val isEmailVerified: Boolean = false,
    val authProvider: AuthProvider,
    val lastLoginDate: Long? = null // timestamp in milliseconds
)

/**
 * Authentication provider types
 */
enum class AuthProvider {
    EMAIL_PASSWORD,
    GOOGLE,
    ANONYMOUS
}
