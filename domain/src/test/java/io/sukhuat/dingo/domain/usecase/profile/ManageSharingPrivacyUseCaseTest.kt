package io.sukhuat.dingo.domain.usecase.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.SharingPrivacySettings
import io.sukhuat.dingo.domain.repository.SharingRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ManageSharingPrivacyUseCaseTest {

    private lateinit var sharingRepository: SharingRepository
    private lateinit var manageSharingPrivacyUseCase: ManageSharingPrivacyUseCase

    private val testPrivacySettings = SharingPrivacySettings(
        allowAchievementSharing = true,
        allowProfileSharing = true,
        allowReferralSharing = true,
        includeAppPromotion = true,
        shareWithRealName = false
    )

    @Before
    fun setUp() {
        sharingRepository = mockk()
        manageSharingPrivacyUseCase = ManageSharingPrivacyUseCase(sharingRepository)
    }

    @Test
    fun `getSharingPrivacySettings should return privacy settings from repository`() = runTest {
        // Given
        coEvery { sharingRepository.getSharingPrivacySettings() } returns flowOf(testPrivacySettings)

        // When
        val result = manageSharingPrivacyUseCase.getSharingPrivacySettings()

        // Then
        result.collect { settings ->
            assertEquals(testPrivacySettings, settings)
        }
        coVerify { sharingRepository.getSharingPrivacySettings() }
    }

    @Test
    fun `toggleProfileSharing should update profile sharing setting`() = runTest {
        // Given
        val enabled = false
        coEvery { sharingRepository.updateSharingPrivacySettings(any()) } returns Unit

        // When
        manageSharingPrivacyUseCase.toggleProfileSharing(enabled)

        // Then
        coVerify { sharingRepository.updateSharingPrivacySettings(any()) }
    }

    @Test
    fun `toggleAchievementSharing should update achievement sharing setting`() = runTest {
        // Given
        val enabled = true
        coEvery { sharingRepository.getSharingPrivacySettings() } returns flowOf(testPrivacySettings)
        coEvery { sharingRepository.updateSharingPrivacySettings(any()) } returns Unit

        // When
        manageSharingPrivacyUseCase.toggleAchievementSharing(enabled)

        // Then
        coVerify { sharingRepository.updateSharingPrivacySettings(any()) }
    }

    @Test
    fun `toggleReferralSharing should update referral sharing setting`() = runTest {
        // Given
        val enabled = true
        coEvery { sharingRepository.updateSharingPrivacySettings(any()) } returns Unit

        // When
        manageSharingPrivacyUseCase.toggleReferralSharing(enabled)

        // Then
        coVerify { sharingRepository.updateSharingPrivacySettings(any()) }
    }

    @Test
    fun `toggleAppPromotion should update app promotion setting`() = runTest {
        // Given
        val enabled = true
        coEvery { sharingRepository.updateSharingPrivacySettings(any()) } returns Unit

        // When
        manageSharingPrivacyUseCase.toggleAppPromotion(enabled)

        // Then
        coVerify { sharingRepository.updateSharingPrivacySettings(any()) }
    }

    @Test
    fun `getSharingPrivacySettings should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Privacy settings loading failed")
        coEvery { sharingRepository.getSharingPrivacySettings() } throws exception

        // When & Then
        try {
            manageSharingPrivacyUseCase.getSharingPrivacySettings()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Privacy settings loading failed", e.message)
        }
    }

    @Test
    fun `update methods should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Update failed")
        coEvery { sharingRepository.updateSharingPrivacySettings(any()) } throws exception

        // When & Then
        try {
            manageSharingPrivacyUseCase.toggleProfileSharing(true)
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Update failed", e.message)
        }
    }
}
