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

class GetAudioFeedbackPreferencesUseCaseTest {

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var getAudioFeedbackPreferencesUseCase: GetAudioFeedbackPreferencesUseCase

    private val testUserPreferences = UserPreferences(
        soundEnabled = true,
        vibrationEnabled = true
    )

    @Before
    fun setUp() {
        userPreferencesRepository = mockk()
        getAudioFeedbackPreferencesUseCase = GetAudioFeedbackPreferencesUseCase(userPreferencesRepository)
    }

    @Test
    fun `invoke should return audio feedback preferences flow from repository`() = runTest {
        // Given
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(testUserPreferences)

        // When
        val result = getAudioFeedbackPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(true, preferences.soundEnabled)
            assertEquals(true, preferences.vibrationEnabled)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should return preferences with all audio feedback enabled`() = runTest {
        // Given
        val allEnabledPreferences = testUserPreferences.copy(
            soundEnabled = true,
            vibrationEnabled = true
        )
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(allEnabledPreferences)

        // When
        val result = getAudioFeedbackPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(true, preferences.soundEnabled)
            assertEquals(true, preferences.vibrationEnabled)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should return preferences with all audio feedback disabled`() = runTest {
        // Given
        val allDisabledPreferences = testUserPreferences.copy(
            soundEnabled = false,
            vibrationEnabled = false
        )
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(allDisabledPreferences)

        // When
        val result = getAudioFeedbackPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(false, preferences.soundEnabled)
            assertEquals(false, preferences.vibrationEnabled)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should handle selective audio feedback preferences`() = runTest {
        // Given
        val selectivePreferences = testUserPreferences.copy(
            soundEnabled = true,
            vibrationEnabled = false
        )
        every { userPreferencesRepository.getUserPreferences() } returns flowOf(selectivePreferences)

        // When
        val result = getAudioFeedbackPreferencesUseCase()

        // Then
        result.collect { preferences ->
            assertEquals(true, preferences.soundEnabled)
            assertEquals(false, preferences.vibrationEnabled)
        }
        verify { userPreferencesRepository.getUserPreferences() }
    }

    @Test
    fun `invoke should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Audio feedback preferences loading failed")
        every { userPreferencesRepository.getUserPreferences() } throws exception

        // When & Then
        try {
            getAudioFeedbackPreferencesUseCase()
            assert(false) { "Expected exception to be thrown" }
        } catch (e: RuntimeException) {
            assertEquals("Audio feedback preferences loading failed", e.message)
        }
    }
}
