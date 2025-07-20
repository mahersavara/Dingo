package io.sukhuat.dingo.domain.repository

import io.sukhuat.dingo.domain.model.ReferralData
import io.sukhuat.dingo.domain.model.SharingPrivacySettings
import io.sukhuat.dingo.domain.model.SharingStats
import io.sukhuat.dingo.domain.model.SocialPlatform
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for sharing functionality
 */
interface SharingRepository {

    /**
     * Get sharing privacy settings for the current user
     */
    suspend fun getSharingPrivacySettings(): Flow<SharingPrivacySettings>

    /**
     * Update sharing privacy settings
     */
    suspend fun updateSharingPrivacySettings(settings: SharingPrivacySettings)

    /**
     * Generate a shareable profile link
     */
    suspend fun generateProfileLink(userId: String): String

    /**
     * Get referral data for the current user
     */
    suspend fun getReferralData(): Flow<ReferralData>

    /**
     * Generate a new referral code
     */
    suspend fun generateReferralCode(): String

    /**
     * Track a sharing event for analytics
     */
    suspend fun trackSharingEvent(
        contentType: String,
        platform: SocialPlatform,
        achievementId: String? = null
    )

    /**
     * Get sharing statistics
     */
    suspend fun getSharingStats(): Flow<SharingStats>

    /**
     * Create a shareable image for achievements
     */
    suspend fun createShareableImage(
        achievementTitle: String,
        achievementDescription: String,
        userDisplayName: String
    ): String
}
