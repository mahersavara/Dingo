package io.sukhuat.dingo.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.components.LoadingIndicator
import io.sukhuat.dingo.common.components.ResponsiveValues
import io.sukhuat.dingo.common.components.rememberResponsiveValues
import io.sukhuat.dingo.common.localization.AppLanguage
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.ui.screens.profile.components.PasswordChangeDialog
import io.sukhuat.dingo.ui.screens.profile.components.ProfileHeader
import io.sukhuat.dingo.ui.screens.profile.components.ProfileQuickActions
import io.sukhuat.dingo.ui.screens.profile.components.ProfileStatistics

/**
 * Main profile screen with tabbed interface displaying user profile information,
 * statistics, account management, and help sections
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    performanceMonitor: ProfilePerformanceMonitor = remember { ProfilePerformanceMonitor() }
) {
    // Monitor screen performance
    performanceMonitor.MonitorScreenPerformance("ProfileScreen")

    // Memory-efficient state management
    val uiState by viewModel.uiState.collectAsState()
    val tabState by viewModel.tabState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val imageUploadState by viewModel.imageUploadState.collectAsState()

    // Lazy loading manager for achievements
    val lazyLoadingManager = remember { LazyLoadingManager() }
    val memoryEfficientRenderer = lazyLoadingManager.rememberMemoryEfficientRenderer()
    val preferences by viewModel.userPreferences.collectAsState(initial = UserPreferences())
    val currentLanguage = io.sukhuat.dingo.common.localization.LocalAppLanguage.current

    val responsiveValues = rememberResponsiveValues()
    val snackbarHostState = remember { SnackbarHostState() }
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // Pull-to-refresh state
    val pullToRefreshState = rememberPullToRefreshState()

    // Simplified sync - remove LaunchedEffect to prevent infinite loops
    // Only sync when user explicitly clicks tabs

    // Handle pull-to-refresh
    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            viewModel.refreshStatistics()
            pullToRefreshState.endRefresh()
        }
    }

    // Get profile actions
    val profileActions = remember(viewModel) { viewModel.getProfileActions() }

    DingoScaffold(
        title = stringResource(R.string.profile),
        navigationIcon = {
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateBack()
                },
                modifier = Modifier.semantics {
                    contentDescription = "Navigate back"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Navigate back",
                    tint = Color.White
                )
            }
        },
        topBarActions = {
            // Refresh button
            IconButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.refreshStatistics()
                },
                modifier = Modifier.semantics {
                    contentDescription = "Refresh profile data"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh profile data",
                    tint = Color.White
                )
            }
        },
        snackbarHostState = snackbarHostState,
        contentPadding = PaddingValues(0.dp),
        userProfile = (uiState as? ProfileUiState.Success)?.profile
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = uiState) {
                is ProfileUiState.Loading -> {
                    LoadingIndicator(
                        isFullScreen = true,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                is ProfileUiState.Success -> {
                    ProfileContent(
                        profile = currentState.profile,
                        statistics = currentState.statistics,
                        tabState = tabState,
                        editState = editState,
                        imageUploadState = imageUploadState,
                        isRefreshing = currentState.isRefreshing,
                        responsiveValues = responsiveValues,
                        profileActions = profileActions,
                        preferences = preferences,
                        currentLanguage = currentLanguage,
                        onTabSelected = viewModel::selectTab,
                        onNavigateToSettings = onNavigateToSettings,
                        onStartEditing = viewModel::startEditing,
                        onCancelEditing = viewModel::cancelEditing,
                        onConfirmEdit = viewModel::confirmEdit,
                        onUpdateTempDisplayName = viewModel::updateTempDisplayName,
                        onUploadProfileImage = { uri -> viewModel.uploadProfileImage(uri) },
                        onDeleteProfileImage = viewModel::deleteProfileImage,
                        onShareAchievement = viewModel::shareAchievement,
                        onRefreshStats = viewModel::refreshStatistics,
                        onExportData = viewModel::exportUserData,
                        onDeleteAccount = viewModel::deleteAccount,
                        onToggleDarkMode = viewModel::toggleDarkMode,
                        onToggleNotifications = viewModel::toggleNotifications,
                        onToggleSound = viewModel::toggleSound,
                        onToggleVibration = viewModel::toggleVibration,
                        onLanguageChange = viewModel::updateLanguage,
                        onChangePassword = viewModel::changePassword
                    )
                }

                is ProfileUiState.Error -> {
                    ProfileErrorContent(
                        errorMessage = currentState.message,
                        onRetry = viewModel::retry,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Pull-to-refresh indicator
            PullToRefreshContainer(
                state = pullToRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

/**
 * Main profile content with tabbed interface
 */
@Composable
fun ProfileContent(
    profile: UserProfile,
    statistics: ProfileStatistics,
    tabState: ProfileTabState,
    editState: ProfileEditState,
    imageUploadState: ImageUploadState,
    isRefreshing: Boolean,
    responsiveValues: ResponsiveValues,
    profileActions: ProfileActions,
    preferences: io.sukhuat.dingo.domain.model.UserPreferences,
    currentLanguage: io.sukhuat.dingo.common.localization.AppLanguage,
    onTabSelected: (ProfileTab) -> Unit,
    onNavigateToSettings: () -> Unit,
    onStartEditing: (ProfileField) -> Unit,
    onCancelEditing: () -> Unit,
    onConfirmEdit: () -> Unit,
    onUpdateTempDisplayName: (String) -> Unit,
    onUploadProfileImage: (android.net.Uri) -> Unit,
    onDeleteProfileImage: () -> Unit,
    onShareAchievement: (String) -> Unit,
    onRefreshStats: () -> Unit,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onChangePassword: (String, String, String) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    // State for password change dialog
    var showPasswordChangeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Profile overview"
            }
    ) {
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = tabState.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabState.selectedTab.ordinal]),
                    color = RusticGold,
                    height = 3.dp
                )
            },
            divider = {}
        ) {
            tabState.tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = tabState.selectedTab == tab,
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onTabSelected(tab)
                        // Direct tab selection without pager
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "${tab.title} tab"
                        stateDescription = if (tabState.selectedTab == tab) "Selected" else "Not selected"
                    }
                ) {
                    Text(
                        text = stringResource(getTabTitleResource(tab)),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (tabState.selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                        color = if (tabState.selectedTab == tab) RusticGold else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
                    )
                }
            }
        }

        // Simple Tab Content without Pager
        when (tabState.selectedTab) {
            ProfileTab.OVERVIEW -> {
                ProfileOverviewTab(
                    profile = profile,
                    statistics = statistics,
                    editState = editState,
                    imageUploadState = imageUploadState,
                    isRefreshing = isRefreshing,
                    responsiveValues = responsiveValues,
                    preferences = preferences,
                    currentLanguage = currentLanguage,
                    onStartEditing = onStartEditing,
                    onCancelEditing = onCancelEditing,
                    onConfirmEdit = onConfirmEdit,
                    onUpdateTempDisplayName = onUpdateTempDisplayName,
                    onUploadProfileImage = onUploadProfileImage,
                    onDeleteProfileImage = onDeleteProfileImage,
                    onChangePasswordClick = { showPasswordChangeDialog = true },
                    onNavigateToSettings = onNavigateToSettings,
                    onToggleDarkMode = onToggleDarkMode,
                    onToggleNotifications = onToggleNotifications,
                    onToggleSound = onToggleSound,
                    onToggleVibration = onToggleVibration,
                    onLanguageChange = onLanguageChange
                )
            }

            ProfileTab.STATISTICS -> {
                ProfileStatisticsTab(
                    statistics = statistics,
                    isRefreshing = isRefreshing,
                    responsiveValues = responsiveValues,
                    onShareAchievement = onShareAchievement,
                    onRefreshStats = onRefreshStats
                )
            }

            ProfileTab.ACCOUNT -> {
                ProfileAccountTab(
                    profile = profile,
                    responsiveValues = responsiveValues,
                    onExportData = onExportData,
                    onDeleteAccount = onDeleteAccount
                )
            }

            ProfileTab.HELP -> {
                ProfileHelpTab(
                    responsiveValues = responsiveValues
                )
            }
        }

        // Password change dialog
        if (showPasswordChangeDialog) {
            PasswordChangeDialog(
                onDismiss = { showPasswordChangeDialog = false },
                onPasswordChange = { currentPassword, newPassword, confirmPassword ->
                    showPasswordChangeDialog = false
                    // Call ViewModel to change password
                    onChangePassword(currentPassword, newPassword, confirmPassword)
                }
            )
        }
    }
}

/**
 * Overview tab content
 */
@Composable
private fun ProfileOverviewTab(
    profile: UserProfile,
    statistics: ProfileStatistics,
    editState: ProfileEditState,
    imageUploadState: ImageUploadState,
    isRefreshing: Boolean,
    responsiveValues: ResponsiveValues,
    preferences: io.sukhuat.dingo.domain.model.UserPreferences,
    currentLanguage: io.sukhuat.dingo.common.localization.AppLanguage,
    onStartEditing: (ProfileField) -> Unit,
    onCancelEditing: () -> Unit,
    onConfirmEdit: () -> Unit,
    onUpdateTempDisplayName: (String) -> Unit,
    onUploadProfileImage: (android.net.Uri) -> Unit,
    onDeleteProfileImage: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Profile overview" },
        contentPadding = PaddingValues(responsiveValues.contentPadding),
        verticalArrangement = Arrangement.spacedBy(responsiveValues.gridSpacing)
    ) {
        item {
            ProfileHeader(
                profile = profile,
                editState = editState,
                imageUploadState = imageUploadState,
                onStartEditing = onStartEditing,
                onCancelEditing = onCancelEditing,
                onConfirmEdit = onConfirmEdit,
                onUpdateTempDisplayName = onUpdateTempDisplayName,
                onUploadProfileImage = onUploadProfileImage,
                onDeleteProfileImage = onDeleteProfileImage,
                onChangePasswordClick = onChangePasswordClick
            )
        }

        item {
            ProfileQuickActions(
                preferences = preferences,
                currentLanguage = currentLanguage,
                onToggleDarkMode = onToggleDarkMode,
                onToggleNotifications = onToggleNotifications,
                onToggleSound = onToggleSound,
                onToggleVibration = onToggleVibration,
                onLanguageChange = onLanguageChange,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        if (statistics.totalGoalsCreated > 0) {
            item {
                Text(
                    text = stringResource(R.string.quick_stats),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .semantics {
                            heading()
                            contentDescription = "Quick statistics section"
                        }
                )
            }

            item {
                ProfileStatistics(
                    statistics = statistics,
                    onShareAchievement = { /* Handled in statistics tab */ },
                    onRefreshStats = { /* Handled by pull-to-refresh */ },
                    isRefreshing = isRefreshing
                )
            }
        }
    }
}

/**
 * Statistics tab content
 */
@Composable
private fun ProfileStatisticsTab(
    statistics: ProfileStatistics,
    isRefreshing: Boolean,
    responsiveValues: ResponsiveValues,
    onShareAchievement: (String) -> Unit,
    onRefreshStats: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Profile statistics" },
        contentPadding = PaddingValues(responsiveValues.contentPadding),
        verticalArrangement = Arrangement.spacedBy(responsiveValues.gridSpacing)
    ) {
        item {
            ProfileStatistics(
                statistics = statistics,
                onShareAchievement = onShareAchievement,
                onRefreshStats = onRefreshStats,
                isRefreshing = isRefreshing
            )
        }
    }
}

/**
 * Account tab content
 */
@Composable
private fun ProfileAccountTab(
    profile: UserProfile,
    responsiveValues: ResponsiveValues,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Account management" },
        contentPadding = PaddingValues(responsiveValues.contentPadding),
        verticalArrangement = Arrangement.spacedBy(responsiveValues.gridSpacing)
    ) {
        item {
            // Temporarily comment out AccountSecurity until we fix the component parameters
            Text(
                text = stringResource(R.string.account_security),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                    contentDescription = "Account security section"
                }
            )
            Text(
                text = stringResource(R.string.account_security_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.semantics {
                    contentDescription = "Account security features placeholder"
                }
            )
        }

        item {
            // Temporarily comment out DataManagement until we fix the component parameters
            Text(
                text = stringResource(R.string.data_management),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                    contentDescription = "Data management section"
                }
            )
            Text(
                text = stringResource(R.string.data_management_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.semantics {
                    contentDescription = "Data management features placeholder"
                }
            )
        }
    }
}

/**
 * Help tab content
 */
@Composable
private fun ProfileHelpTab(
    responsiveValues: ResponsiveValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Help and support" },
        contentPadding = PaddingValues(responsiveValues.contentPadding),
        verticalArrangement = Arrangement.spacedBy(responsiveValues.gridSpacing)
    ) {
        item {
            // Temporarily comment out HelpSupport until we fix the component parameters
            Text(
                text = stringResource(R.string.help_support),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    heading()
                    contentDescription = "Help and support section"
                }
            )
            Text(
                text = stringResource(R.string.help_support_placeholder),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.semantics {
                    contentDescription = "Help and support features placeholder"
                }
            )
        }
    }
}

/**
 * Error content for profile screen
 */
@Composable
fun ProfileErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.semantics {
            contentDescription = "Error loading profile"
        },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.error_loading_profile),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics {
                heading()
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        androidx.compose.material3.Button(
            onClick = onRetry,
            modifier = Modifier.semantics {
                contentDescription = "Retry loading profile"
            }
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}

/**
 * Loading content for profile screen
 */
@Composable
fun ProfileLoadingContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Loading profile"
            },
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator(
            isFullScreen = true
        )
    }
}

/**
 * Helper function to get string resource for tab titles
 */
@Composable
private fun getTabTitleResource(tab: ProfileTab): Int {
    return when (tab) {
        ProfileTab.OVERVIEW -> R.string.overview
        ProfileTab.STATISTICS -> R.string.statistics
        ProfileTab.ACCOUNT -> R.string.account_settings
        ProfileTab.HELP -> R.string.help_support
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MountainSunriseTheme {
        ProfileScreen(
            onNavigateBack = {},
            onNavigateToSettings = {}
        )
    }
}
