package io.sukhuat.dingo.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sukhuat.dingo.data.notification.NotificationScheduler
import io.sukhuat.dingo.domain.usecase.preferences.GetUserPreferencesUseCase
import io.sukhuat.dingo.domain.usecase.preferences.ManageNotificationPermissionsUseCase
import io.sukhuat.dingo.domain.usecase.preferences.ToggleNotificationSettingsUseCase
import io.sukhuat.dingo.domain.usecase.preferences.UpdatePreferencesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the settings screen with enhanced notification management
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase,
    private val toggleNotificationSettingsUseCase: ToggleNotificationSettingsUseCase,
    private val manageNotificationPermissionsUseCase: ManageNotificationPermissionsUseCase,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Loading)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _notificationPermissionStatus = MutableStateFlow(manageNotificationPermissionsUseCase.getNotificationPermissionStatus())
    val notificationPermissionStatus: StateFlow<io.sukhuat.dingo.domain.usecase.preferences.NotificationPermissionStatus> = _notificationPermissionStatus.asStateFlow()

    init {
        loadUserPreferences()
        checkNotificationPermissions()
    }

    private fun loadUserPreferences() {
        getUserPreferencesUseCase()
            .onEach { preferences ->
                _uiState.value = SettingsUiState.Success(preferences)
            }
            .catch { error ->
                _uiState.value = SettingsUiState.Error("Failed to load preferences: ${error.message}")
            }
            .launchIn(viewModelScope)
    }

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateSoundEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update sound setting: ${e.message}")
            }
        }
    }

    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateVibrationEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update vibration setting: ${e.message}")
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                toggleNotificationSettingsUseCase.toggleMainNotifications(enabled)

                // Update notification scheduling based on the new setting
                if (enabled) {
                    scheduleNotificationsIfNeeded()
                } else {
                    notificationScheduler.cancelWeeklyReminder()
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update notifications setting: ${e.message}")
            }
        }
    }

    fun toggleWeeklyReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                toggleNotificationSettingsUseCase.toggleWeeklyReminders(enabled)

                // Schedule or cancel weekly reminders based on the new setting
                if (enabled) {
                    notificationScheduler.scheduleWeeklyReminder()
                } else {
                    notificationScheduler.cancelWeeklyReminder()
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update weekly reminders setting: ${e.message}")
            }
        }
    }

    fun toggleGoalCompletionNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                toggleNotificationSettingsUseCase.toggleGoalCompletionNotifications(enabled)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update goal completion notifications setting: ${e.message}")
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateDarkModeEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update dark mode setting: ${e.message}")
            }
        }
    }

    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateLanguageCode(languageCode)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update language setting: ${e.message}")
            }
        }
    }

    fun toggleAutoBackup(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateAutoBackupEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update auto backup setting: ${e.message}")
            }
        }
    }

    fun toggleAnalytics(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateAnalyticsEnabled(enabled)
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to update analytics setting: ${e.message}")
            }
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.resetToDefaults()
                // Cancel all scheduled notifications when resetting
                notificationScheduler.cancelWeeklyReminder()
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error("Failed to reset settings: ${e.message}")
            }
        }
    }

    /**
     * Check notification permissions and update status
     */
    fun checkNotificationPermissions() {
        _notificationPermissionStatus.value = manageNotificationPermissionsUseCase.getNotificationPermissionStatus()
    }

    /**
     * Get intent to open notification settings
     */
    fun getNotificationSettingsIntent() = manageNotificationPermissionsUseCase.getNotificationSettingsIntent()

    /**
     * Schedule notifications if needed based on current preferences
     */
    private suspend fun scheduleNotificationsIfNeeded() {
        val currentState = _uiState.value
        if (currentState is SettingsUiState.Success) {
            if (currentState.preferences.weeklyRemindersEnabled) {
                notificationScheduler.scheduleWeeklyReminder()
            }
        }
    }

    /**
     * Check if notification permissions are granted
     */
    fun hasNotificationPermission(): Boolean {
        return manageNotificationPermissionsUseCase.hasNotificationPermission()
    }
}
