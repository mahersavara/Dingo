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

class GetUserPreferencesUseCaseTest {

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var getUserPreferencesUseCase: GetUserPreferencesUseCase

    private val testUserPreferences = UserPreferences(
        darkModeEnabled = false,
        notificationsEnabled = true,
        soundEnabled = true,
        vibrationEnabled = true,
        languageCode = "en"
    )

    @Before
    fun setUp() {
        userPreferencesRepository = mockk()
        getUserPreferencesUseCase = GetUserPreferencesUseCase(userPreferencesRepository)
    }

    @Test
    fun `invoke should return user preferences flow from repository`() = runTest {
        // Given
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(testUserPreferences)

        // When
        val result = getUserPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(testUserPreferences, preferences)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should return default preferences when none exist`() = runTest {
        // Given
        val defaultPreferences = UserPreferences(
            darkModeEnabled = false,
            notificationsEnabled = true,
            soundEnabled = true,
            vibrationEnabled = true,
            languageCode = "en"
        )
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(defaultPreferences)

        // When
        val result = getUserPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(defaultPreferences, preferences)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should handle different language preferences`() = runTest {
        // Given
        val spanishPreferences = testUserPreferences.copy(languageCode = "es")
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(spanishPreferences)

        // When
        val result = getUserPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals("es", preferences.languageCode)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should handle all disabled preferences`() = runTest {
        // Given
        val allDisabledPreferences = UserPreferences(
            darkModeEnabled = false,
            notificationsEnabled = false,
            soundEnabled = false,
            vibrationEnabled = false,
            languageCode = "en"
        )
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(allDisabledPreferences)

        // When
        val result = getUserPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(false, preferences.darkModeEnabled)
            assertEquals(false, preferences.notificationsEnabled)
            assertEquals(false, preferences.soundEnabled)
            assertEquals(false, preferences.vibrationEnabled)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Preferences loading failed")
        every { userPreferencesRepository.getUserPreferences() } throws exception

        // When & Then
        try {
            getUserPreferencesUseCase()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Preferences loading failed", e.message)
        }
    }
}
