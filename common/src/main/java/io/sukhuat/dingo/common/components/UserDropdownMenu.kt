package io.sukhuat.dingo.common.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.localization.AppLanguage
import io.sukhuat.dingo.common.localization.SupportedLanguages
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold

/**
 * A dropdown menu item with an icon and text
 */
data class DropdownMenuItemData(
    val icon: ImageVector,
    val text: String,
    val contentDescription: String,
    val onClick: () -> Unit
)

/**
 * A dropdown menu that appears when clicking on the user profile icon or settings icon
 * @param isAuthenticated Whether the user is authenticated
 * @param userProfileImageUrl URL of the user's profile image (null for default icon)
 * @param currentLanguage The currently selected language
 * @param onProfileClick Called when the profile option is clicked
 * @param onLanguageChange Called when a language is selected
 * @param onSettingsClick Called when the settings option is clicked
 * @param onLogoutClick Called when the logout option is clicked
 */
@Composable
fun UserDropdownMenu(
    isAuthenticated: Boolean,
    userProfileImageUrl: String? = null,
    currentLanguage: AppLanguage,
    onProfileClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Get screen size for responsive layout
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Adjust dropdown width based on screen size
    val dropdownWidth = when {
        screenWidth < 360 -> 180.dp
        screenWidth < 600 -> 220.dp
        else -> 260.dp
    }

    // Scale animation for the icon when clicked
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.1f else 1.0f,
        label = "IconScale"
    )

    val buttonContentDescription = if (isAuthenticated) {
        stringResource(R.string.profile)
    } else {
        stringResource(R.string.settings)
    }

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopEnd)
    ) {
        // Icon button to open dropdown
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier
                .scale(scale)
                .semantics {
                    contentDescription = buttonContentDescription
                }
        ) {
            if (isAuthenticated) {
                // Show profile avatar for authenticated users
                UserProfileIcon(imageUrl = userProfileImageUrl)
            } else {
                // Show settings icon for non-authenticated users
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = RusticGold,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(dropdownWidth)
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Profile option (only for authenticated users)
            if (isAuthenticated) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.profile)) },
                    onClick = {
                        expanded = false
                        onProfileClick()
                    },
                    leadingIcon = {
                        CircleIconButton(
                            icon = Icons.Default.Person,
                            contentDescription = stringResource(R.string.profile)
                        )
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
            }

            // Language option
            val languageText = stringResource(R.string.language)
            val flagIcon = ImageVector.vectorResource(currentLanguage.flagResId)
            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(languageText)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = flagIcon,
                            contentDescription = currentLanguage.displayName,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                onClick = {
                    expanded = false
                    showLanguageDialog = true
                },
                leadingIcon = {
                    CircleIconButton(
                        icon = flagIcon,
                        contentDescription = languageText
                    )
                }
            )

            // Settings option
            DropdownMenuItem(
                text = { Text(stringResource(R.string.settings)) },
                onClick = {
                    expanded = false
                    onSettingsClick()
                },
                leadingIcon = {
                    CircleIconButton(
                        icon = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings)
                    )
                }
            )

            // Logout option (only for authenticated users)
            if (isAuthenticated) {
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.logout)) },
                    onClick = {
                        expanded = false
                        onLogoutClick()
                    },
                    leadingIcon = {
                        CircleIconButton(
                            icon = Icons.Default.ExitToApp,
                            contentDescription = stringResource(R.string.logout),
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                            iconTint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }

    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = onLanguageChange,
            onDismiss = { showLanguageDialog = false }
        )
    }
}

/**
 * A circular icon button used in the dropdown menu
 */
@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconTint: Color = RusticGold
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Dialog for selecting a language
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_language)) },
        text = {
            Column {
                SupportedLanguages.forEach { language ->
                    val flagIcon = ImageVector.vectorResource(language.flagResId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageSelected(language.code)
                                onDismiss()
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = flagIcon,
                            contentDescription = language.displayName,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(language.displayName)
                        Spacer(modifier = Modifier.weight(1f))
                        if (language.code == currentLanguage.code) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = RusticGold
                            )
                        }
                    }
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

@Preview
@Composable
fun UserDropdownMenuPreviewAuthenticated() {
    MountainSunriseTheme {
        Surface {
            UserDropdownMenu(
                isAuthenticated = true,
                currentLanguage = SupportedLanguages[0],
                onProfileClick = {},
                onLanguageChange = {},
                onSettingsClick = {},
                onLogoutClick = {}
            )
        }
    }
}

@Preview
@Composable
fun UserDropdownMenuPreviewUnauthenticated() {
    MountainSunriseTheme {
        Surface {
            UserDropdownMenu(
                isAuthenticated = false,
                currentLanguage = SupportedLanguages[0],
                onProfileClick = {},
                onLanguageChange = {},
                onSettingsClick = {},
                onLogoutClick = {}
            )
        }
    }
}
