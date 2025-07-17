package io.sukhuat.dingo.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import io.sukhuat.dingo.common.icons.MedievalIcons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.components.GeneralItem
import io.sukhuat.dingo.common.components.NavigableGeneralItem
import io.sukhuat.dingo.common.components.ToggleGeneralItem
import io.sukhuat.dingo.common.components.TrailingContent
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.usecase.preferences.NotificationPermissionStatus

/**
 * Example of how to integrate settings into your existing screens
 * This shows different ways to use the GeneralItem component and settings functionality
 */
@Composable
fun SettingsIntegrationExample(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val notificationPermissionStatus by viewModel.notificationPermissionStatus.collectAsState()
    val context = LocalContext.current

    var showPermissionDialog by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Settings Integration Examples",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (uiState) {
                is SettingsUiState.Success -> {
                    val preferences = (uiState as SettingsUiState.Success).preferences

                    // Example 1: Simple toggle with GeneralItem
                    ToggleGeneralItem(
                        title = "Sound Effects",
                        description = "Play sounds for interactions and achievements",
                        leadingIcon = MedievalIcons.Lute,
                        checked = preferences.soundEnabled,
                        onCheckedChange = viewModel::toggleSound
                    )

                    // Example 2: Notification toggle with permission handling
                    GeneralItem(
                        title = "Notifications",
                        description = when (notificationPermissionStatus) {
                            NotificationPermissionStatus.GRANTED -> "Notifications are enabled"
                            NotificationPermissionStatus.PERMISSION_DENIED -> "Permission required"
                            NotificationPermissionStatus.NOTIFICATIONS_DISABLED -> "Disabled in system settings"
                        },
                        leadingIcon = Icons.Default.Notifications,
                        trailingContent = TrailingContent.Switch(
                            checked = preferences.notificationsEnabled && notificationPermissionStatus == NotificationPermissionStatus.GRANTED,
                            onCheckedChange = { enabled ->
                                if (enabled && notificationPermissionStatus != NotificationPermissionStatus.GRANTED) {
                                    showPermissionDialog = true
                                } else {
                                    viewModel.toggleNotifications(enabled)
                                }
                            }
                        )
                    )

                    // Example 3: Navigable item to full settings screen
                    NavigableGeneralItem(
                        title = "All Settings",
                        description = "View and manage all app settings",
                        leadingIcon = Icons.Default.Settings,
                        onClick = {
                            // Navigate to full settings screen
                            // In your actual implementation, you would use your navigation system
                        }
                    )

                    // Example 4: Custom trailing content with badge
                    GeneralItem(
                        title = "Weekly Reminders",
                        description = "Get reminded about your weekly goals",
                        leadingIcon = Icons.Default.Notifications,
                        trailingContent = if (preferences.weeklyRemindersEnabled) {
                            TrailingContent.Badge(
                                text = "ON",
                                backgroundColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            TrailingContent.Text("OFF")
                        },
                        onClick = {
                            viewModel.toggleWeeklyReminders(!preferences.weeklyRemindersEnabled)
                        }
                    )
                }

                is SettingsUiState.Loading -> {
                    Text("Loading settings...")
                }

                is SettingsUiState.Error -> {
                    Text(
                        text = "Error: ${(uiState as SettingsUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Permission dialog
    if (showPermissionDialog) {
        NotificationPermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onOpenSettings = {
                context.startActivity(viewModel.getNotificationSettingsIntent())
                showPermissionDialog = false
            }
        )
    }
}

@Composable
private fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Permission Required") },
        text = {
            Text("To receive notifications, please enable them in your device settings. This will allow you to get reminders and goal completion notifications.")
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
fun SettingsIntegrationExamplePreview() {
    MountainSunriseTheme {
        // Note: This preview won't work with Hilt ViewModel
        // In a real app, you'd provide mock data or use a preview parameter
        Text("Settings Integration Example")
    }
}
