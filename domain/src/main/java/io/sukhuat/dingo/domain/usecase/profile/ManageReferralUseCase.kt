package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.ReferralData
import io.sukhuat.dingo.domain.model.ShareableContent
import io.sukhuat.dingo.domain.model.SocialPlatform
import io.sukhuat.dingo.domain.repository.SharingRepository
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for managing referral system and invitations
 */
class ManageReferralUseCase @Inject constructor(
    private val sharingRepository: SharingRepository,
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * Get current user's referral data
     */
    suspend fun getReferralData(): Flow<ReferralData> {
        return sharingRepository.getReferralData()
    }

    /**
     * Generate a new referral code for the user
     */
    suspend fun generateNewReferralCode(): String {
        return sharingRepository.generateReferralCode()
    }

    /**
     * Create shareable referral content
     * * @param personalMessage Optional personal message to include
     * @param includeIncentive Whether to mention any referral incentives
     * @return Shareable content for referral invitation
     */
    suspend fun createReferralShareContent(
        personalMessage: String? = null,
        includeIncentive: Boolean = true
    ): ShareableContent {
        val userProfile = userProfileRepository.getUserProfile().first()
        val referralData = sharingRepository.getReferralData().first()

        val shareText = generateReferralText(
            userDisplayName = userProfile.displayName,
            personalMessage = personalMessage,
            includeIncentive = includeIncentive
        )

        val hashtags = generateReferralHashtags()

        return ShareableContent(
            text = shareText,
            hashtags = hashtags,
            profileLink = referralData.referralLink
        )
    }

    /**
     * Generate platform-specific referral content
     */
    suspend fun generatePlatformSpecificReferralContent(
        platform: SocialPlatform,
        personalMessage: String? = null,
        includeIncentive: Boolean = true
    ): ShareableContent {
        val baseContent = createReferralShareContent(personalMessage, includeIncentive)

        return when (platform) {
            SocialPlatform.TWITTER -> baseContent.copy(
                text = truncateForTwitter(baseContent.text),
                hashtags = baseContent.hashtags.take(2)
            )
            SocialPlatform.FACEBOOK -> baseContent.copy(
                text = baseContent.text + "\n\n" + baseContent.hashtags.joinToString(" ")
            )
            SocialPlatform.INSTAGRAM -> baseContent.copy(
                text = baseContent.text + "\n\n" + baseContent.hashtags.joinToString(" ")
            )
            SocialPlatform.WHATSAPP -> baseContent.copy(
                text = baseContent.text + "\n\nJoin here: ${baseContent.profileLink}",
                hashtags = emptyList() // WhatsApp doesn't use hashtags
            )
            SocialPlatform.LINKEDIN -> baseContent.copy(
                text = makeLinkedInFriendly(baseContent.text),
                hashtags = baseContent.hashtags.take(3)
            )
            SocialPlatform.GENERIC -> baseContent
        }
    }

    /**
     * Track a referral invitation sent
     */
    suspend fun trackReferralInvitation(platform: SocialPlatform) {
        sharingRepository.trackSharingEvent(
            contentType = "referral_invitation",
            platform = platform
        )
    }

    private fun generateReferralText(
        userDisplayName: String,
        personalMessage: String?,
        includeIncentive: Boolean
    ): String {
        val displayName = userDisplayName.ifEmpty { "I" }

        var text = if (personalMessage != null) {
            "$personalMessage\n\n"
        } else {
            ""
        }

        text += "🎯 $displayName have been using Dingo to achieve my goals and thought you might like it too!"
        text += "\n\nIt's a simple, beautiful app that makes goal-setting actually fun and achievable."

        if (includeIncentive) {
            text += "\n\n🎁 Plus, we both get special rewards when you join!"
        }

        text += "\n\nWould love to have you on this journey with me! 🚀"

        return text
    }

    private fun generateReferralHashtags(): List<String> {
        return listOf(
            "#GoalSetting",
            "#PersonalGrowth",
            "#DingoApp",
            "#JoinMe"
        )
    }

    private fun truncateForTwitter(text: String, maxLength: Int = 240): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - 3) + "..."
        }
    }

    private fun makeLinkedInFriendly(text: String): String {
        return text.replace("🎯", "").replace("🚀", "").replace("🎁", "")
            .replace("I have been using", "I've been using")
    }
}
