package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.ShareableContent
import io.sukhuat.dingo.domain.model.SocialPlatform
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.repository.SharingRepository
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for sharing user profile and achievements overview
 */
class ShareProfileUseCase @Inject constructor(
    private val sharingRepository: SharingRepository,
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * Generate shareable content for user profile
     * * @param includeStats Whether to include achievement statistics
     * @param includeAppPromotion Whether to include app promotion
     * @return Shareable content with profile information
     */
    suspend operator fun invoke(
        includeStats: Boolean = true,
        includeAppPromotion: Boolean = true
    ): ShareableContent {
        val userProfile = userProfileRepository.getUserProfile().first()
        val profileLink = sharingRepository.generateProfileLink(userProfile.userId)

        val shareText = generateProfileShareText(
            userProfile = userProfile,
            includeStats = includeStats,
            includeAppPromotion = includeAppPromotion
        )

        val hashtags = generateProfileHashtags()

        return ShareableContent(
            text = shareText,
            hashtags = hashtags,
            profileLink = profileLink
        )
    }

    /**
     * Generate platform-specific profile sharing content
     */
    suspend fun generatePlatformSpecificContent(
        platform: SocialPlatform,
        includeStats: Boolean = true,
        includeAppPromotion: Boolean = true
    ): ShareableContent {
        val baseContent = invoke(includeStats, includeAppPromotion)

        return when (platform) {
            SocialPlatform.TWITTER -> baseContent.copy(
                text = truncateForTwitter(baseContent.text),
                hashtags = baseContent.hashtags.take(3)
            )
            SocialPlatform.FACEBOOK -> baseContent.copy(
                text = baseContent.text + "\n\n" + baseContent.hashtags.joinToString(" ")
            )
            SocialPlatform.INSTAGRAM -> baseContent.copy(
                text = baseContent.text + "\n\n" + baseContent.hashtags.joinToString(" ")
            )
            SocialPlatform.LINKEDIN -> baseContent.copy(
                text = makeLinkedInFriendly(baseContent.text),
                hashtags = baseContent.hashtags.take(5)
            )
            SocialPlatform.WHATSAPP -> baseContent.copy(
                text = baseContent.text + "\n\nCheck out my profile: ${baseContent.profileLink}"
            )
            SocialPlatform.GENERIC -> baseContent
        }
    }

    private suspend fun generateProfileShareText(
        userProfile: UserProfile,
        includeStats: Boolean,
        includeAppPromotion: Boolean
    ): String {
        val displayName = userProfile.displayName.ifEmpty { "A Dingo user" }

        var shareText = "🎯 Check out $displayName's goal-setting journey!"

        if (includeStats) {
            // Note: In a real implementation, you'd fetch actual statistics
            shareText += "\n\n📊 Making progress on personal goals and building great habits!"
        }

        if (includeAppPromotion) {
            shareText += "\n\nJoin us on Dingo - the app that makes goal-setting fun and achievable! 🚀"
        }

        return shareText
    }

    private fun generateProfileHashtags(): List<String> {
        return listOf(
            "#GoalSetting",
            "#PersonalGrowth",
            "#Productivity",
            "#SelfImprovement",
            "#DingoApp",
            "#Goals2024"
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
        return text.replace("🎯", "").replace("🚀", "")
            .replace("Check out", "I'd like to share")
    }
}
