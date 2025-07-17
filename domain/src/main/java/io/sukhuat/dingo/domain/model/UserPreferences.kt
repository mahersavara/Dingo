package io.sukhuat.dingo.domain.model

/**
 * Domain model representing user preferences/settings
 */
data class UserPreferences(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val weeklyRemindersEnabled: Boolean = true,
    val goalCompletionNotifications: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val languageCode: String = "en",
    val autoBackupEnabled: Boolean = true,
    val analyticsEnabled: Boolean = true
)
