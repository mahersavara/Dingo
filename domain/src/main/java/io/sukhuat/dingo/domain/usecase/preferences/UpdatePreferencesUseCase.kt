package io.sukhuat.dingo.domain.usecase.preferences

import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Use case for updating user preferences
 */
class UpdatePreferencesUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    suspend fun updateSoundEnabled(enabled: Boolean) {
        userPreferencesRepository.updateSoundEnabled(enabled)
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        userPreferencesRepository.updateVibrationEnabled(enabled)
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        userPreferencesRepository.updateNotificationsEnabled(enabled)
    }

    suspend fun updateWeeklyRemindersEnabled(enabled: Boolean) {
        userPreferencesRepository.updateWeeklyRemindersEnabled(enabled)
    }

    suspend fun updateGoalCompletionNotifications(enabled: Boolean) {
        userPreferencesRepository.updateGoalCompletionNotifications(enabled)
    }

    suspend fun updateDarkModeEnabled(enabled: Boolean) {
        userPreferencesRepository.updateDarkModeEnabled(enabled)
    }

    suspend fun updateLanguageCode(languageCode: String) {
        userPreferencesRepository.updateLanguageCode(languageCode)
    }

    suspend fun updateAutoBackupEnabled(enabled: Boolean) {
        userPreferencesRepository.updateAutoBackupEnabled(enabled)
    }

    suspend fun updateAnalyticsEnabled(enabled: Boolean) {
        userPreferencesRepository.updateAnalyticsEnabled(enabled)
    }

    suspend fun resetToDefaults() {
        userPreferencesRepository.resetToDefaults()
    }
}
