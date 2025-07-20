package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.sukhuat.dingo.common.localization.AppLanguage
import io.sukhuat.dingo.common.localization.SupportedLanguages
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.UserPreferences

/**
 * Quick actions component providing easy access to common settings and preferences
 * including theme toggle, language selection, and notification preferences
 */
@Composable
fun ProfileQuickActions(
    preferences: UserPreferences,
    currentLanguage: AppLanguage,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Title
            Text(
                text = "Quick Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Theme Toggle
            QuickActionItem(
                icon = if (preferences.darkModeEnabled) Icons.Default.DarkMode else Icons.Default.LightMode,
                title = "Dark Mode",
                subtitle = if (preferences.darkModeEnabled) "Dark theme enabled" else "Light theme enabled",
                trailing = {
                    AnimatedSwitch(
                        checked = preferences.darkModeEnabled,
                        onCheckedChange = onToggleDarkMode,
                        contentDescription = "Dark mode: ${if (preferences.darkModeEnabled) "enabled" else "disabled"}"
                    )
                }
            )

            // Language Selection
            LanguageSelector(
                currentLanguage = currentLanguage,
                onLanguageChange = onLanguageChange
            )

            // Notification Settings
            QuickActionItem(
                icon = if (preferences.notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                title = "Notifications",
                subtitle = if (preferences.notificationsEnabled) "Notifications enabled" else "Notifications disabled",
                trailing = {
                    AnimatedSwitch(
                        checked = preferences.notificationsEnabled,
                        onCheckedChange = onToggleNotifications,
                        contentDescription = "Notifications: ${if (preferences.notificationsEnabled) "enabled" else "disabled"}"
                    )
                }
            )

            // Sound Settings
            QuickActionItem(
                icon = if (preferences.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                title = "Sound Effects",
                subtitle = if (preferences.soundEnabled) "Sound enabled" else "Sound disabled",
                trailing = {
                    AnimatedSwitch(
                        checked = preferences.soundEnabled,
                        onCheckedChange = onToggleSound,
                        contentDescription = "Sound: ${if (preferences.soundEnabled) "enabled" else "disabled"}"
                    )
                }
            )

            // Vibration Settings
            QuickActionItem(
                icon = Icons.Default.Vibration,
                title = "Vibration",
                subtitle = if (preferences.vibrationEnabled) "Vibration enabled" else "Vibration disabled",
                trailing = {
                    AnimatedSwitch(
                        checked = preferences.vibrationEnabled,
                        onCheckedChange = onToggleVibration,
                        contentDescription = "Vibration: ${if (preferences.vibrationEnabled) "enabled" else "disabled"}"
                    )
                }
            )

            // Settings Navigation
            QuickActionItem(
                icon = Icons.Default.Settings,
                title = "All Settings",
                subtitle = "Access complete settings menu",
                onClick = onNavigateToSettings,
                showArrow = true
            )
        }
    }
}

/**
 * Individual quick action item
 */
@Composable
private fun QuickActionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    showArrow: Boolean = false
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Row(
        modifier = clickableModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = RusticGold.copy(alpha = 0.1f)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = RusticGold,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Trailing content
        if (trailing != null) {
            trailing()
        } else if (showArrow) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_more),
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Language selector with dropdown
 */
@Composable
private fun LanguageSelector(
    currentLanguage: AppLanguage,
    onLanguageChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        QuickActionItem(
            icon = Icons.Default.Language,
            title = "Language",
            subtitle = currentLanguage.displayName,
            onClick = { expanded = true },
            trailing = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Current language flag
                    AsyncImage(
                        model = currentLanguage.flagResId,
                        contentDescription = currentLanguage.displayName,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Icon(
                        painter = painterResource(id = android.R.drawable.arrow_down_float),
                        contentDescription = "Expand",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        )

        // Language dropdown
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(200.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SupportedLanguages.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = language.flagResId,
                                contentDescription = language.displayName,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            Text(
                                text = language.displayName,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            if (language.code == currentLanguage.code) {
                                Spacer(modifier = Modifier.weight(1f))
                                Surface(
                                    modifier = Modifier.size(8.dp),
                                    shape = CircleShape,
                                    color = RusticGold
                                ) {}
                            }
                        }
                    },
                    onClick = {
                        onLanguageChange(language.code)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Animated switch with smooth transitions
 */
@Composable
private fun AnimatedSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = ""
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.05f else 1f,
        animationSpec = tween(150),
        label = "SwitchScale"
    )

    val thumbColor by animateColorAsState(
        targetValue = if (checked) RusticGold else MaterialTheme.colorScheme.outline,
        animationSpec = tween(150),
        label = "SwitchThumbColor"
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) RusticGold.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
        animationSpec = tween(150),
        label = "SwitchTrackColor"
    )

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
            .scale(scale)
            .semantics {
                this.contentDescription = contentDescription
            },
        colors = SwitchDefaults.colors(
            checkedThumbColor = thumbColor,
            uncheckedThumbColor = thumbColor,
            checkedTrackColor = trackColor,
            uncheckedTrackColor = trackColor,
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileQuickActionsPreview() {
    MountainSunriseTheme {
        ProfileQuickActions(
            preferences = UserPreferences(
                soundEnabled = true,
                vibrationEnabled = true,
                notificationsEnabled = true,
                darkModeEnabled = false,
                languageCode = "en"
            ),
            currentLanguage = SupportedLanguages[0],
            onToggleDarkMode = {},
            onToggleNotifications = {},
            onToggleSound = {},
            onToggleVibration = {},
            onLanguageChange = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileQuickActionsDarkPreview() {
    MountainSunriseTheme {
        ProfileQuickActions(
            preferences = UserPreferences(
                soundEnabled = false,
                vibrationEnabled = false,
                notificationsEnabled = false,
                darkModeEnabled = true,
                languageCode = "vi"
            ),
            currentLanguage = SupportedLanguages[1],
            onToggleDarkMode = {},
            onToggleNotifications = {},
            onToggleSound = {},
            onToggleVibration = {},
            onLanguageChange = {},
            onNavigateToSettings = {}
        )
    }
}
