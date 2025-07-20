package io.sukhuat.dingo.ui.screens.profile.components

/**
 * UI state for data management operations
 */
data class DataManagementUiState(
    val isLoading: Boolean = false,
    val dataExportState: DataExportState = DataExportState(),
    val privacyControlsState: PrivacyControlsState = PrivacyControlsState(),
    val signOutState: SignOutState = SignOutState(),
    val error: String? = null
)

/**
 * State for data export operations
 */
data class DataExportState(
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportedDataSize: String? = null,
    val exportError: String? = null,
    val showExportDialog: Boolean = false,
    val exportSuccess: Boolean = false,
    val exportedFilePath: String? = null
)

/**
 * State for privacy controls and data sharing preferences
 */
data class PrivacyControlsState(
    val allowDataCollection: Boolean = true,
    val allowAnalytics: Boolean = true,
    val allowPersonalization: Boolean = true,
    val allowNotifications: Boolean = true,
    val profileVisibility: ProfileVisibility = ProfileVisibility.PRIVATE,
    val shareAchievements: Boolean = false,
    val shareProgress: Boolean = false,
    val isUpdatingPreferences: Boolean = false
)

/**
 * State for sign out operations
 */
data class SignOutState(
    val showSignOutDialog: Boolean = false,
    val isSigningOut: Boolean = false,
    val signOutError: String? = null
)

/**
 * Profile visibility options
 */
enum class ProfileVisibility(val displayName: String, val description: String) {
    PRIVATE("Private", "Only you can see your profile"),
    FRIENDS("Friends Only", "Only your friends can see your profile"),
    PUBLIC("Public", "Anyone can see your profile")
}

/**
 * Actions available in data management
 */
data class DataManagementActions(
    val onShowDataExport: () -> Unit,
    val onHideDataExport: () -> Unit,
    val onExportData: () -> Unit,
    val onUpdateDataCollection: (Boolean) -> Unit,
    val onUpdateAnalytics: (Boolean) -> Unit,
    val onUpdatePersonalization: (Boolean) -> Unit,
    val onUpdateNotifications: (Boolean) -> Unit,
    val onUpdateProfileVisibility: (ProfileVisibility) -> Unit,
    val onUpdateShareAchievements: (Boolean) -> Unit,
    val onUpdateShareProgress: (Boolean) -> Unit,
    val onShowSignOut: () -> Unit,
    val onHideSignOut: () -> Unit,
    val onSignOut: () -> Unit,
    val onDismissError: () -> Unit,
    val onOpenExportedFile: (String) -> Unit
)
