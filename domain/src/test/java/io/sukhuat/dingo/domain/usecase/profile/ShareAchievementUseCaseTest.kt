package io.sukhuat.dingo.domain.usecase.profile

import io.mockk.mockk
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.repository.SharingRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShareAchievementUseCaseTest {

    private lateinit var sharingRepository: SharingRepository
    private lateinit var shareAchievementUseCase: ShareAchievementUseCase

    private val testAchievement = Achievement(
        id = "achievement-1",
        title = "First Goal",
        description = "Created your first goal",
        iconResId = 1,
        unlockedDate = LocalDateTime.of(2024, 1, 15, 10, 0),
        isUnlocked = true
    )

    @Before
    fun setUp() {
        sharingRepository = mockk()
        shareAchievementUseCase = ShareAchievementUseCase(sharingRepository)
    }

    @Test
    fun `invoke should generate shareable achievement content successfully`() = runTest {
        // When
        val result = shareAchievementUseCase(testAchievement)

        // Then
        assertEquals("First Goal", result.achievementTitle)
        assertEquals("Created your first goal", result.achievementDescription)
        assertEquals(1, result.achievementIconResId)
        assertTrue(result.text.contains("First Goal"))
        assertTrue(result.hashtags.isNotEmpty())
    }

    @Test
    fun `invoke should handle unlocked achievement`() = runTest {
        // Given
        val unlockedAchievement = testAchievement.copy(isUnlocked = true)

        // When
        val result = shareAchievementUseCase(unlockedAchievement)

        // Then
        assertEquals("First Goal", result.achievementTitle)
        assertEquals(true, unlockedAchievement.isUnlocked)
        assertTrue(result.text.contains("unlocked"))
    }

    @Test
    fun `invoke should throw error for locked achievement`() = runTest {
        // Given
        val lockedAchievement = testAchievement.copy(isUnlocked = false, unlockedDate = null)

        // When & Then
        try {
            shareAchievementUseCase(lockedAchievement)
            assert(false) { "Expected exception to be thrown" }
        } catch (e: io.sukhuat.dingo.domain.model.ProfileError.ValidationError) {
            assertEquals("achievement", e.field)
            assertEquals("Cannot share locked achievement", e.message)
        }
    }

    @Test
    fun `generatePlatformSpecificContent should handle different platforms`() = runTest {
        // When
        val twitterResult = shareAchievementUseCase.generatePlatformSpecificContent(
            testAchievement,
            io.sukhuat.dingo.domain.model.SocialPlatform.TWITTER
        )
        val whatsappResult = shareAchievementUseCase.generatePlatformSpecificContent(
            testAchievement,
            io.sukhuat.dingo.domain.model.SocialPlatform.WHATSAPP
        )

        // Then
        assertTrue(twitterResult.hashtags.size <= 3) // Twitter hashtag limit
        assertTrue(whatsappResult.hashtags.isEmpty()) // WhatsApp doesn't use hashtags
    }
}
