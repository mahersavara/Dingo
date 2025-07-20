package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.ShareableContent
import io.sukhuat.dingo.domain.model.SharingStats
import io.sukhuat.dingo.domain.model.SocialPlatform
import io.sukhuat.dingo.domain.repository.SharingRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case for sharing achievements on social media
 */
class ShareAchievementUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {

    /**
     * Generate shareable content for an achievement
     * * @param achievement The achievement to share
     * @param includeAppPromotion Whether to include app promotion in the share text
     * @return Shareable content with text and optional image
     * @throws ProfileError.ValidationError if achievement is not unlocked
     */
    suspend operator fun invoke(
        achievement: Achievement,
        includeAppPromotion: Boolean = true
    ): ShareableContent {
        // Validate that achievement is unlocked
        if (!achievement.isUnlocked) {
            throw ProfileError.ValidationError(
                "achievement",
                "Cannot share locked achievement"
            )
        }

        val shareText = generateShareText(achievement, includeAppPromotion)
        val hashtags = generateHashtags(achievement)

        return ShareableContent(
            text = shareText,
            hashtags = hashtags,
            achievementTitle = achievement.title,
            achievementDescription = achievement.description,
            achievementIconResId = achievement.iconResId
        )
    }

    /**
     * Generate share text for different social media platforms
     * * @param achievement The achievement to share
     * @param platform The target social media platform
     * @param includeAppPromotion Whether to include app promotion
     * @return Platform-specific share text
     */
    suspend fun generatePlatformSpecificContent(
        achievement: Achievement,
        platform: SocialPlatform,
        includeAppPromotion: Boolean = true
    ): ShareableContent {
        val baseContent = invoke(achievement, includeAppPromotion)

        return when (platform) {
            SocialPlatform.TWITTER -> baseContent.copy(
                text = truncateForTwitter(baseContent.text),
                hashtags = baseContent.hashtags.take(3) // Twitter hashtag limit
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
                text = baseContent.text + "\n\n" + baseContent.hashtags.joinToString(" "),
                hashtags = emptyList() // WhatsApp doesn't use hashtags
            )
            SocialPlatform.GENERIC -> baseContent
        }
    }

    /**
     * Get sharing statistics for achievements
     * This could be used for analytics or gamification
     */
    suspend fun getSharingStats(): SharingStats {
        return sharingRepository.getSharingStats().first()
    }

    private fun generateShareText(achievement: Achievement, includeAppPromotion: Boolean): String {
        val baseText = "🎉 Just unlocked the '${achievement.title}' achievement! ${achievement.description}"

        return if (includeAppPromotion) {
            "$baseText\n\nJoin me on my goal-setting journey with Dingo! 🎯"
        } else {
            baseText
        }
    }

    private fun generateHashtags(achievement: Achievement): List<String> {
        val baseHashtags = listOf("#GoalSetting", "#Achievement", "#PersonalGrowth", "#DingoApp")

        // Add achievement-specific hashtags based on achievement type
        val specificHashtags = when {
            achievement.id.contains("streak") -> listOf("#Consistency", "#DailyHabits")
            achievement.id.contains("goal") -> listOf("#Goals", "#Success")
            achievement.id.contains("first") -> listOf("#FirstSteps", "#NewBeginning")
            achievement.id.contains("master") -> listOf("#Mastery", "#Excellence")
            else -> emptyList()
        }

        return baseHashtags + specificHashtags
    }

    private fun truncateForTwitter(text: String, maxLength: Int = 240): String {
        return if (text.length <= maxLength) {
            text
        } else {
            text.take(maxLength - 3) + "..."
        }
    }

    private fun makeLinkedInFriendly(text: String): String {
        return text.replace("🎉", "").replace("🎯", "")
            .replace("Just unlocked", "I'm proud to have achieved")
    }
}
