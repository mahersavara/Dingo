package io.sukhuat.dingo.common.components

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.localization.AppLanguage
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.localization.SupportedLanguages
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.UserProfile

/**
 * User dropdown menu with profile, language, settings, and logout options
 * Enhanced to support Google Sign-In users with image priority logic
 */
@Composable
fun UserDropdownMenu(
    isAuthenticated: Boolean = false,
    userProfile: UserProfile? = null,
    currentLanguage: AppLanguage = LocalAppLanguage.current,
    onProfileClick: () -> Unit = {},
    onYearPlannerClick: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showLanguageSelector by remember { mutableStateOf(false) }
    var languageOptionPosition by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    // Debug logging
    Log.d("UserDropdownMenu", "isAuthenticated: $isAuthenticated")
    Log.d("UserDropdownMenu", "userProfile: $userProfile")
    Log.d("UserDropdownMenu", "userProfile != null: ${userProfile != null}")
    Log.d("UserDropdownMenu", "condition result: ${isAuthenticated && userProfile != null}")

    // Scale animation for the icon when clicked
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.1f else 1.0f,
        label = "IconScale"
    )

    Box {
        // User icon button with enhanced profile icon
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.scale(scale)
        ) {
            if (isAuthenticated && userProfile != null) {
                // Use the UserProfileIcon component for proper priority logic
                UserProfileIcon(
                    userProfile = userProfile,
                    size = 32.dp,
                    borderWidth = 1.dp,
                    borderColor = RusticGold
                )
            } else {
                // Default user icon for guests or when profile is not available
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Main dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
                showLanguageSelector = false
            },
            modifier = Modifier
                .width(200.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // User info display if authenticated
            if (isAuthenticated && userProfile != null) {
                // User info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    UserProfileIcon(
                        userProfile = userProfile,
                        size = 48.dp,
                        borderWidth = 2.dp,
                        borderColor = RusticGold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = userProfile.displayName.ifEmpty { "User" },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = userProfile.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Divider()
            }

            // Profile option
            if (isAuthenticated) {
                DropdownMenuItem(
                    text = { Text("Profile") },
                    onClick = {
                        expanded = false
                        onProfileClick()
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null
                        )
                    }
                )
            }

            // Year Planner option
            if (isAuthenticated) {
                DropdownMenuItem(
                    text = { Text("Year Planner") },
                    onClick = {
                        expanded = false
                        onYearPlannerClick()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_calendar_year),
                            contentDescription = null
                        )
                    }
                )
            }

            // Language option with position tracking
            DropdownMenuItem(
                text = { Text("Language") },
                onClick = {
                    showLanguageSelector = !showLanguageSelector
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_language),
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    languageOptionPosition = coordinates.positionInWindow()
                }
            )

            // Settings option
            DropdownMenuItem(
                text = { Text("Settings") },
                onClick = {
                    expanded = false
                    onSettingsClick()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null
                    )
                }
            )

            // Logout option
            if (isAuthenticated) {
                Divider()

                DropdownMenuItem(
                    text = { Text("Logout") },
                    onClick = {
                        expanded = false
                        onLogoutClick()
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = null
                        )
                    }
                )
            }
        }

        // Language selector submenu positioned to the left
        DropdownMenu(
            expanded = showLanguageSelector,
            onDismissRequest = { showLanguageSelector = false },
            modifier = Modifier
                .width(180.dp)
                .background(MaterialTheme.colorScheme.surface),
            offset = androidx.compose.ui.unit.DpOffset(
                x = (-200).dp, // Position to the left of main dropdown
                y = if (isAuthenticated) 220.dp else 48.dp // Adjust for new user info section
            )
        ) {
            Text(
                text = "Select Language",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Divider()

            for (language in SupportedLanguages) {
                LanguageItem(
                    language = language,
                    isSelected = language.code == currentLanguage.code,
                    onClick = {
                        onLanguageChange(language.code)
                        showLanguageSelector = false
                    }
                )
            }
        }
    }
}

/**
 * Backward compatibility overload that accepts userProfileImageUrl string
 * This maintains compatibility with existing code while migration is in progress
 */
@Composable
fun UserDropdownMenu(
    isAuthenticated: Boolean = false,
    userProfileImageUrl: String? = null,
    currentLanguage: AppLanguage = LocalAppLanguage.current,
    onProfileClick: () -> Unit = {},
    onYearPlannerClick: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    // Create a simple UserProfile for backward compatibility
    val userProfile = if (isAuthenticated && userProfileImageUrl != null) {
        io.sukhuat.dingo.domain.model.UserProfile(
            userId = "unknown",
            displayName = "User",
            email = "user@example.com",
            profileImageUrl = userProfileImageUrl,
            hasCustomImage = true, // Assume it's custom since we only have one URL
            joinDate = System.currentTimeMillis(),
            authProvider = io.sukhuat.dingo.domain.model.AuthProvider.EMAIL_PASSWORD
        )
    } else {
        null
    }

    UserDropdownMenu(
        isAuthenticated = isAuthenticated,
        userProfile = userProfile,
        currentLanguage = currentLanguage,
        onProfileClick = onProfileClick,
        onYearPlannerClick = onYearPlannerClick,
        onLanguageChange = onLanguageChange,
        onSettingsClick = onSettingsClick,
        onLogoutClick = onLogoutClick
    )
}

@Composable
fun LanguageItem(
    language: AppLanguage,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flag icon
        AsyncImage(
            model = language.flagResId,
            contentDescription = language.displayName,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Language name
        Text(
            text = language.displayName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        // Selection check
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
fun UserDropdownMenuPreview() {
    MountainSunriseTheme {
        Surface {
            UserDropdownMenu(
                isAuthenticated = true,
                userProfileImageUrl = null,
                currentLanguage = SupportedLanguages[0]
            )
        }
    }
}

@Preview
@Composable
fun UserDropdownMenuGuestPreview() {
    MountainSunriseTheme {
        Surface {
            UserDropdownMenu(
                isAuthenticated = false,
                userProfileImageUrl = null,
                currentLanguage = SupportedLanguages[0]
            )
        }
    }
}
