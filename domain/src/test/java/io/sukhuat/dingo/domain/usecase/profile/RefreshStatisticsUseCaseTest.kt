package io.sukhuat.dingo.domain.usecase.profile

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class RefreshStatisticsUseCaseTest {

    private lateinit var profileStatisticsRepository: ProfileStatisticsRepository
    private lateinit var refreshStatisticsUseCase: RefreshStatisticsUseCase

    @Before
    fun setUp() {
        profileStatisticsRepository = mockk(relaxed = true)
        refreshStatisticsUseCase = RefreshStatisticsUseCase(profileStatisticsRepository)
    }

    @Test
    fun `invoke should call repository refresh method`() = runTest {
        // Given
        coEvery { profileStatisticsRepository.refreshStatistics() } returns Unit

        // When
        refreshStatisticsUseCase()

        // Then
        coVerify { profileStatisticsRepository.refreshStatistics() }
    }

    @Test
    fun `invoke should wrap repository exceptions in UnknownError`() = runTest {
        // Given
        val exception = RuntimeException("Refresh failed")
        coEvery { profileStatisticsRepository.refreshStatistics() } throws exception

        // When & Then
        val wrappedException = assertFailsWith<ProfileError.UnknownError> {
            refreshStatisticsUseCase()
        }
        assertEquals(exception, wrappedException.cause)
    }
}
