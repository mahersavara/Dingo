package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.theme.Success
import io.sukhuat.dingo.domain.model.ReferralData
import io.sukhuat.dingo.domain.model.SharingPrivacySettings
import io.sukhuat.dingo.domain.model.SharingStats

/**
 * Main sharing components section for profile
 */
@Composable
fun SharingComponents(
    onShareAchievement: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SharingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Sharing Section
        ProfileSharingSection(
            onShareProfile = { viewModel.shareProfile() },
            isLoading = uiState.isGeneratingContent,
            privacySettings = uiState.privacySettings
        )

        // Referral System Section
        uiState.referralData?.let { referralData ->
            ReferralSection(
                referralData = referralData,
                onShareReferral = { message -> viewModel.shareReferral(message) },
                onGenerateNewCode = { viewModel.generateReferralCode() },
                isLoading = uiState.isGeneratingContent
            )
        }

        // Sharing Statistics
        uiState.sharingStats?.let { sharingStats ->
            SharingStatsSection(
                stats = sharingStats
            )
        }

        // Privacy Controls
        SharingPrivacySection(
            privacySettings = uiState.privacySettings,
            onUpdateSettings = { settings -> viewModel.updatePrivacySettings(settings) }
        )
    }

    // Sharing Dialog
    if (dialogState.isVisible) {
        SharingDialog(
            dialogState = dialogState,
            onSelectPlatform = { platform -> viewModel.selectPlatform(platform) },
            onConfirmShare = { viewModel.confirmShare() },
            onDismiss = { viewModel.dismissDialog() }
        )
    }
}

/**
 * Profile sharing section
 */
@Composable
private fun ProfileSharingSection(
    onShareProfile: () -> Unit,
    isLoading: Boolean,
    privacySettings: SharingPrivacySettings
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share Your Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = RusticGold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Let others see your goal-setting journey and achievements",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onShareProfile,
                enabled = privacySettings.allowProfileSharing && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Share My Profile")
            }

            if (!privacySettings.allowProfileSharing) {
                Text(
                    text = "Profile sharing is disabled in privacy settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Referral system section
 */
@Composable
private fun ReferralSection(
    referralData: ReferralData,
    onShareReferral: (String?) -> Unit,
    onGenerateNewCode: () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Invite Friends",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = Success
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Referral Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ReferralStatItem(
                    label = "Invited",
                    value = referralData.totalInvites.toString(),
                    color = MaterialTheme.colorScheme.primary
                )

                ReferralStatItem(
                    label = "Joined",
                    value = referralData.successfulInvites.toString(),
                    color = Success
                )

                ReferralStatItem(
                    label = "Pending",
                    value = referralData.pendingInvites.toString(),
                    color = RusticGold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Referral Code
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Your Referral Code",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = referralData.referralCode,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(onClick = { /* Copy to clipboard */ }) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy code",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        IconButton(onClick = onGenerateNewCode) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Generate new code",
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Share Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onShareReferral(null) },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Invite Friends")
                    }
                }

                OutlinedButton(
                    onClick = { /* Copy link */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Link")
                }
            }
        }
    }
}

/**
 * Individual referral stat item
 */
@Composable
private fun ReferralStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Sharing statistics section
 */
@Composable
private fun SharingStatsSection(
    stats: SharingStats
) {
    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDetails = !showDetails },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Sharing Activity",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${stats.totalShares} total shares",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }

                    Icon(
                        imageVector = if (showDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showDetails) "Hide details" else "Show details",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            AnimatedVisibility(
                visible = showDetails,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    if (stats.mostSharedAchievement != null) {
                        Text(
                            text = "Most shared: ${stats.mostSharedAchievement}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RusticGold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        text = "Platform breakdown:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    stats.platformBreakdown.forEach { (platform, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = platform.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = count.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Privacy controls section
 */
@Composable
private fun SharingPrivacySection(
    privacySettings: SharingPrivacySettings,
    onUpdateSettings: (SharingPrivacySettings) -> Unit
) {
    var showPrivacyControls by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPrivacyControls = !showPrivacyControls },
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Privacy Controls",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = if (showPrivacyControls) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (showPrivacyControls) "Hide controls" else "Show controls",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            AnimatedVisibility(
                visible = showPrivacyControls,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PrivacyToggleItem(
                        title = "Achievement Sharing",
                        description = "Allow sharing of unlocked achievements",
                        checked = privacySettings.allowAchievementSharing,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(privacySettings.copy(allowAchievementSharing = enabled))
                        }
                    )

                    PrivacyToggleItem(
                        title = "Profile Sharing",
                        description = "Allow sharing of profile and statistics",
                        checked = privacySettings.allowProfileSharing,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(privacySettings.copy(allowProfileSharing = enabled))
                        }
                    )

                    PrivacyToggleItem(
                        title = "Referral Invitations",
                        description = "Allow sending referral invitations",
                        checked = privacySettings.allowReferralSharing,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(privacySettings.copy(allowReferralSharing = enabled))
                        }
                    )

                    PrivacyToggleItem(
                        title = "App Promotion",
                        description = "Include app promotion in shared content",
                        checked = privacySettings.includeAppPromotion,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(privacySettings.copy(includeAppPromotion = enabled))
                        }
                    )

                    PrivacyToggleItem(
                        title = "Use Real Name",
                        description = "Share with your real name instead of username",
                        checked = privacySettings.shareWithRealName,
                        onCheckedChange = { enabled ->
                            onUpdateSettings(privacySettings.copy(shareWithRealName = enabled))
                        }
                    )
                }
            }
        }
    }
}

/**
 * Individual privacy toggle item
 */
@Composable
private fun PrivacyToggleItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SharingComponentsPreview() {
    MountainSunriseTheme {
        SharingComponents(
            onShareAchievement = {}
        )
    }
}
