package io.sukhuat.dingo.domain.model

/**
 * Shareable content for achievements and profile
 */
data class ShareableContent(
    val text: String,
    val hashtags: List<String>,
    val achievementTitle: String? = null,
    val achievementDescription: String? = null,
    val achievementIconResId: Int? = null,
    val profileLink: String? = null,
    val imageUri: String? = null
)

/**
 * Social media platforms for sharing
 */
enum class SocialPlatform {
    TWITTER,
    FACEBOOK,
    INSTAGRAM,
    LINKEDIN,
    WHATSAPP,
    GENERIC
}

/**
 * Statistics about achievement sharing
 */
data class SharingStats(
    val totalShares: Int,
    val mostSharedAchievement: String?,
    val platformBreakdown: Map<SocialPlatform, Int>
)

/**
 * Referral system data
 */
data class ReferralData(
    val referralCode: String,
    val referralLink: String,
    val totalInvites: Int,
    val successfulInvites: Int,
    val pendingInvites: Int
)

/**
 * Privacy settings for sharing features
 */
data class SharingPrivacySettings(
    val allowAchievementSharing: Boolean = true,
    val allowProfileSharing: Boolean = true,
    val allowReferralSharing: Boolean = true,
    val includeAppPromotion: Boolean = true,
    val shareWithRealName: Boolean = false
)

/**
 * Share intent data for Android sharing
 */
data class ShareIntentData(
    val text: String,
    val subject: String? = null,
    val imageUri: String? = null,
    val mimeType: String = "text/plain"
)
