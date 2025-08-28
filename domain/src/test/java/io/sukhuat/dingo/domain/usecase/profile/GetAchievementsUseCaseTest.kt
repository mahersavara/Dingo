package io.sukhuat.dingo.domain.usecase.profile

import io.mockk.coEvery
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetAchievementsUseCaseTest {

    private lateinit var profileStatisticsRepository: ProfileStatisticsRepository
    private lateinit var getAchievementsUseCase: GetAchievementsUseCase

    private val testAchievements = listOf(
        Achievement(
            id = "achievement-1",
            title = "First Goal",
            description = "Created your first goal",
            iconResId = 1,
            unlockedDate = 1705320000000L, // Jan 15, 2024 10:00 AM
            isUnlocked = true
        ),
        Achievement(
            id = "achievement-2",
            title = "Goal Crusher",
            description = "Completed 10 goals",
            iconResId = 2,
            unlockedDate = null,
            isUnlocked = false
        )
    )

    @Before
    fun setUp() {
        profileStatisticsRepository = mockk()
        getAchievementsUseCase = GetAchievementsUseCase(profileStatisticsRepository)
    }

    @Test
    fun `invoke should return achievements from repository`() = runTest {
        // Given
        coEvery { profileStatisticsRepository.getAchievements() } returns testAchievements

        // When
        val result = getAchievementsUseCase()

        // Then
        assertEquals(testAchievements, result)
        assertEquals(2, result.size)
        assertEquals("achievement-1", result[0].id)
        assertEquals("achievement-2", result[1].id)
        assertTrue(result[0].isUnlocked)
        assertTrue(!result[1].isUnlocked)
    }

    @Test
    fun `invoke should return empty list when no achievements`() = runTest {
        // Given
        coEvery { profileStatisticsRepository.getAchievements() } returns emptyList()

        // When
        val result = getAchievementsUseCase()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `invoke should wrap repository exceptions in UnknownError`() = runTest {
        // Given
        val exception = RuntimeException("Failed to get achievements")
        coEvery { profileStatisticsRepository.getAchievements() } throws exception

        // When & Then
        val wrappedException = assertFailsWith<ProfileError.UnknownError> {
            getAchievementsUseCase()
        }
        assertEquals(exception, wrappedException.cause)
    }
}
