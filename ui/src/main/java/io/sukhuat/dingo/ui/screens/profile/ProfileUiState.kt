package io.sukhuat.dingo.ui.screens.profile

import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.ui.screens.profile.components.AccountSecurityUiState
import io.sukhuat.dingo.ui.screens.profile.components.DataManagementUiState

/**
 * UI state for the profile screen
 */
sealed class ProfileUiState {
    data class Loading(val message: String = "Loading profile...") : ProfileUiState()
    data class Success(
        val profile: UserProfile,
        val statistics: ProfileStatistics,
        val isRefreshing: Boolean = false,
        val isUploadingImage: Boolean = false,
        val uploadProgress: Float = 0f,
        val isOfflineMode: Boolean = false,
        val accountSecurityState: AccountSecurityUiState = AccountSecurityUiState(),
        val dataManagementState: DataManagementUiState = DataManagementUiState()
    ) : ProfileUiState()
    data class Error(
        val message: String,
        val isRetryable: Boolean = true,
        val errorType: String = "UnknownError"
    ) : ProfileUiState()
}

/**
 * Data class representing user interactions and actions available in the profile screen
 */
data class ProfileActions(
    val onEditProfile: () -> Unit,
    val onUpdateDisplayName: (String) -> Unit,
    val onUploadProfileImage: () -> Unit,
    val onDeleteProfileImage: () -> Unit,
    val onShareProfile: () -> Unit,
    val onShareAchievement: (String) -> Unit,
    val onExportData: () -> Unit,
    val onDeleteAccount: () -> Unit,
    val onRefreshStats: () -> Unit,
    val onNavigateToSettings: () -> Unit,
    val onNavigateToAccountSecurity: () -> Unit,
    val onNavigateToHelp: () -> Unit,
    val onRetry: () -> Unit
)

/**
 * State management for tab navigation within the profile screen
 */
data class ProfileTabState(
    val selectedTab: ProfileTab = ProfileTab.OVERVIEW,
    val tabs: List<ProfileTab> = ProfileTab.values().toList()
)

/**
 * Available tabs in the profile screen
 */
enum class ProfileTab(val title: String) {
    OVERVIEW("Overview"),
    STATISTICS("Statistics"),
    ACCOUNT("Account"),
    HELP("Help")
}

/**
 * State for profile editing operations
 */
data class ProfileEditState(
    val isEditing: Boolean = false,
    val editingField: ProfileField? = null,
    val tempDisplayName: String = "",
    val isValidating: Boolean = false,
    val validationError: String? = null
)

/**
 * Fields that can be edited in the profile
 */
enum class ProfileField {
    DISPLAY_NAME,
    PROFILE_IMAGE
}

/**
 * State for image upload operations
 */
data class ImageUploadState(
    val isUploading: Boolean = false,
    val progress: Float = 0f,
    val error: String? = null
)
