package io.sukhuat.dingo.domain.model

/**
 * Domain model representing user profile information
 */
data class UserProfile(
    val userId: String,
    val displayName: String,
    val email: String,
    val profileImageUrl: String? = null, // Custom uploaded image URL
    val googlePhotoUrl: String? = null, // Google profile photo URL
    val hasCustomImage: Boolean = false, // Flag indicating if user has uploaded custom image
    val lastImageUpdate: Long? = null, // Timestamp of last image update
    val joinDate: Long, // timestamp in milliseconds
    val isEmailVerified: Boolean = false,
    val authProvider: AuthProvider,
    val authCapabilities: AuthCapabilities = AuthCapabilities(), // Authentication capabilities
    val lastLoginDate: Long? = null // timestamp in milliseconds
)

/**
 * Authentication capabilities for the user account
 * Indicates what authentication methods are available and what operations can be performed
 */
data class AuthCapabilities(
    val hasGoogleAuth: Boolean = false, // User has Google Sign-In linked
    val hasPasswordAuth: Boolean = false, // User has email/password authentication
    val canChangePassword: Boolean = false // User can change password (requires email/password auth)
)

/**
 * Authentication provider types
 */
enum class AuthProvider {
    EMAIL_PASSWORD,
    GOOGLE,
    MULTIPLE, // User has both Google and Email/Password linked
    ANONYMOUS
}
