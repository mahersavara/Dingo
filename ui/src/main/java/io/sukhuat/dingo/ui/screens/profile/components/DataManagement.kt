package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.components.SelectableGeneralItem
import io.sukhuat.dingo.common.components.ToggleGeneralItem
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile

/**
 * Data Management component for handling data export, privacy controls, and sign out
 */
@Composable
fun DataManagement(
    userProfile: UserProfile,
    uiState: DataManagementUiState,
    actions: DataManagementActions,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success message when data is exported
    LaunchedEffect(uiState.dataExportState.exportSuccess) {
        if (uiState.dataExportState.exportSuccess) {
            snackbarHostState.showSnackbar("Data exported successfully")
        }
    }

    // Show error messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            actions.onDismissError()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Data Export Section
            DataExportSection(
                exportState = uiState.dataExportState,
                actions = actions
            )

            // Privacy Controls Section
            PrivacyControlsSection(
                privacyState = uiState.privacyControlsState,
                actions = actions
            )

            // Profile Visibility Section
            ProfileVisibilitySection(
                privacyState = uiState.privacyControlsState,
                actions = actions
            )

            // Data Sharing Preferences Section
            DataSharingSection(
                privacyState = uiState.privacyControlsState,
                actions = actions
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Sign Out Section
            SignOutSection(
                userProfile = userProfile,
                signOutState = uiState.signOutState,
                actions = actions
            )
        }

        // Data Export Dialog
        if (uiState.dataExportState.showExportDialog) {
            DataExportDialog(
                exportState = uiState.dataExportState,
                actions = actions
            )
        }

        // Sign Out Dialog
        if (uiState.signOutState.showSignOutDialog) {
            SignOutDialog(
                signOutState = uiState.signOutState,
                actions = actions
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun DataExportSection(
    exportState: DataExportState,
    actions: DataManagementActions
) {
    Column {
        Text(
            text = "Data Export",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Your Data",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Download a copy of all your data for backup or transfer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Export includes information
                Text(
                    text = "Export includes:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                val exportItems = listOf(
                    "• Profile information and settings",
                    "• All goals and progress data",
                    "• Achievement history",
                    "• User preferences",
                    "• Account activity logs"
                )

                exportItems.forEach { item ->
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = actions.onShowDataExport,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !exportState.isExporting
                ) {
                    if (exportState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (exportState.isExporting) "Exporting..." else "Export Data")
                }

                // Show exported file info if available
                exportState.exportedFilePath?.let { filePath ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "File saved to Downloads",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { actions.onOpenExportedFile(filePath) }
                        ) {
                            Text("Open")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyControlsSection(
    privacyState: PrivacyControlsState,
    actions: DataManagementActions
) {
    Column {
        Text(
            text = "Privacy Controls",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ToggleGeneralItem(
            title = "Data Collection",
            description = "Allow app to collect usage data for improvements",
            leadingIcon = Icons.Default.Analytics,
            checked = privacyState.allowDataCollection,
            onCheckedChange = actions.onUpdateDataCollection,
            enabled = !privacyState.isUpdatingPreferences
        )

        ToggleGeneralItem(
            title = "Analytics",
            description = "Help improve the app with anonymous usage analytics",
            leadingIcon = Icons.Default.Analytics,
            checked = privacyState.allowAnalytics,
            onCheckedChange = actions.onUpdateAnalytics,
            enabled = !privacyState.isUpdatingPreferences
        )

        ToggleGeneralItem(
            title = "Personalization",
            description = "Use your data to personalize your experience",
            leadingIcon = Icons.Default.Person,
            checked = privacyState.allowPersonalization,
            onCheckedChange = actions.onUpdatePersonalization,
            enabled = !privacyState.isUpdatingPreferences
        )

        ToggleGeneralItem(
            title = "Notifications",
            description = "Receive notifications about your goals and achievements",
            leadingIcon = Icons.Default.Notifications,
            checked = privacyState.allowNotifications,
            onCheckedChange = actions.onUpdateNotifications,
            enabled = !privacyState.isUpdatingPreferences
        )
    }
}

@Composable
private fun ProfileVisibilitySection(
    privacyState: PrivacyControlsState,
    actions: DataManagementActions
) {
    Column {
        Text(
            text = "Profile Visibility",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ProfileVisibility.values().forEach { visibility ->
            SelectableGeneralItem(
                title = visibility.displayName,
                description = visibility.description,
                leadingIcon = Icons.Default.Visibility,
                selected = privacyState.profileVisibility == visibility,
                onClick = { actions.onUpdateProfileVisibility(visibility) },
                enabled = !privacyState.isUpdatingPreferences
            )
        }
    }
}

@Composable
private fun DataSharingSection(
    privacyState: PrivacyControlsState,
    actions: DataManagementActions
) {
    Column {
        Text(
            text = "Data Sharing",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ToggleGeneralItem(
            title = "Share Achievements",
            description = "Allow sharing of your achievements on social media",
            leadingIcon = Icons.Default.Share,
            checked = privacyState.shareAchievements,
            onCheckedChange = actions.onUpdateShareAchievements,
            enabled = !privacyState.isUpdatingPreferences
        )

        ToggleGeneralItem(
            title = "Share Progress",
            description = "Allow sharing of your goal progress with friends",
            leadingIcon = Icons.Default.Share,
            checked = privacyState.shareProgress,
            onCheckedChange = actions.onUpdateShareProgress,
            enabled = !privacyState.isUpdatingPreferences
        )
    }
}

@Composable
private fun SignOutSection(
    userProfile: UserProfile,
    signOutState: SignOutState,
    actions: DataManagementActions
) {
    Column {
        Text(
            text = "Session Management",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Sign out of your account on this device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current session info
                Text(
                    text = "Current session:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Signed in as ${userProfile.email}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Account type: ${when (userProfile.authProvider) {
                        AuthProvider.EMAIL_PASSWORD -> "Email & Password"
                        AuthProvider.GOOGLE -> "Google Account"
                        AuthProvider.MULTIPLE -> "Multiple (Google + Email)"
                        AuthProvider.ANONYMOUS -> "Anonymous"
                    }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = actions.onShowSignOut,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !signOutState.isSigningOut
                ) {
                    if (signOutState.isSigningOut) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (signOutState.isSigningOut) "Signing Out..." else "Sign Out")
                }
            }
        }
    }
}

@Composable
private fun DataExportDialog(
    exportState: DataExportState,
    actions: DataManagementActions
) {
    AlertDialog(
        onDismissRequest = actions.onHideDataExport,
        icon = {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Export Your Data",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "This will create a downloadable file containing all your personal data stored in the app.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Export progress
                if (exportState.isExporting) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Preparing your data...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LinearProgressIndicator(
                            progress = exportState.exportProgress,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "${(exportState.exportProgress * 100).toInt()}% complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    // Export information
                    Text(
                        text = "The exported file will include:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    val exportItems = listOf(
                        "• Profile information and settings",
                        "• All goals and progress data",
                        "• Achievement history and statistics",
                        "• User preferences and customizations",
                        "• Account activity and login history"
                    )

                    exportItems.forEach { item ->
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // File format info
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = "File Format: JSON",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "The data will be exported in a human-readable JSON format that can be imported into other applications.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Show error if any
                exportState.exportError?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = actions.onExportData,
                enabled = !exportState.isExporting
            ) {
                if (exportState.isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (exportState.isExporting) "Exporting..." else "Export Data")
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions.onHideDataExport,
                enabled = !exportState.isExporting
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SignOutDialog(
    signOutState: SignOutState,
    actions: DataManagementActions
) {
    AlertDialog(
        onDismissRequest = actions.onHideSignOut,
        icon = {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Sign Out",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Are you sure you want to sign out?",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "You will need to sign in again to access your account and data.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Show error if any
                signOutState.signOutError?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = actions.onSignOut,
                enabled = !signOutState.isSigningOut
            ) {
                if (signOutState.isSigningOut) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (signOutState.isSigningOut) "Signing Out..." else "Sign Out")
            }
        },
        dismissButton = {
            TextButton(
                onClick = actions.onHideSignOut,
                enabled = !signOutState.isSigningOut
            ) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DataManagementPreview() {
    MountainSunriseTheme {
        val sampleProfile = UserProfile(
            userId = "123",
            displayName = "John Doe",
            email = "john.doe@example.com",
            profileImageUrl = null,
            joinDate = System.currentTimeMillis() - (6 * 30 * 24 * 60 * 60 * 1000L),
            isEmailVerified = true,
            authProvider = AuthProvider.EMAIL_PASSWORD,
            lastLoginDate = System.currentTimeMillis() - (2 * 60 * 60 * 1000L)
        )

        val sampleUiState = DataManagementUiState(
            privacyControlsState = PrivacyControlsState(
                allowDataCollection = true,
                allowAnalytics = false,
                allowPersonalization = true,
                allowNotifications = true,
                profileVisibility = ProfileVisibility.PRIVATE,
                shareAchievements = false,
                shareProgress = true
            )
        )

        val sampleActions = DataManagementActions(
            onShowDataExport = {},
            onHideDataExport = {},
            onExportData = {},
            onUpdateDataCollection = {},
            onUpdateAnalytics = {},
            onUpdatePersonalization = {},
            onUpdateNotifications = {},
            onUpdateProfileVisibility = {},
            onUpdateShareAchievements = {},
            onUpdateShareProgress = {},
            onShowSignOut = {},
            onHideSignOut = {},
            onSignOut = {},
            onDismissError = {},
            onOpenExportedFile = {}
        )

        DataManagement(
            userProfile = sampleProfile,
            uiState = sampleUiState,
            actions = sampleActions
        )
    }
}
