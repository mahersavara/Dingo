package io.sukhuat.dingo.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import io.sukhuat.dingo.common.icons.MedievalIcons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.components.GeneralItem
import io.sukhuat.dingo.common.components.LoadingIndicator
import io.sukhuat.dingo.common.components.TrailingContent
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
                title = { Text("Settings") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = onNavigateBack) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
                        text = "Error loading settings",
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
            title = { Text("Reset Settings") },
            text = { Text("Are you sure you want to reset all settings to their default values? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefaults()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
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
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize()
    ) {
        // Audio & Feedback Section
        item {
            SettingsSectionHeader(title = "Audio & Feedback")
        }

        items(getAudioFeedbackSettings(preferences, onSoundToggle, onVibrationToggle)) { setting ->
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
            SettingsSectionHeader(title = "Notifications")
        }

        items(getNotificationSettings(preferences, onNotificationsToggle, onWeeklyRemindersToggle, onGoalCompletionNotificationsToggle)) { setting ->
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
            SettingsSectionHeader(title = "Appearance")
        }

        items(getAppearanceSettings(preferences, currentLanguage, onDarkModeToggle, onLanguageClick)) { setting ->
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
            SettingsSectionHeader(title = "Privacy & Data")
        }

        items(getPrivacyDataSettings(preferences, onAutoBackupToggle, onAnalyticsToggle)) { setting ->
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
            SettingsSectionHeader(title = "About")
        }

        items(getAboutSettings(onResetClick)) { setting ->
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
        title = { Text("Select Language") },
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
                Text("Cancel")
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
private fun getAudioFeedbackSettings(
    preferences: UserPreferences,
    onSoundToggle: (Boolean) -> Unit,
    onVibrationToggle: (Boolean) -> Unit
): List<SettingItem> = listOf(
    SettingItem(
        title = "Sound Effects",
        description = "Play sounds for interactions and achievements",
        icon = MedievalIcons.Lute,
        trailingContent = TrailingContent.Switch(
            checked = preferences.soundEnabled,
            onCheckedChange = onSoundToggle
        )
    ),
    SettingItem(
        title = "Vibration",
        description = "Haptic feedback for goal completions",
        icon = MedievalIcons.Bell,
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
    onGoalCompletionNotificationsToggle: (Boolean) -> Unit
): List<SettingItem> = listOf(
    SettingItem(
        title = "Notifications",
        description = "Enable all app notifications",
        icon = Icons.Default.Notifications,
        trailingContent = TrailingContent.Switch(
            checked = preferences.notificationsEnabled,
            onCheckedChange = onNotificationsToggle
        )
    ),
    SettingItem(
        title = "Weekly Reminders",
        description = "Get reminded about your weekly goals",
        icon = MedievalIcons.Bell,
        trailingContent = TrailingContent.Switch(
            checked = preferences.weeklyRemindersEnabled,
            onCheckedChange = onWeeklyRemindersToggle
        )
    ),
    SettingItem(
        title = "Goal Completion",
        description = "Notifications when you complete goals",
        icon = MedievalIcons.Bell,
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
    onLanguageClick: () -> Unit
): List<SettingItem> = listOf(
    SettingItem(
        title = "Dark Mode",
        description = "Use dark theme throughout the app",
        icon = MedievalIcons.Sun,
        trailingContent = TrailingContent.Switch(
            checked = preferences.darkModeEnabled,
            onCheckedChange = onDarkModeToggle
        )
    ),
    SettingItem(
        title = "Language",
        description = "Current: $currentLanguage",
        icon = MedievalIcons.Scroll,
        trailingContent = TrailingContent.Arrow,
        onClick = onLanguageClick
    )
)

private fun getPrivacyDataSettings(
    preferences: UserPreferences,
    onAutoBackupToggle: (Boolean) -> Unit,
    onAnalyticsToggle: (Boolean) -> Unit
): List<SettingItem> = listOf(
    SettingItem(
        title = "Auto Backup",
        description = "Automatically backup your goals to cloud",
        icon = MedievalIcons.CloudWind,
        trailingContent = TrailingContent.Switch(
            checked = preferences.autoBackupEnabled,
            onCheckedChange = onAutoBackupToggle
        )
    ),
    SettingItem(
        title = "Analytics",
        description = "Help improve the app by sharing usage data",
        icon = Icons.Default.Lock,
        trailingContent = TrailingContent.Switch(
            checked = preferences.analyticsEnabled,
            onCheckedChange = onAnalyticsToggle
        )
    )
)

private fun getAboutSettings(
    onResetClick: () -> Unit
): List<SettingItem> = listOf(
    SettingItem(
        title = "App Version",
        description = "1.0.0",
        icon = Icons.Default.Info,
        trailingContent = TrailingContent.None
    ),
    SettingItem(
        title = "Reset Settings",
        description = "Reset all settings to default values",
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
