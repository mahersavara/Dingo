package io.sukhuat.dingo.domain.usecase.account

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.LoginRecord
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GetLoginHistoryUseCaseTest {

    private lateinit var userProfileRepository: UserProfileRepository
    private lateinit var getLoginHistoryUseCase: GetLoginHistoryUseCase

    private val testLoginHistory = listOf(
        LoginRecord(
            timestamp = 1721296800000L, // July 18, 2024 10:00 AM
            deviceInfo = "Android 14, Pixel 7",
            ipAddress = "192.168.1.1",
            location = "New York, US"
        ),
        LoginRecord(
            timestamp = 1721217000000L, // July 17, 2024 3:30 PM
            deviceInfo = "Android 14, Pixel 7",
            ipAddress = "192.168.1.1",
            location = "New York, US"
        ),
        LoginRecord(
            timestamp = 1721116500000L, // July 16, 2024 9:15 AM
            deviceInfo = "Unknown Device",
            ipAddress = "10.0.0.1",
            location = null
        )
    )

    @Before
    fun setUp() {
        userProfileRepository = mockk()
        getLoginHistoryUseCase = GetLoginHistoryUseCase(userProfileRepository)
    }

    @Test
    fun `invoke should return formatted login history from repository`() = runTest {
        // Given
        coEvery { userProfileRepository.getLoginHistory() } returns testLoginHistory

        // When
        val result = getLoginHistoryUseCase()

        // Then
        assertEquals(3, result.size)
        assertEquals("Android 14, Pixel 7", result[0].deviceInfo)
        assertEquals("192.168.1.1", result[0].ipAddress)
        assertEquals("New York, US", result[0].location)
        coVerify { userProfileRepository.getLoginHistory() }
    }

    @Test
    fun `invoke should return empty list when no login history exists`() = runTest {
        // Given
        coEvery { userProfileRepository.getLoginHistory() } returns emptyList()

        // When
        val result = getLoginHistoryUseCase()

        // Then
        assertEquals(emptyList(), result)
        coVerify { userProfileRepository.getLoginHistory() }
    }

    @Test
    fun `invoke should format timestamps correctly`() = runTest {
        // Given
        coEvery { userProfileRepository.getLoginHistory() } returns testLoginHistory

        // When
        val result = getLoginHistoryUseCase()

        // Then
        assertTrue(result[0].formattedDate.isNotEmpty())
        assertTrue(result[0].formattedTime.isNotEmpty())
        assertTrue(result[0].relativeTime.isNotEmpty())
        coVerify { userProfileRepository.getLoginHistory() }
    }

    @Test
    fun `invoke should handle null location gracefully`() = runTest {
        // Given
        val historyWithNullLocation = listOf(
            LoginRecord(
                timestamp = 1721296800000L,
                deviceInfo = "Test Device",
                ipAddress = "192.168.1.1",
                location = null
            )
        )
        coEvery { userProfileRepository.getLoginHistory() } returns historyWithNullLocation

        // When
        val result = getLoginHistoryUseCase()

        // Then
        assertEquals(1, result.size)
        assertEquals("Unknown location", result[0].location)
        coVerify { userProfileRepository.getLoginHistory() }
    }

    @Test
    fun `getLoginSummary should return correct summary`() = runTest {
        // Given
        coEvery { userProfileRepository.getLoginHistory() } returns testLoginHistory

        // When
        val result = getLoginHistoryUseCase.getLoginSummary()

        // Then
        assertEquals(3, result.totalLogins)
        assertEquals(2, result.uniqueDevices) // "Android 14, Pixel 7" and "Unknown Device"
        assertEquals(1, result.uniqueLocations) // Only "New York, US" (null location is filtered out)
        assertTrue(result.lastLogin != null)
        coVerify { userProfileRepository.getLoginHistory() }
    }

    @Test
    fun `getLoginSummary should handle empty history gracefully`() = runTest {
        // Given
        coEvery { userProfileRepository.getLoginHistory() } returns emptyList()

        // When
        val result = getLoginHistoryUseCase.getLoginSummary()

        // Then
        assertEquals(0, result.totalLogins)
        assertEquals(0, result.uniqueDevices)
        assertEquals(0, result.uniqueLocations)
        assertEquals(null, result.lastLogin)
        assertEquals(false, result.hasSuspiciousActivity)
        assertEquals(0, result.recentLoginsCount)
        coVerify { userProfileRepository.getLoginHistory() }
    }

    @Test
    fun `invoke should propagate ProfileError exceptions`() = runTest {
        // Given
        val exception = ProfileError.AuthenticationExpired
        coEvery { userProfileRepository.getLoginHistory() } throws exception

        // When & Then
        assertFailsWith<ProfileError.AuthenticationExpired> {
            getLoginHistoryUseCase()
        }
    }

    @Test
    fun `invoke should wrap other exceptions in ProfileError UnknownError`() = runTest {
        // Given
        val exception = RuntimeException("Login history loading failed")
        coEvery { userProfileRepository.getLoginHistory() } throws exception

        // When & Then
        val thrownException = assertFailsWith<ProfileError.UnknownError> {
            getLoginHistoryUseCase()
        }
        assertEquals(exception, thrownException.cause)
    }
}
