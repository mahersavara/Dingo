package io.sukhuat.dingo.domain.usecase.profile

import io.mockk.coEvery
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetUserProfileUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var getUserProfileUseCase: GetUserProfileUseCase

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

    @Before
    fun setUp() {
        userProfileRepository = mockk()
        getUserProfileUseCase = GetUserProfileUseCase(userProfileRepository)
    }

    @Test
    fun `invoke should return user profile flow from repository`() = runTest {
        // Given
        coEvery { userProfileRepository.getUserProfile() } returns flowOf(testUserProfile)

        // When
        val result = getUserProfileUseCase()

        // Then
        result.collect { profile ->
            assertEquals(testUserProfile, profile)
        }
    }

    @Test
    fun `invoke should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Repository error")
        coEvery { userProfileRepository.getUserProfile() } throws exception

        // When & Then
        try {
            getUserProfileUseCase()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Repository error", e.message)
        }
    }
}
