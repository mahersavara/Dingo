package io.sukhuat.dingo.domain.usecase.preferences

import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import javax.inject.Inject

/**
 * Use case for toggling notification settings with proper dependency handling
 */
class ToggleNotificationSettingsUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {

    /**
     * Toggle main notifications setting
     * When disabled, this will also disable all sub-notification settings
     */
    suspend fun toggleMainNotifications(enabled: Boolean) {
        userPreferencesRepository.updateNotificationsEnabled(enabled)

        // If main notifications are disabled, disable all sub-notifications
        if (!enabled) {
            userPreferencesRepository.updateWeeklyRemindersEnabled(false)
            userPreferencesRepository.updateGoalCompletionNotifications(false)
        }
    }

    /**
     * Toggle weekly reminders
     * This will automatically enable main notifications if they're disabled
     */
    suspend fun toggleWeeklyReminders(enabled: Boolean) {
        if (enabled) {
            // Enable main notifications if enabling weekly reminders
            userPreferencesRepository.updateNotificationsEnabled(true)
        }
        userPreferencesRepository.updateWeeklyRemindersEnabled(enabled)
    }

    /**
     * Toggle goal completion notifications
     * This will automatically enable main notifications if they're disabled
     */
    suspend fun toggleGoalCompletionNotifications(enabled: Boolean) {
        if (enabled) {
            // Enable main notifications if enabling goal completion notifications
            userPreferencesRepository.updateNotificationsEnabled(true)
        }
        userPreferencesRepository.updateGoalCompletionNotifications(enabled)
    }
}
