package io.sukhuat.dingo.domain.repository

import io.sukhuat.dingo.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user preferences
 */
interface UserPreferencesRepository {
    /**
     * Get user preferences as a Flow
     */
    fun getUserPreferences(): Flow<UserPreferences>

    /**
     * Update sound enabled setting
     */
    suspend fun updateSoundEnabled(enabled: Boolean)

    /**
     * Update vibration enabled setting
     */
    suspend fun updateVibrationEnabled(enabled: Boolean)

    /**
     * Update notifications enabled setting
     */
    suspend fun updateNotificationsEnabled(enabled: Boolean)

    /**
     * Update weekly reminders enabled setting
     */
    suspend fun updateWeeklyRemindersEnabled(enabled: Boolean)

    /**
     * Update goal completion notifications setting
     */
    suspend fun updateGoalCompletionNotifications(enabled: Boolean)

    /**
     * Update dark mode enabled setting
     */
    suspend fun updateDarkModeEnabled(enabled: Boolean)

    /**
     * Update language code setting
     */
    suspend fun updateLanguageCode(languageCode: String)

    /**
     * Update auto backup enabled setting
     */
    suspend fun updateAutoBackupEnabled(enabled: Boolean)

    /**
     * Update analytics enabled setting
     */
    suspend fun updateAnalyticsEnabled(enabled: Boolean)

    /**
     * Reset all preferences to default values
     */
    suspend fun resetToDefaults()
}
