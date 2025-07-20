package io.sukhuat.dingo.domain.usecase.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.repository.SharingRepository
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ShareProfileUseCaseTest {

    private lateinit var sharingRepository: SharingRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var shareProfileUseCase: ShareProfileUseCase

    private val testUserProfile = UserProfile(
        userId = "test-user-id",
        displayName = "Test User",
        email = "test@example.com",
        profileImageUrl = "https://example.com/image.jpg",
        joinDate = LocalDateTime.of(2024, 1, 1, 0, 0),
        isEmailVerified = true,
        authProvider = AuthProvider.EMAIL_PASSWORD,
        lastLoginDate = LocalDateTime.of(2024, 7, 18, 10, 0)
    )

    private val testProfileStatistics = ProfileStatistics(
        totalGoalsCreated = 10,
        completedGoals = 7,
        completionRate = 0.7f,
        currentStreak = 5,
        longestStreak = 8,
        monthlyStats = emptyMap(),
        achievements = emptyList()
    )

    @Before
    fun setUp() {
        sharingRepository = mockk()
        userProfileRepository = mockk()
        shareProfileUseCase = ShareProfileUseCase(sharingRepository, userProfileRepository)
    }

    @Test
    fun `invoke should generate shareable profile content successfully`() = runTest {
        // Given
        val expectedProfileLink = "https://dingo.app/profile/test-user-id"
        coEvery { userProfileRepository.getUserProfile() } returns flowOf(testUserProfile)
        coEvery { sharingRepository.generateProfileLink(testUserProfile.userId) } returns expectedProfileLink

        // When
        val result = shareProfileUseCase()

        // Then
        assertEquals(expectedProfileLink, result.profileLink)
        assertTrue(result.text.contains("Test User"))
        assertTrue(result.hashtags.isNotEmpty())
        coVerify { userProfileRepository.getUserProfile() }
        coVerify { sharingRepository.generateProfileLink(testUserProfile.userId) }
    }

    @Test
    fun `invoke should handle different sharing options`() = runTest {
        // Given
        val expectedProfileLink = "https://dingo.app/profile/test-user-id"
        coEvery { userProfileRepository.getUserProfile() } returns flowOf(testUserProfile)
        coEvery { sharingRepository.generateProfileLink(testUserProfile.userId) } returns expectedProfileLink

        // When
        val resultWithStats = shareProfileUseCase(includeStats = true, includeAppPromotion = false)
        val resultWithoutStats = shareProfileUseCase(includeStats = false, includeAppPromotion = true)

        // Then
        assertTrue(resultWithStats.text.contains("progress"))
        assertTrue(resultWithoutStats.text.contains("Dingo"))
        coVerify(exactly = 2) { userProfileRepository.getUserProfile() }
    }

    @Test
    fun `generatePlatformSpecificContent should handle different platforms`() = runTest {
        // Given
        val expectedProfileLink = "https://dingo.app/profile/test-user-id"
        coEvery { userProfileRepository.getUserProfile() } returns flowOf(testUserProfile)
        coEvery { sharingRepository.generateProfileLink(testUserProfile.userId) } returns expectedProfileLink

        // When
        val twitterResult = shareProfileUseCase.generatePlatformSpecificContent(
            io.sukhuat.dingo.domain.model.SocialPlatform.TWITTER
        )
        val whatsappResult = shareProfileUseCase.generatePlatformSpecificContent(
            io.sukhuat.dingo.domain.model.SocialPlatform.WHATSAPP
        )

        // Then
        assertTrue(twitterResult.hashtags.size <= 3) // Twitter hashtag limit
        assertTrue(whatsappResult.text.contains("Check out my profile:"))
    }

    @Test
    fun `invoke should handle profile loading failure`() = runTest {
        // Given
        val exception = RuntimeException("Profile loading failed")
        coEvery { userProfileRepository.getUserProfile() } throws exception

        // When & Then
        try {
            shareProfileUseCase()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Profile loading failed", e.message)
        }
    }
}
