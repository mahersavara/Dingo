package io.sukhuat.dingo.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.components.GeneralItem
import io.sukhuat.dingo.common.components.LoadingIndicator
import io.sukhuat.dingo.common.components.TrailingContent
// import io.sukhuat.dingo.common.icons.MedievalIcons // REMOVED: Medieval theme deleted
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.localization.SupportedLanguages
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.UserPreferences

/**
 * Settings screen with organized sections for user preferences
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage = LocalAppLanguage.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is SettingsUiState.Loading -> {
                LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is SettingsUiState.Success -> {
                val successState = uiState as SettingsUiState.Success
                SettingsContent(
                    preferences = successState.preferences,
                    currentLanguage = currentLanguage.displayName,
                    onSoundToggle = viewModel::toggleSound,
                    onVibrationToggle = viewModel::toggleVibration,
                    onNotificationsToggle = viewModel::toggleNotifications,
                    onWeeklyRemindersToggle = viewModel::toggleWeeklyReminders,
                    onGoalCompletionNotificationsToggle = viewModel::toggleGoalCompletionNotifications,
                    onDarkModeToggle = viewModel::toggleDarkMode,
                    onLanguageClick = { showLanguageDialog = true },
                    onAutoBackupToggle = viewModel::toggleAutoBackup,
                    onAnalyticsToggle = viewModel::toggleAnalytics,
                    onResetClick = { showResetDialog = true },
                    onProfileClick = onNavigateToProfile,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is SettingsUiState.Error -> {
                val errorState = uiState as SettingsUiState.Error
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_settings),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = errorState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }

    // Reset confirmation dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_settings_title)) },
            text = { Text(stringResource(R.string.reset_settings_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text(stringResource(R.string.reset))
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguageCode = currentLanguage.code,
            onLanguageSelected = { languageCode ->
                onLanguageChange(languageCode)
                viewModel.updateLanguage(languageCode)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
private fun SettingsContent(
    preferences: UserPreferences,
    currentLanguage: String,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onWeeklyRemindersToggle: (Boolean) -> Unit,
    onGoalCompletionNotificationsToggle: (Boolean) -> Unit,
    onDarkModeToggle: (Boolean) -> Unit,
    onLanguageClick: () -> Unit,
    onAutoBackupToggle: (Boolean) -> Unit,
    onAnalyticsToggle: (Boolean) -> Unit,
    onResetClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Extract all string resources at the beginning
    val profileSectionTitle = stringResource(R.string.profile)
    val userProfileTitle = stringResource(R.string.user_profile)
    val userProfileDescription = stringResource(R.string.user_profile_description)
    val audioFeedbackSectionTitle = stringResource(R.string.audio_feedback)
    val soundEffectsTitle = stringResource(R.string.sound_effects)
    val soundEffectsDescription = stringResource(R.string.sound_effects_description)
    val vibrationTitle = stringResource(R.string.vibration)
    val vibrationDescription = stringResource(R.string.vibration_description)
    val notificationsSectionTitle = stringResource(R.string.notifications_settings)
    val notificationsTitle = stringResource(R.string.notifications_settings)
    val notificationsDescription = stringResource(R.string.notifications_description)
    val weeklyRemindersTitle = stringResource(R.string.weekly_reminders)
    val weeklyRemindersDescription = stringResource(R.string.weekly_reminders_description)
    val goalCompletionTitle = stringResource(R.string.goal_completion_notifications)
    val goalCompletionDescription = stringResource(R.string.goal_completion_description)
    val appearanceSectionTitle = stringResource(R.string.appearance)
    val darkModeTitle = stringResource(R.string.dark_mode)
    val darkModeDescription = stringResource(R.string.dark_mode_description)
    val languageTitle = stringResource(R.string.language)
    val currentLanguageLabel = stringResource(R.string.current_language, currentLanguage)
    val privacySectionTitle = stringResource(R.string.privacy_security)
    val autoBackupTitle = stringResource(R.string.auto_backup)
    val autoBackupDescription = stringResource(R.string.auto_backup_description)
    val analyticsTitle = stringResource(R.string.analytics)
    val analyticsDescription = stringResource(R.string.analytics_description)
    val aboutSectionTitle = stringResource(R.string.about_info)
    val appVersionTitle = stringResource(R.string.app_version)
    val resetSettingsTitle = stringResource(R.string.reset_settings)
    val resetSettingsDescription = stringResource(R.string.reset_settings_description)

    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        // Profile Section
        item {
            SettingsSectionHeader(title = profileSectionTitle)
        }

        items(
            getProfileSettings(
                onProfileClick = onProfileClick,
                userProfileTitle = userProfileTitle,
                userProfileDescription = userProfileDescription
            )
        ) { setting ->
            GeneralItem(
                title = setting.title,
                description = setting.description,
                leadingIcon = setting.icon,
                trailingContent = setting.trailingContent,
                onClick = setting.onClick
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Audio & Feedback Section
        item {
            SettingsSectionHeader(title = audioFeedbackSectionTitle)
        }

        items(
            getAudioFeedbackSettings(
                preferences = preferences,
                onSoundToggle = onSoundToggle,
                onVibrationToggle = onVibrationToggle,
                soundEffectsTitle = soundEffectsTitle,
                soundEffectsDescription = soundEffectsDescription,
                vibrationTitle = vibrationTitle,
                vibrationDescription = vibrationDescription
            )
        ) { setting ->
            GeneralItem(
                title = setting.title,
                description = setting.description,
                leadingIcon = setting.icon,
                trailingContent = setting.trailingContent
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Notifications Section
        item {
            SettingsSectionHeader(title = notificationsSectionTitle)
        }

        items(
            getNotificationSettings(
                preferences = preferences,
                onNotificationsToggle = onNotificationsToggle,
                onWeeklyRemindersToggle = onWeeklyRemindersToggle,
                onGoalCompletionNotificationsToggle = onGoalCompletionNotificationsToggle,
                notificationsTitle = notificationsTitle,
                notificationsDescription = notificationsDescription,
                weeklyRemindersTitle = weeklyRemindersTitle,
                weeklyRemindersDescription = weeklyRemindersDescription,
                goalCompletionTitle = goalCompletionTitle,
                goalCompletionDescription = goalCompletionDescription
            )
        ) { setting ->
            GeneralItem(
                title = setting.title,
                description = setting.description,
                leadingIcon = setting.icon,
                trailingContent = setting.trailingContent
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Appearance Section
        item {
            SettingsSectionHeader(title = appearanceSectionTitle)
        }

        items(
            getAppearanceSettings(
                preferences = preferences,
                currentLanguage = currentLanguage,
                onDarkModeToggle = onDarkModeToggle,
                onLanguageClick = onLanguageClick,
                darkModeTitle = darkModeTitle,
                darkModeDescription = darkModeDescription,
                languageTitle = languageTitle,
                currentLanguageLabel = currentLanguageLabel
            )
        ) { setting ->
            GeneralItem(
                title = setting.title,
                description = setting.description,
                leadingIcon = setting.icon,
                trailingContent = setting.trailingContent,
                onClick = setting.onClick
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // Privacy & Data Section
        item {
            SettingsSectionHeader(title = privacySectionTitle)
        }

        items(
            getPrivacyDataSettings(
                preferences = preferences,
                onAutoBackupToggle = onAutoBackupToggle,
                onAnalyticsToggle = onAnalyticsToggle,
                autoBackupTitle = autoBackupTitle,
                autoBackupDescription = autoBackupDescription,
                analyticsTitle = analyticsTitle,
                analyticsDescription = analyticsDescription
            )
        ) { setting ->
            GeneralItem(
                title = setting.title,
                description = setting.description,
                leadingIcon = setting.icon,
                trailingContent = setting.trailingContent
            )
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }

        // About Section
        item {
            SettingsSectionHeader(title = aboutSectionTitle)
        }

        items(
            getAboutSettings(
                onResetClick = onResetClick,
                appVersionTitle = appVersionTitle,
                resetSettingsTitle = resetSettingsTitle,
                resetSettingsDescription = resetSettingsDescription
            )
        ) { setting ->
            GeneralItem(
                title = setting.title,
                description = setting.description,
                leadingIcon = setting.icon,
                trailingContent = setting.trailingContent,
                onClick = setting.onClick
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun LanguageSelectionDialog(
    currentLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            Column {
                SupportedLanguages.forEach { language ->
                    GeneralItem(
                        title = language.displayName,
                        leadingIcon = language.flagResId,
                        trailingContent = if (language.code == currentLanguageCode) {
                            TrailingContent.Text("✓")
                        } else {
                            TrailingContent.None
                        },
                        onClick = { onLanguageSelected(language.code) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

// Data classes for settings items
private data class SettingItem(
    val title: String,
    val description: String? = null,
    val icon: ImageVector,
    val trailingContent: TrailingContent = TrailingContent.None,
    val onClick: (() -> Unit)? = null
)

// Settings data functions
private fun getProfileSettings(
    onProfileClick: () -> Unit,
    userProfileTitle: String,
    userProfileDescription: String
): List<SettingItem> = listOf(
    SettingItem(
        title = userProfileTitle,
        description = userProfileDescription,
        icon = Icons.Default.Settings,
        trailingContent = TrailingContent.Arrow,
        onClick = onProfileClick
    )
)

private fun getAudioFeedbackSettings(
    preferences: UserPreferences,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit,
    soundEffectsTitle: String,
    soundEffectsDescription: String,
    vibrationTitle: String,
    vibrationDescription: String
): List<SettingItem> = listOf(
    SettingItem(
        title = soundEffectsTitle,
        description = soundEffectsDescription,
        icon = Icons.Default.MusicNote,
        trailingContent = TrailingContent.Switch(
            checked = preferences.soundEnabled,
            onCheckedChange = onSoundToggle
        )
    ),
    SettingItem(
        title = vibrationTitle,
        description = vibrationDescription,
        icon = Icons.Default.Notifications,
        trailingContent = TrailingContent.Switch(
            checked = preferences.vibrationEnabled,
            onCheckedChange = onVibrationToggle
        )
    )
)

private fun getNotificationSettings(
    preferences: UserPreferences,
    onNotificationsToggle: (Boolean) -> Unit,
    onWeeklyRemindersToggle: (Boolean) -> Unit,
    onGoalCompletionNotificationsToggle: (Boolean) -> Unit,
    notificationsTitle: String,
    notificationsDescription: String,
    weeklyRemindersTitle: String,
    weeklyRemindersDescription: String,
    goalCompletionTitle: String,
    goalCompletionDescription: String
): List<SettingItem> = listOf(
    SettingItem(
        title = notificationsTitle,
        description = notificationsDescription,
        icon = Icons.Default.Notifications,
        trailingContent = TrailingContent.Switch(
            checked = preferences.notificationsEnabled,
            onCheckedChange = onNotificationsToggle
        )
    ),
    SettingItem(
        title = weeklyRemindersTitle,
        description = weeklyRemindersDescription,
        icon = Icons.Default.Notifications,
        trailingContent = TrailingContent.Switch(
            checked = preferences.weeklyRemindersEnabled,
            onCheckedChange = onWeeklyRemindersToggle
        )
    ),
    SettingItem(
        title = goalCompletionTitle,
        description = goalCompletionDescription,
        icon = Icons.Default.Notifications,
        trailingContent = TrailingContent.Switch(
            checked = preferences.goalCompletionNotifications,
            onCheckedChange = onGoalCompletionNotificationsToggle
        )
    )
)

private fun getAppearanceSettings(
    preferences: UserPreferences,
    currentLanguage: String,
    onDarkModeToggle: (Boolean) -> Unit,
    onLanguageClick: () -> Unit,
    darkModeTitle: String,
    darkModeDescription: String,
    languageTitle: String,
    currentLanguageLabel: String
): List<SettingItem> = listOf(
    SettingItem(
        title = darkModeTitle,
        description = darkModeDescription,
        icon = Icons.Default.WbSunny,
        trailingContent = TrailingContent.Switch(
            checked = preferences.darkModeEnabled,
            onCheckedChange = onDarkModeToggle
        )
    ),
    SettingItem(
        title = languageTitle,
        description = currentLanguageLabel,
        icon = Icons.Default.Description,
        trailingContent = TrailingContent.Arrow,
        onClick = onLanguageClick
    )
)

private fun getPrivacyDataSettings(
    preferences: UserPreferences,
    onAutoBackupToggle: (Boolean) -> Unit,
    onAnalyticsToggle: (Boolean) -> Unit,
    autoBackupTitle: String,
    autoBackupDescription: String,
    analyticsTitle: String,
    analyticsDescription: String
): List<SettingItem> = listOf(
    SettingItem(
        title = autoBackupTitle,
        description = autoBackupDescription,
        icon = Icons.Default.Cloud,
        trailingContent = TrailingContent.Switch(
            checked = preferences.autoBackupEnabled,
            onCheckedChange = onAutoBackupToggle
        )
    ),
    SettingItem(
        title = analyticsTitle,
        description = analyticsDescription,
        icon = Icons.Default.Lock,
        trailingContent = TrailingContent.Switch(
            checked = preferences.analyticsEnabled,
            onCheckedChange = onAnalyticsToggle
        )
    )
)

private fun getAboutSettings(
    onResetClick: () -> Unit,
    appVersionTitle: String,
    resetSettingsTitle: String,
    resetSettingsDescription: String
): List<SettingItem> = listOf(
    SettingItem(
        title = appVersionTitle,
        description = "1.0.0",
        icon = Icons.Default.Info,
        trailingContent = TrailingContent.None
    ),
    SettingItem(
        title = resetSettingsTitle,
        description = resetSettingsDescription,
        icon = Icons.Default.Refresh,
        trailingContent = TrailingContent.Arrow,
        onClick = onResetClick
    )
)

@Preview
@Composable
fun SettingsScreenPreview() {
    MountainSunriseTheme {
        SettingsScreen(
            onNavigateBack = {},
            onLanguageChange = {}
        )
    }
}
