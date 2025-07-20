package io.sukhuat.dingo.ui.screens.profile.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.usecase.account.ExportUserDataUseCase
import io.sukhuat.dingo.domain.usecase.preferences.UpdatePreferencesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for DataManagement component
 */
@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val exportUserDataUseCase: ExportUserDataUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataManagementUiState())
    val uiState: StateFlow<DataManagementUiState> = _uiState.asStateFlow()

    /**
     * Show data export dialog
     */
    fun showDataExport() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            dataExportState = currentState.dataExportState.copy(
                showExportDialog = true,
                exportError = null,
                exportSuccess = false,
                exportedFilePath = null
            )
        )
    }

    /**
     * Hide data export dialog
     */
    fun hideDataExport() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            dataExportState = currentState.dataExportState.copy(
                showExportDialog = false
            )
        )
    }

    /**
     * Export user data
     */
    fun exportData() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    dataExportState = currentState.dataExportState.copy(
                        isExporting = true,
                        exportProgress = 0f,
                        exportError = null
                    )
                )

                // Simulate progress updates
                updateExportProgress(0.2f)
                kotlinx.coroutines.delay(500)

                updateExportProgress(0.5f)
                kotlinx.coroutines.delay(500)

                updateExportProgress(0.8f)
                kotlinx.coroutines.delay(500)

                // Export the data
                val exportedData = exportUserDataUseCase()

                updateExportProgress(1f)
                kotlinx.coroutines.delay(300)

                // Simulate file saving (in real implementation, this would save to device storage)
                val fileName = "dingo_data_export_${System.currentTimeMillis()}.json"
                val filePath = "/storage/emulated/0/Download/$fileName"

                val successState = _uiState.value
                _uiState.value = successState.copy(
                    dataExportState = successState.dataExportState.copy(
                        isExporting = false,
                        exportProgress = 0f,
                        exportSuccess = true,
                        exportedFilePath = filePath,
                        exportedDataSize = "${exportedData.length / 1024}KB",
                        showExportDialog = false
                    )
                )

                // Reset success flag after delay
                kotlinx.coroutines.delay(5000)
                val resetState = _uiState.value
                _uiState.value = resetState.copy(
                    dataExportState = resetState.dataExportState.copy(
                        exportSuccess = false,
                        exportedFilePath = null
                    )
                )
            } catch (error: ProfileError) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    dataExportState = currentState.dataExportState.copy(
                        isExporting = false,
                        exportProgress = 0f,
                        exportError = when (error) {
                            is ProfileError.NetworkUnavailable -> "No internet connection"
                            is ProfileError.AuthenticationExpired -> "Please sign in again"
                            else -> "Failed to export data"
                        }
                    )
                )
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    dataExportState = currentState.dataExportState.copy(
                        isExporting = false,
                        exportProgress = 0f,
                        exportError = "An unexpected error occurred"
                    )
                )
            }
        }
    }

    /**
     * Update data collection preference
     */
    fun updateDataCollection(allow: Boolean) {
        updatePrivacyPreference { currentState ->
            currentState.copy(
                privacyControlsState = currentState.privacyControlsState.copy(
                    allowDataCollection = allow
                )
            )
        }
    }

    /**
     * Update analytics preference
     */
    fun updateAnalytics(allow: Boolean) {
        updatePrivacyPreference { currentState ->
            currentState.copy(
                privacyControlsState = currentState.privacyControlsState.copy(
                    allowAnalytics = allow
                )
            )
        }
    }

    /**
     * Update personalization preference
     */
    fun updatePersonalization(allow: Boolean) {
        updatePrivacyPreference { currentState ->
            currentState.copy(
                privacyControlsState = currentState.privacyControlsState.copy(
                    allowPersonalization = allow
                )
            )
        }
    }

    /**
     * Update notifications preference
     */
    fun updateNotifications(allow: Boolean) {
        updatePrivacyPreference { currentState ->
            currentState.copy(
                privacyControlsState = currentState.privacyControlsState.copy(
                    allowNotifications = allow
                )
            )
        }
    }

    /**
     * Update profile visibility
     */
    fun updateProfileVisibility(visibility: ProfileVisibility) {
        updatePrivacyPreference { currentState ->
            currentState.copy(
                privacyControlsState = currentState.privacyControlsState.copy(
                    profileVisibility = visibility
                )
            )
        }
    }

    /**
     * Update share achievements preference
     */
    fun updateShareAchievements(allow: Boolean) {
        updatePrivacyPreference { currentState ->
            currentState.copy(
                privacyControlsState = currentState.privacyControlsState.copy(
                    shareAchievements = allow
                )
            )
        }
    }

    /**
     * Update share progress preference
     */
    fun updateShareProgress(allow: Boolean) {
        updatePrivacyPreference { currentState ->
            currentState.copy(
                privacyControlsState = currentState.privacyControlsState.copy(
                    shareProgress = allow
                )
            )
        }
    }

    /**
     * Show sign out dialog
     */
    fun showSignOut() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            signOutState = currentState.signOutState.copy(
                showSignOutDialog = true,
                signOutError = null
            )
        )
    }

    /**
     * Hide sign out dialog
     */
    fun hideSignOut() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            signOutState = currentState.signOutState.copy(
                showSignOutDialog = false
            )
        )
    }

    /**
     * Sign out user
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    signOutState = currentState.signOutState.copy(
                        isSigningOut = true,
                        signOutError = null
                    )
                )

                // Simulate sign out process
                kotlinx.coroutines.delay(1000)

                // In real implementation, this would:
                // 1. Clear Firebase Auth session
                // 2. Clear local preferences/cache
                // 3. Navigate back to authentication screen

                // For now, we'll just reset the state
                val successState = _uiState.value
                _uiState.value = successState.copy(
                    signOutState = successState.signOutState.copy(
                        isSigningOut = false,
                        showSignOutDialog = false
                    )
                )
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    signOutState = currentState.signOutState.copy(
                        isSigningOut = false,
                        signOutError = "Failed to sign out. Please try again."
                    )
                )
            }
        }
    }

    /**
     * Open exported file
     */
    fun openExportedFile(filePath: String) {
        // In real implementation, this would open the file with the system's default app
        // For now, we'll just show a message
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            error = "File saved to: $filePath"
        )
    }

    /**
     * Dismiss error message
     */
    fun dismissError() {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(error = null)
    }

    /**
     * Get actions for the UI
     */
    fun getActions(): DataManagementActions {
        return DataManagementActions(
            onShowDataExport = ::showDataExport,
            onHideDataExport = ::hideDataExport,
            onExportData = ::exportData,
            onUpdateDataCollection = ::updateDataCollection,
            onUpdateAnalytics = ::updateAnalytics,
            onUpdatePersonalization = ::updatePersonalization,
            onUpdateNotifications = ::updateNotifications,
            onUpdateProfileVisibility = ::updateProfileVisibility,
            onUpdateShareAchievements = ::updateShareAchievements,
            onUpdateShareProgress = ::updateShareProgress,
            onShowSignOut = ::showSignOut,
            onHideSignOut = ::hideSignOut,
            onSignOut = ::signOut,
            onDismissError = ::dismissError,
            onOpenExportedFile = ::openExportedFile
        )
    }

    /**
     * Helper function to update privacy preferences
     */
    private fun updatePrivacyPreference(
        updateFunction: (DataManagementUiState) -> DataManagementUiState
    ) {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val updatedState = updateFunction(currentState).copy(
                    privacyControlsState = currentState.privacyControlsState.copy(
                        isUpdatingPreferences = true
                    )
                )
                _uiState.value = updatedState

                // Simulate preference update
                kotlinx.coroutines.delay(500)

                // In real implementation, this would call updatePreferencesUseCase
                // with the appropriate preference values

                val finalState = _uiState.value
                _uiState.value = finalState.copy(
                    privacyControlsState = finalState.privacyControlsState.copy(
                        isUpdatingPreferences = false
                    )
                )
            } catch (error: Exception) {
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    privacyControlsState = currentState.privacyControlsState.copy(
                        isUpdatingPreferences = false
                    ),
                    error = "Failed to update preferences"
                )
            }
        }
    }

    /**
     * Update export progress
     */
    private fun updateExportProgress(progress: Float) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            dataExportState = currentState.dataExportState.copy(
                exportProgress = progress
            )
        )
    }
}
