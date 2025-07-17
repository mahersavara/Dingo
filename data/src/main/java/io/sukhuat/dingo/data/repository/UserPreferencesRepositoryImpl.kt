package io.sukhuat.dingo.data.repository

import io.sukhuat.dingo.data.preferences.UserPreferencesDataStore
import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserPreferencesRepository using DataStore
 */
@Singleton
class UserPreferencesRepositoryImpl @Inject constructor(
    private val userPreferencesDataStore: UserPreferencesDataStore
) : UserPreferencesRepository {

    override fun getUserPreferences(): Flow<UserPreferences> {
        return userPreferencesDataStore.userPreferences
    }

    override suspend fun updateSoundEnabled(enabled: Boolean) {
        userPreferencesDataStore.updateSoundEnabled(enabled)
    }

    override suspend fun updateVibrationEnabled(enabled: Boolean) {
        userPreferencesDataStore.updateVibrationEnabled(enabled)
    }

    override suspend fun updateNotificationsEnabled(enabled: Boolean) {
        userPreferencesDataStore.updateNotificationsEnabled(enabled)
    }

    override suspend fun updateWeeklyRemindersEnabled(enabled: Boolean) {
        userPreferencesDataStore.updateWeeklyRemindersEnabled(enabled)
    }

    override suspend fun updateGoalCompletionNotifications(enabled: Boolean) {
        userPreferencesDataStore.updateGoalCompletionNotifications(enabled)
    }

    override suspend fun updateDarkModeEnabled(enabled: Boolean) {
        userPreferencesDataStore.updateDarkModeEnabled(enabled)
    }

    override suspend fun updateLanguageCode(languageCode: String) {
        userPreferencesDataStore.updateLanguageCode(languageCode)
    }

    override suspend fun updateAutoBackupEnabled(enabled: Boolean) {
        userPreferencesDataStore.updateAutoBackupEnabled(enabled)
    }

    override suspend fun updateAnalyticsEnabled(enabled: Boolean) {
        userPreferencesDataStore.updateAnalyticsEnabled(enabled)
    }

    override suspend fun resetToDefaults() {
        userPreferencesDataStore.resetToDefaults()
    }
}
