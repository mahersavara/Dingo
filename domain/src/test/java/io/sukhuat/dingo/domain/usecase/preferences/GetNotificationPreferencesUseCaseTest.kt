package io.sukhuat.dingo.domain.usecase.preferences

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetNotificationPreferencesUseCaseTest {

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var getNotificationPreferencesUseCase: GetNotificationPreferencesUseCase

    private val testUserPreferences = UserPreferences(
        notificationsEnabled = true,
        weeklyRemindersEnabled = true,
        goalCompletionNotifications = true
    )

    @Before
    fun setUp() {
        userPreferencesRepository = mockk()
        getNotificationPreferencesUseCase = GetNotificationPreferencesUseCase(userPreferencesRepository)
    }

    @Test
    fun `invoke should return notification preferences flow from repository`() = runTest {
        // Given
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(testUserPreferences)

        // When
        val result = getNotificationPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(true, preferences.notificationsEnabled)
            assertEquals(true, preferences.weeklyRemindersEnabled)
            assertEquals(true, preferences.goalCompletionNotifications)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should return preferences with all notifications enabled`() = runTest {
        // Given
        val allEnabledPreferences = testUserPreferences.copy(
            notificationsEnabled = true,
            weeklyRemindersEnabled = true,
            goalCompletionNotifications = true
        )
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(allEnabledPreferences)

        // When
        val result = getNotificationPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(true, preferences.notificationsEnabled)
            assertEquals(true, preferences.weeklyRemindersEnabled)
            assertEquals(true, preferences.goalCompletionNotifications)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should return preferences with all notifications disabled`() = runTest {
        // Given
        val allDisabledPreferences = testUserPreferences.copy(
            notificationsEnabled = false,
            weeklyRemindersEnabled = false,
            goalCompletionNotifications = false
        )
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(allDisabledPreferences)

        // When
        val result = getNotificationPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(false, preferences.notificationsEnabled)
            assertEquals(false, preferences.weeklyRemindersEnabled)
            assertEquals(false, preferences.goalCompletionNotifications)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should handle selective notification preferences`() = runTest {
        // Given
        val selectivePreferences = testUserPreferences.copy(
            notificationsEnabled = true,
            weeklyRemindersEnabled = true,
            goalCompletionNotifications = false
        )
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(selectivePreferences)

        // When
        val result = getNotificationPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(true, preferences.notificationsEnabled)
            assertEquals(true, preferences.weeklyRemindersEnabled)
            assertEquals(false, preferences.goalCompletionNotifications)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Notification preferences loading failed")
        every { userPreferencesRepository.getUserPreferences() } throws exception

        // When & Then
        try {
            getNotificationPreferencesUseCase()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Notification preferences loading failed", e.message)
        }
    }
}
