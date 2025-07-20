package io.sukhuat.dingo.domain.usecase.preferences

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UpdatePreferencesUseCaseTest {

    private lateinit var userPreferencesRepository: UserPreferencesRepository
    private lateinit var updatePreferencesUseCase: UpdatePreferencesUseCase

    @Before
    fun setUp() {
        userPreferencesRepository = mockk(relaxed = true)
        updatePreferencesUseCase = UpdatePreferencesUseCase(userPreferencesRepository)
    }

    @Test
    fun `updateDarkModeEnabled should call repository with correct value`() = runTest {
        // Given
        val enabled = true
        coEvery { userPreferencesRepository.updateDarkModeEnabled(enabled) } returns Unit

        // When
        updatePreferencesUseCase.updateDarkModeEnabled(enabled)

        // Then
        coVerify { userPreferencesRepository.updateDarkModeEnabled(enabled) }
    }

    @Test
    fun `updateNotificationsEnabled should call repository with correct value`() = runTest {
        // Given
        val enabled = false
        coEvery { userPreferencesRepository.updateNotificationsEnabled(enabled) } returns Unit

        // When
        updatePreferencesUseCase.updateNotificationsEnabled(enabled)

        // Then
        coVerify { userPreferencesRepository.updateNotificationsEnabled(enabled) }
    }

    @Test
    fun `updateSoundEnabled should call repository with correct value`() = runTest {
        // Given
        val enabled = true
        coEvery { userPreferencesRepository.updateSoundEnabled(enabled) } returns Unit

        // When
        updatePreferencesUseCase.updateSoundEnabled(enabled)

        // Then
        coVerify { userPreferencesRepository.updateSoundEnabled(enabled) }
    }

    @Test
    fun `updateVibrationEnabled should call repository with correct value`() = runTest {
        // Given
        val enabled = false
        coEvery { userPreferencesRepository.updateVibrationEnabled(enabled) } returns Unit

        // When
        updatePreferencesUseCase.updateVibrationEnabled(enabled)

        // Then
        coVerify { userPreferencesRepository.updateVibrationEnabled(enabled) }
    }

    @Test
    fun `updateLanguageCode should call repository with valid language code`() = runTest {
        // Given
        val languageCode = "es"
        coEvery { userPreferencesRepository.updateLanguageCode(languageCode) } returns Unit

        // When
        updatePreferencesUseCase.updateLanguageCode(languageCode)

        // Then
        coVerify { userPreferencesRepository.updateLanguageCode(languageCode) }
    }

    @Test
    fun `updateLanguageCode should accept empty language code`() = runTest {
        // Given
        val emptyLanguageCode = ""
        coEvery { userPreferencesRepository.updateLanguageCode(emptyLanguageCode) } returns Unit

        // When
        updatePreferencesUseCase.updateLanguageCode(emptyLanguageCode)

        // Then
        coVerify { userPreferencesRepository.updateLanguageCode(emptyLanguageCode) }
    }

    @Test
    fun `updateLanguageCode should accept blank language code`() = runTest {
        // Given
        val blankLanguageCode = "   "
        coEvery { userPreferencesRepository.updateLanguageCode(blankLanguageCode) } returns Unit

        // When
        updatePreferencesUseCase.updateLanguageCode(blankLanguageCode)

        // Then
        coVerify { userPreferencesRepository.updateLanguageCode(blankLanguageCode) }
    }

    @Test
    fun `updateLanguageCode should accept valid language codes`() = runTest {
        // Given
        val validLanguageCodes = listOf("en", "es", "fr", "de", "it", "pt", "zh", "ja", "ko")

        // When & Then
        validLanguageCodes.forEach { code ->
            updatePreferencesUseCase.updateLanguageCode(code)
            coVerify { userPreferencesRepository.updateLanguageCode(code) }
        }
    }

    @Test
    fun `updateLanguageCode should handle long language codes`() = runTest {
        // Given
        val longLanguageCode = "en-US"
        coEvery { userPreferencesRepository.updateLanguageCode(longLanguageCode) } returns Unit

        // When
        updatePreferencesUseCase.updateLanguageCode(longLanguageCode)

        // Then
        coVerify { userPreferencesRepository.updateLanguageCode(longLanguageCode) }
    }

    @Test
    fun `update methods should propagate repository exceptions`() = runTest {
        // Given
        val exception = RuntimeException("Update failed")
        coEvery { userPreferencesRepository.updateDarkModeEnabled(any()) } throws exception

        // When & Then
        val thrownException = assertFailsWith<RuntimeException> {
            updatePreferencesUseCase.updateDarkModeEnabled(true)
        }
        assertEquals("Update failed", thrownException.message)
    }

    @Test
    fun `multiple updates should work independently`() = runTest {
        // Given
        coEvery { userPreferencesRepository.updateDarkModeEnabled(any()) } returns Unit
        coEvery { userPreferencesRepository.updateNotificationsEnabled(any()) } returns Unit
        coEvery { userPreferencesRepository.updateLanguageCode(any()) } returns Unit

        // When
        updatePreferencesUseCase.updateDarkModeEnabled(true)
        updatePreferencesUseCase.updateNotificationsEnabled(false)
        updatePreferencesUseCase.updateLanguageCode("fr")

        // Then
        coVerify { userPreferencesRepository.updateDarkModeEnabled(true) }
        coVerify { userPreferencesRepository.updateNotificationsEnabled(false) }
        coVerify { userPreferencesRepository.updateLanguageCode("fr") }
    }
}
