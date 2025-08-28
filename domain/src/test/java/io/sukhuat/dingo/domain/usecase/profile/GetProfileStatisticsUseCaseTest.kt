package io.sukhuat.dingo.domain.usecase.profile

import io.mockk.coEvery
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetProfileStatisticsUseCaseTest {

    private lateinit var profileStatisticsRepository: ProfileStatisticsRepository
    private lateinit var getProfileStatisticsUseCase: GetProfileStatisticsUseCase

    private val testAchievement = Achievement(
        id = "achievement-1",
        title = "First Goal",
        description = "Created your first goal",
        iconResId = 1,
        unlockedDate = 1705320000000L, // Jan 15, 2024 10:00 AM
        isUnlocked = true
    )

    private val testMonthlyStats = MonthlyStats(
        month = "2024-01",
        goalsCreated = 5,
        goalsCompleted = 3,
        completionRate = 0.6f
    )

    private val testProfileStatistics = ProfileStatistics(
        totalGoalsCreated = 10,
        completedGoals = 7,
        completionRate = 0.7f,
        currentStreak = 5,
        longestStreak = 8,
        monthlyStats = mapOf("2024-01" to testMonthlyStats),
        achievements = listOf(testAchievement)
    )

    @Before
    fun setUp() {
        profileStatisticsRepository = mockk()
        getProfileStatisticsUseCase = GetProfileStatisticsUseCase(profileStatisticsRepository)
    }

    @Test
    fun `invoke should return profile statistics flow from repository`() = runTest {
        // Given
        coEvery { profileStatisticsRepository.getProfileStatistics() } returns flowOf(testProfileStatistics)

        // When
        val result = getProfileStatisticsUseCase()

        // Then
        result.collect { statistics ->
            assertEquals(testProfileStatistics, statistics)
            assertEquals(10, statistics.totalGoalsCreated)
            assertEquals(7, statistics.completedGoals)
            assertEquals(0.7f, statistics.completionRate)
            assertEquals(5, statistics.currentStreak)
            assertEquals(8, statistics.longestStreak)
            assertEquals(1, statistics.monthlyStats.size)
            assertEquals(1, statistics.achievements.size)
        }
    }

    @Test
    fun `invoke should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Statistics repository error")
        coEvery { profileStatisticsRepository.getProfileStatistics() } throws exception

        // When & Then
        try {
            getProfileStatisticsUseCase()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Statistics repository error", e.message)
        }
    }
}
