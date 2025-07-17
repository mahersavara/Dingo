package io.sukhuat.dingo.domain.usecase.preferences

import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting notification-specific preferences
 */
class GetNotificationPreferencesUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    /**
     * Get notification preferences as a flow
     */
    operator fun invoke(): Flow<NotificationPreferences> {
        return userPreferencesRepository.getUserPreferences().map { preferences ->
            NotificationPreferences(
                notificationsEnabled = preferences.notificationsEnabled,
                weeklyRemindersEnabled = preferences.weeklyRemindersEnabled,
                goalCompletionNotifications = preferences.goalCompletionNotifications
            )
        }
    }
}

/**
 * Data class representing notification-specific preferences
 */
data class NotificationPreferences(
    val notificationsEnabled: Boolean,
    val weeklyRemindersEnabled: Boolean,
    val goalCompletionNotifications: Boolean
) {
    /**
     * Check if any notifications are enabled
     */
    val hasAnyNotificationsEnabled: Boolean
        get() = notificationsEnabled && (weeklyRemindersEnabled || goalCompletionNotifications)
}
