package io.sukhuat.dingo.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.domain.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

/**
 * DataStore implementation for user preferences
 * Handles persistent storage of user settings using Jetpack DataStore
 */
@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        // Audio & Feedback preferences
        private val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")

        // Notification preferences
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val WEEKLY_REMINDERS_ENABLED = booleanPreferencesKey("weekly_reminders_enabled")
        private val GOAL_COMPLETION_NOTIFICATIONS = booleanPreferencesKey("goal_completion_notifications")

        // Appearance preferences
        private val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
        private val LANGUAGE_CODE = stringPreferencesKey("language_code")

        // Privacy & Data preferences
        private val AUTO_BACKUP_ENABLED = booleanPreferencesKey("auto_backup_enabled")
        private val ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
    }

    /**
     * Flow of user preferences with error handling
     */
    val userPreferences: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                soundEnabled = preferences[SOUND_ENABLED] ?: true,
                vibrationEnabled = preferences[VIBRATION_ENABLED] ?: true,
                notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true,
                weeklyRemindersEnabled = preferences[WEEKLY_REMINDERS_ENABLED] ?: true,
                goalCompletionNotifications = preferences[GOAL_COMPLETION_NOTIFICATIONS] ?: true,
                darkModeEnabled = preferences[DARK_MODE_ENABLED] ?: false,
                languageCode = preferences[LANGUAGE_CODE] ?: "en",
                autoBackupEnabled = preferences[AUTO_BACKUP_ENABLED] ?: true,
                analyticsEnabled = preferences[ANALYTICS_ENABLED] ?: true
            )
        }
        .catch { exception ->
            // If there's an error reading preferences, emit default values
            emit(UserPreferences())
        }

    // Audio & Feedback settings
    suspend fun updateSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED] = enabled
        }
    }

    // Notification settings
    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateWeeklyRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WEEKLY_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun updateGoalCompletionNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GOAL_COMPLETION_NOTIFICATIONS] = enabled
        }
    }

    // Appearance settings
    suspend fun updateDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_ENABLED] = enabled
        }
    }

    suspend fun updateLanguageCode(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE] = languageCode
        }
    }

    // Privacy & Data settings
    suspend fun updateAutoBackupEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_BACKUP_ENABLED] = enabled
        }
    }

    suspend fun updateAnalyticsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[ANALYTICS_ENABLED] = enabled
        }
    }

    /**
     * Reset all preferences to their default values
     */
    suspend fun resetToDefaults() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
