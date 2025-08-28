package io.sukhuat.dingo.domain.usecase.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.ReferralData
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.repository.SharingRepository
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ManageReferralUseCaseTest {

    private lateinit var sharingRepository: SharingRepository
    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var manageReferralUseCase: ManageReferralUseCase

    private val testUserProfile = UserProfile(
        userId = "test-user-id",
        displayName = "Test User",
        email = "test@example.com",
        profileImageUrl = "https://example.com/image.jpg",
        joinDate = 1704067200000L, // Jan 1, 2024 00:00
        isEmailVerified = true,
        authProvider = AuthProvider.EMAIL_PASSWORD,
        lastLoginDate = 1721293200000L // July 18, 2024 10:00 AM
    )

    private val testReferralData = ReferralData(
        referralCode = "TEST123",
        referralLink = "https://dingo.app/ref/TEST123",
        totalInvites = 5,
        successfulInvites = 3,
        pendingInvites = 2
    )

    @Before
    fun setUp() {
        sharingRepository = mockk()
        userProfileRepository = mockk()
        manageReferralUseCase = ManageReferralUseCase(sharingRepository, userProfileRepository)
    }

    @Test
    fun `generateNewReferralCode should create referral code successfully`() = runTest {
        // Given
        val expectedReferralCode = "TEST123"
        coEvery { sharingRepository.generateReferralCode() } returns expectedReferralCode

        // When
        val result = manageReferralUseCase.generateNewReferralCode()

        // Then
        assertEquals(expectedReferralCode, result)
        coVerify { sharingRepository.generateReferralCode() }
    }

    @Test
    fun `getReferralData should return referral statistics`() = runTest {
        // Given
        coEvery { sharingRepository.getReferralData() } returns flowOf(testReferralData)

        // When
        val result = manageReferralUseCase.getReferralData()

        // Then
        result.collect { data ->
            assertEquals(testReferralData, data)
        }
        coVerify { sharingRepository.getReferralData() }
    }

    @Test
    fun `trackReferralInvitation should record invitation`() = runTest {
        // Given
        val platform = io.sukhuat.dingo.domain.model.SocialPlatform.WHATSAPP
        coEvery { sharingRepository.trackSharingEvent("referral_invitation", platform) } returns Unit

        // When
        manageReferralUseCase.trackReferralInvitation(platform)

        // Then
        coVerify { sharingRepository.trackSharingEvent("referral_invitation", platform) }
    }

    @Test
    fun `createReferralShareContent should generate shareable content`() = runTest {
        // Given
        val personalMessage = "Check this out!"
        val expectedShareContent = io.sukhuat.dingo.domain.model.ShareableContent(
            text = "Test content",
            hashtags = listOf("#GoalSetting", "#PersonalGrowth"),
            profileLink = testReferralData.referralLink
        )
        coEvery { userProfileRepository.getUserProfile() } returns flowOf(testUserProfile)
        coEvery { sharingRepository.getReferralData() } returns flowOf(testReferralData)

        // When
        val result = manageReferralUseCase.createReferralShareContent(personalMessage, true)

        // Then
        assertEquals(testReferralData.referralLink, result.profileLink)
        coVerify { userProfileRepository.getUserProfile() }
        coVerify { sharingRepository.getReferralData() }
    }

    @Test
    fun `generateNewReferralCode should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Referral generation failed")
        coEvery { sharingRepository.generateReferralCode() } throws exception

        // When & Then
        try {
            manageReferralUseCase.generateNewReferralCode()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Referral generation failed", e.message)
        }
    }

    @Test
    fun `getReferralData should handle repository failure`() = runTest {
        // Given
        val exception = RuntimeException("Referral data loading failed")
        coEvery { sharingRepository.getReferralData() } throws exception

        // When & Then
        try {
            manageReferralUseCase.getReferralData()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Referral data loading failed", e.message)
        }
    }

    @Test
    fun `trackReferralInvitation should handle tracking failure`() = runTest {
        // Given
        val platform = io.sukhuat.dingo.domain.model.SocialPlatform.WHATSAPP
        val exception = RuntimeException("Tracking failed")
        coEvery { sharingRepository.trackSharingEvent("referral_invitation", platform) } throws exception

        // When & Then
        try {
            manageReferralUseCase.trackReferralInvitation(platform)
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Tracking failed", e.message)
        }
    }
}
