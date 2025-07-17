package io.sukhuat.dingo.ui.screens.settings

import io.sukhuat.dingo.domain.model.UserPreferences

/**
 * UI state for the settings screen
 */
sealed class SettingsUiState {
    object Loading : SettingsUiState()
    data class Success(val preferences: UserPreferences) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}

/**
 * Settings sections for organizing preferences
 */
enum class SettingsSection {
    NOTIFICATIONS,
    APPEARANCE,
    PRIVACY,
    ABOUT
}
