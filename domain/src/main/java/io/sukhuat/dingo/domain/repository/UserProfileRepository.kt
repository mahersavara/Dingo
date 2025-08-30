package io.sukhuat.dingo.domain.repository

import android.net.Uri
import io.sukhuat.dingo.domain.model.AuthCapabilities
import io.sukhuat.dingo.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user profile data
 */
interface UserProfileRepository {
    /**
     * Get user profile as a Flow for real-time updates
     */
    suspend fun getUserProfile(): Flow<UserProfile>

    /**
     * Update user's display name
     */
    suspend fun updateDisplayName(name: String)

    /**
     * Update user's profile image
     * @param imageUri URI of the image to upload
     * @return URL of the uploaded image
     */
    suspend fun updateProfileImage(imageUri: Uri): String

    /**
     * Delete user's profile image
     */
    suspend fun deleteProfileImage()

    /**
     * Update profile with Google photo URL
     * @param photoUrl Google photo URL to set, or null to remove
     */
    suspend fun updateGooglePhotoUrl(photoUrl: String?)

    /**
     * Get auth capabilities for current user
     * @return AuthCapabilities indicating available authentication methods
     */
    suspend fun getAuthCapabilities(): AuthCapabilities

    /**
     * Export user data for GDPR compliance
     * @return JSON string containing user data
     */
    suspend fun exportUserData(): String

    /**
     * Delete user account and all associated data
     */
    suspend fun deleteUserAccount()

    /**
     * Get user's login history for security tracking
     */
    suspend fun getLoginHistory(): List<LoginRecord>

    /**
     * Change user password (for email/password accounts)
     * @param currentPassword Current password for verification
     * @param newPassword New password to set
     */
    suspend fun changePassword(currentPassword: String, newPassword: String)
}

/**
 * Login record for security tracking
 */
data class LoginRecord(
    val timestamp: Long,
    val deviceInfo: String,
    val ipAddress: String? = null,
    val location: String? = null
)
