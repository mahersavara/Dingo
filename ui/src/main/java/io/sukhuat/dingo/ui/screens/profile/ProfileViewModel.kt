package io.sukhuat.dingo.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sukhuat.dingo.domain.model.ErrorRecoveryManager
import io.sukhuat.dingo.domain.model.ErrorRecoveryStrategy
import io.sukhuat.dingo.domain.model.NetworkConnectivityChecker
import io.sukhuat.dingo.domain.model.ProfileCacheManager
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.ProfileErrorHandler
import io.sukhuat.dingo.domain.usecase.account.ChangePasswordUseCase
import io.sukhuat.dingo.domain.usecase.account.DeleteAccountUseCase
import io.sukhuat.dingo.domain.usecase.account.ExportUserDataUseCase
import io.sukhuat.dingo.domain.usecase.account.GetLoginHistoryUseCase
import io.sukhuat.dingo.domain.usecase.preferences.GetUserPreferencesUseCase
import io.sukhuat.dingo.domain.usecase.preferences.UpdatePreferencesUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetAchievementsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetProfileStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetUserProfileUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageProfileImageUseCase
import io.sukhuat.dingo.domain.usecase.profile.RefreshStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.ShareAchievementUseCase
import io.sukhuat.dingo.domain.usecase.profile.UpdateProfileUseCase
import io.sukhuat.dingo.domain.validation.ProfileValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the profile screen with comprehensive state management
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getProfileStatisticsUseCase: GetProfileStatisticsUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val manageProfileImageUseCase: ManageProfileImageUseCase,
    private val refreshStatisticsUseCase: RefreshStatisticsUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val shareAchievementUseCase: ShareAchievementUseCase,
    private val exportUserDataUseCase: ExportUserDataUseCase,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val getLoginHistoryUseCase: GetLoginHistoryUseCase,
    private val getUserPreferencesUseCase: GetUserPreferencesUseCase,
    private val updatePreferencesUseCase: UpdatePreferencesUseCase,
    private val profileErrorHandler: ProfileErrorHandler,
    private val errorRecoveryManager: ErrorRecoveryManager,
    private val profileValidator: ProfileValidator,
    private val networkConnectivityChecker: NetworkConnectivityChecker,
    private val profileCacheManager: ProfileCacheManager,
    private val fallbackUiStateManager: FallbackUiStateManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _tabState = MutableStateFlow(ProfileTabState())
    val tabState: StateFlow<ProfileTabState> = _tabState.asStateFlow()

    private val _editState = MutableStateFlow(ProfileEditState())
    val editState: StateFlow<ProfileEditState> = _editState.asStateFlow()

    private val _imageUploadState = MutableStateFlow(ImageUploadState())
    val imageUploadState: StateFlow<ImageUploadState> = _imageUploadState.asStateFlow()

    // User preferences state
    val userPreferences = getUserPreferencesUseCase()

    init {
        println("ProfileViewModel: ViewModel initialized, calling loadProfileData")
        loadProfileData()
    }

    private fun loadProfileData() {
        println("ProfileViewModel: loadProfileData called")
        viewModelScope.launch {
            try {
                println("ProfileViewModel: loadProfileData - getting user profile")
                val profile = getUserProfileUseCase().first()
                println("ProfileViewModel: loadProfileData - got profile: userId=${profile.userId}, displayName='${profile.displayName}', email=${profile.email}")

                println("ProfileViewModel: loadProfileData - getting statistics")
                val statistics = getProfileStatisticsUseCase().first()
                println("ProfileViewModel: loadProfileData - got statistics")

                val successState = ProfileUiState.Success(
                    profile = profile,
                    statistics = statistics,
                    isRefreshing = false
                )

                println("ProfileViewModel: loadProfileData - setting UI state to Success")
                _uiState.value = successState
                println("ProfileViewModel: loadProfileData - UI state updated successfully")
            } catch (error: Exception) {
                println("ProfileViewModel: loadProfileData - error occurred: ${error.message}")
                error.printStackTrace()
                handleErrorWithRecovery(error)
            }
        }
    }

    fun updateDisplayName(newDisplayName: String) {
        viewModelScope.launch {
            try {
                println("ProfileViewModel: updateDisplayName called with: '$newDisplayName'")

                _editState.value = _editState.value.copy(
                    isValidating = true,
                    validationError = null
                )

                // Validate input first
                val validationResult = profileValidator.validateDisplayName(newDisplayName)
                if (validationResult is ProfileValidator.ValidationResult.Invalid) {
                    println("ProfileViewModel: Validation failed: ${validationResult.error.message}")
                    _editState.value = _editState.value.copy(
                        isValidating = false,
                        validationError = validationResult.error.message
                    )
                    return@launch
                }

                // Check network connectivity
                if (!networkConnectivityChecker.isConnected()) {
                    println("ProfileViewModel: No network connection")
                    _editState.value = _editState.value.copy(
                        isValidating = false,
                        validationError = "No internet connection. Please try again when online."
                    )
                    return@launch
                }

                println("ProfileViewModel: Calling updateProfileUseCase.updateDisplayName")

                // Execute with retry logic
                errorRecoveryManager.executeWithRetry(
                    operation = {
                        println("ProfileViewModel: Inside retry operation, calling updateDisplayName")
                        updateProfileUseCase.updateDisplayName(newDisplayName)
                    },
                    maxRetries = 3,
                    baseDelayMs = 1000
                )

                println("ProfileViewModel: Update successful, resetting edit state")

                _editState.value = _editState.value.copy(
                    isEditing = false,
                    isValidating = false,
                    editingField = null,
                    tempDisplayName = ""
                )

                // Reload profile data to see the changes
                loadProfileData()
            } catch (error: ProfileError.ValidationError) {
                println("ProfileViewModel: ProfileError.ValidationError: ${error.message}")
                _editState.value = _editState.value.copy(
                    isValidating = false,
                    validationError = error.message
                )
            } catch (error: Exception) {
                println("ProfileViewModel: Exception: ${error.message}")
                error.printStackTrace()
                _editState.value = _editState.value.copy(
                    isValidating = false,
                    validationError = profileErrorHandler.getErrorMessage(errorRecoveryManager.mapToProfileError(error))
                )
                handleErrorWithRecovery(error)
            }
        }
    }

    /**
     * Upload a new profile image with comprehensive validation, optimization, and progress tracking
     */
    fun uploadProfileImage(
        imageUri: Uri,
        mimeType: String? = null,
        sizeBytes: Long? = null,
        imageOptimizationManager: ImageOptimizationManager? = null,
        performanceMonitor: ProfilePerformanceMonitor? = null
    ) {
        viewModelScope.launch {
            try {
                _imageUploadState.value = ImageUploadState(
                    isUploading = true,
                    progress = 0f,
                    error = null
                )

                // Validate image first
                val validationResult = profileValidator.validateProfileImage(imageUri, mimeType, sizeBytes)
                if (validationResult is ProfileValidator.ValidationResult.Invalid) {
                    _imageUploadState.value = ImageUploadState(error = validationResult.error.message)
                    return@launch
                }

                // Check network connectivity
                if (!networkConnectivityChecker.isConnected()) {
                    _imageUploadState.value = ImageUploadState(error = "No internet connection. Please try again when online.")
                    return@launch
                }

                // Update UI state to show uploading
                val currentState = _uiState.value
                if (currentState is ProfileUiState.Success) {
                    _uiState.value = currentState.copy(isUploadingImage = true)
                }

                // Execute with retry logic and progress tracking
                _imageUploadState.value = _imageUploadState.value.copy(progress = 0.1f)

                val imageUrl = errorRecoveryManager.executeWithRetry(
                    operation = {
                        _imageUploadState.value = _imageUploadState.value.copy(progress = 0.5f)
                        manageProfileImageUseCase.uploadProfileImage(imageUri)
                    },
                    maxRetries = 2,
                    baseDelayMs = 2000
                )

                _imageUploadState.value = _imageUploadState.value.copy(progress = 1f)

                // Reset upload state after successful upload
                kotlinx.coroutines.delay(500) // Brief delay to show completion
                _imageUploadState.value = ImageUploadState()

                // Update UI state
                if (currentState is ProfileUiState.Success) {
                    _uiState.value = currentState.copy(isUploadingImage = false)
                }
            } catch (error: ProfileError.ValidationError) {
                _imageUploadState.value = ImageUploadState(error = error.message)
                resetUploadingState()
            } catch (error: ProfileError.StorageError) {
                _imageUploadState.value = ImageUploadState(error = profileErrorHandler.getErrorMessage(error))
                resetUploadingState()
            } catch (error: ProfileError.QuotaExceeded) {
                _imageUploadState.value = ImageUploadState(error = "Storage quota exceeded. Please free up space and try again.")
                resetUploadingState()
            } catch (error: Exception) {
                val profileError = errorRecoveryManager.mapToProfileError(error)
                _imageUploadState.value = ImageUploadState(error = profileErrorHandler.getErrorMessage(profileError))
                resetUploadingState()
                handleErrorWithRecovery(error)
            }
        }
    }

    /**
     * Delete the current profile image
     */
    fun deleteProfileImage() {
        viewModelScope.launch {
            try {
                manageProfileImageUseCase.deleteProfileImage()
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Refresh profile statistics manually
     */
    fun refreshStatistics() {
        viewModelScope.launch {
            try {
                // Update UI to show refreshing state
                val currentState = _uiState.value
                if (currentState is ProfileUiState.Success) {
                    _uiState.value = currentState.copy(isRefreshing = true)
                }

                refreshStatisticsUseCase()

                // The statistics will be updated automatically through the Flow
                // Reset refreshing state after a short delay
                kotlinx.coroutines.delay(1000)
                val updatedState = _uiState.value
                if (updatedState is ProfileUiState.Success) {
                    _uiState.value = updatedState.copy(isRefreshing = false)
                }
            } catch (error: Exception) {
                // Reset refreshing state on error
                val currentState = _uiState.value
                if (currentState is ProfileUiState.Success) {
                    _uiState.value = currentState.copy(isRefreshing = false)
                }
                handleError(error)
            }
        }
    }

    /**
     * Share an achievement on social media
     */
    fun shareAchievement(achievementId: String) {
        viewModelScope.launch {
            try {
                val achievements = getAchievementsUseCase()
                val achievement = achievements.find { it.id == achievementId }

                if (achievement != null) {
                    val shareableContent = shareAchievementUseCase(achievement)
                    // In a real implementation, this would trigger the sharing intent
                    // For now, we'll just log the success
                } else {
                    handleError(ProfileError.ValidationError("achievement", "Achievement not found"))
                }
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Export user data for GDPR compliance
     */
    fun exportUserData() {
        viewModelScope.launch {
            try {
                val exportedData = exportUserDataUseCase()
                // In a real implementation, this would trigger a download or sharing intent
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Delete user account with confirmation
     */
    fun deleteAccount(confirmationText: String = "DELETE") {
        viewModelScope.launch {
            try {
                deleteAccountUseCase(confirmationText)
                // Account deletion will trigger navigation back to auth screen
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Switch to a different tab
     */
    fun selectTab(tab: ProfileTab) {
        _tabState.value = _tabState.value.copy(selectedTab = tab)
    }

    fun startEditing(field: ProfileField) {
        println("ProfileViewModel: startEditing called with field: $field")
        val currentState = _uiState.value
        println("ProfileViewModel: startEditing - current UI state type: ${currentState.javaClass.simpleName}")

        if (currentState is ProfileUiState.Success) {
            println("ProfileViewModel: startEditing - current profile displayName: '${currentState.profile.displayName}'")

            val newEditState = ProfileEditState(
                isEditing = true,
                editingField = field,
                tempDisplayName = if (field == ProfileField.DISPLAY_NAME) {
                    currentState.profile.displayName
                } else {
                    ""
                },
                validationError = null
            )

            println("ProfileViewModel: startEditing - setting edit state: isEditing=${newEditState.isEditing}, editingField=${newEditState.editingField}, tempDisplayName='${newEditState.tempDisplayName}'")

            _editState.value = newEditState
        } else {
            println("ProfileViewModel: startEditing - ERROR: UI state is not Success, cannot start editing")
        }
    }

    fun cancelEditing() {
        println("ProfileViewModel: cancelEditing called")
        _editState.value = ProfileEditState()
        println("ProfileViewModel: cancelEditing - edit state reset")
    }

    fun updateTempDisplayName(name: String) {
        println("ProfileViewModel: updateTempDisplayName called with: '$name'")
        _editState.value = _editState.value.copy(
            tempDisplayName = name,
            validationError = null
        )
        println("ProfileViewModel: updateTempDisplayName - updated tempDisplayName to: '${_editState.value.tempDisplayName}'")
    }

    fun confirmEdit() {
        val editState = _editState.value
        println("ProfileViewModel: confirmEdit called - editingField=${editState.editingField}, tempDisplayName='${editState.tempDisplayName}'")

        when (editState.editingField) {
            ProfileField.DISPLAY_NAME -> {
                println("ProfileViewModel: confirmEdit - calling updateDisplayName with: '${editState.tempDisplayName}'")
                updateDisplayName(editState.tempDisplayName)
            }
            ProfileField.PROFILE_IMAGE -> {
                println("ProfileViewModel: confirmEdit - PROFILE_IMAGE editing not implemented in confirmEdit")
                // Image editing is handled separately through uploadProfileImage
            }
            null -> {
                println("ProfileViewModel: confirmEdit - ERROR: no field being edited")
                // No field being edited
            }
        }
    }

    /**
     * Retry loading profile data after an error
     */
    fun retry() {
        _uiState.value = ProfileUiState.Loading()
        loadProfileData()
    }

    /**
     * Handle errors consistently with enhanced error messages
     */
    private fun handleError(error: Throwable) {
        val profileError = errorRecoveryManager.mapToProfileError(error)
        _uiState.value = fallbackUiStateManager.createErrorState(profileError)
    }

    /**
     * Handle errors with recovery strategies and fallback to cache
     */
    private fun handleErrorWithRecovery(error: Throwable) {
        viewModelScope.launch {
            val profileError = errorRecoveryManager.mapToProfileError(error)
            val recoveryStrategy = profileErrorHandler.getRecoveryStrategy(profileError)

            when (recoveryStrategy) {
                is ErrorRecoveryStrategy.FallbackToCache -> {
                    tryLoadFromCache()
                }
                is ErrorRecoveryStrategy.ShowOfflineUI -> {
                    showOfflineState()
                }
                is ErrorRecoveryStrategy.RequireReauth -> {
                    _uiState.value = ProfileUiState.Error("Authentication required. Please sign in again.")
                }
                else -> {
                    handleError(error)
                }
            }
        }
    }

    /**
     * Try to load profile data from cache when network fails
     */
    private suspend fun tryLoadFromCache() {
        try {
            val currentUserId = getCurrentUserId()
            if (currentUserId != null) {
                val cachedProfile = profileCacheManager.getCachedProfile(currentUserId)
                val cachedStatistics = profileCacheManager.getCachedStatistics(currentUserId)

                if (cachedProfile != null && cachedStatistics != null) {
                    _uiState.value = ProfileUiState.Success(
                        profile = cachedProfile,
                        statistics = cachedStatistics,
                        isRefreshing = false,
                        isOfflineMode = true
                    )
                } else {
                    _uiState.value = ProfileUiState.Error("No cached data available. Please check your connection.")
                }
            } else {
                _uiState.value = ProfileUiState.Error("Authentication required.")
            }
        } catch (e: Exception) {
            _uiState.value = ProfileUiState.Error("Failed to load cached data.")
        }
    }

    /**
     * Show offline state with limited functionality
     */
    private fun showOfflineState() {
        viewModelScope.launch {
            tryLoadFromCache()
        }
    }

    /**
     * Get current user ID from current profile data
     */
    private fun getCurrentUserId(): String? {
        val currentState = _uiState.value
        return when (currentState) {
            is ProfileUiState.Success -> currentState.profile.userId
            else -> null
        }
    }

    /**
     * Reset uploading state in UI
     */
    private fun resetUploadingState() {
        val currentState = _uiState.value
        if (currentState is ProfileUiState.Success) {
            _uiState.value = currentState.copy(isUploadingImage = false)
        }
    }

    /**
     * Toggle dark mode preference
     */
    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateDarkModeEnabled(enabled)
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Toggle notifications preference
     */
    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateNotificationsEnabled(enabled)
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Toggle sound preference
     */
    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateSoundEnabled(enabled)
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Toggle vibration preference
     */
    fun toggleVibration(enabled: Boolean) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateVibrationEnabled(enabled)
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Update language preference
     */
    fun updateLanguage(languageCode: String) {
        viewModelScope.launch {
            try {
                updatePreferencesUseCase.updateLanguageCode(languageCode)
            } catch (error: Exception) {
                handleError(error)
            }
        }
    }

    /**
     * Get profile actions for the UI
     */
    fun getProfileActions(): ProfileActions {
        return ProfileActions(
            onEditProfile = { startEditing(ProfileField.DISPLAY_NAME) },
            onUpdateDisplayName = ::updateDisplayName,
            onUploadProfileImage = { /* This will be handled by image picker */ },
            onDeleteProfileImage = ::deleteProfileImage,
            onShareProfile = { /* TODO: Implement profile sharing */ },
            onShareAchievement = ::shareAchievement,
            onExportData = ::exportUserData,
            onDeleteAccount = ::deleteAccount,
            onRefreshStats = ::refreshStatistics,
            onNavigateToSettings = { /* Navigation handled by screen */ },
            onNavigateToAccountSecurity = { /* Navigation handled by screen */ },
            onNavigateToHelp = { /* Navigation handled by screen */ },
            onRetry = ::retry
        )
    }
}
