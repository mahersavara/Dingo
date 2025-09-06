package io.sukhuat.dingo.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.localization.AppLanguage
import io.sukhuat.dingo.common.localization.LocalLanguageUpdateState
import io.sukhuat.dingo.common.localization.SupportedLanguages
import io.sukhuat.dingo.common.theme.CloudGray
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.theme.ScreenPaddingHorizontal
import io.sukhuat.dingo.common.theme.ScreenPaddingVertical
import io.sukhuat.dingo.domain.model.UserProfile

/**
 * Enhanced DingoScaffold that accepts a UserProfile object
 * This enables full Google Sign-In support with image priority logic
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DingoScaffold(
    title: String? = null,
    showTopBar: Boolean = true,
    topBarActions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    useGradientBackground: Boolean = false,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    floatingActionButton: @Composable () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(
        horizontal = ScreenPaddingHorizontal,
        vertical = ScreenPaddingVertical
    ),
    isAuthenticated: Boolean = false,
    userProfile: UserProfile? = null,
    currentLanguage: AppLanguage = SupportedLanguages[0],
    showUserMenu: Boolean = false,
    onProfileClick: () -> Unit = {},
    onYearPlannerClick: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    // Force recomposition when language changes
    LocalLanguageUpdateState.current

    // Get screen size for responsive layout
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    // Adjust padding based on screen size
    val horizontalPadding = when {
        screenWidth < 600.dp -> 16.dp
        screenWidth < 840.dp -> 24.dp
        else -> 32.dp
    }

    val responsiveContentPadding = PaddingValues(
        horizontal = horizontalPadding,
        vertical = ScreenPaddingVertical
    )

    val extendedColors = MountainSunriseTheme.extendedColors

    // Create gradient background if enabled
    val backgroundBrush = if (useGradientBackground) {
        Brush.verticalGradient(
            colors = listOf(
                extendedColors.surfaceGradientStart,
                extendedColors.surfaceGradientMiddle,
                extendedColors.surfaceGradientEnd
            )
        )
    } else {
        null
    }

    // Use either solid background or transparent for gradient
    val backgroundColor = if (useGradientBackground) {
        Color.Transparent
    } else {
        extendedColors.backgroundVariant
    }

    // Create a theme-aware gradient for the top app bar
    val topAppBarGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    )

    Scaffold(
        topBar = {
            if (showTopBar) {
                // Custom top app bar with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(brush = topAppBarGradient)
                        .shadow(4.dp)
                ) {
                    // Navigation icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                    ) {
                        navigationIcon()
                    }

                    // Title
                    title?.let {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(horizontal = 64.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (it.uppercase() == it) RusticGold else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Actions
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 16.dp)
                    ) {
                        Surface(
                            color = Color.Transparent,
                            contentColor = CloudGray
                        ) {
                            Row {
                                // Standard actions
                                topBarActions()

                                // User menu (if enabled) with enhanced UserProfile support
                                if (showUserMenu) {
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
                            }
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = floatingActionButton,
        containerColor = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .then(
                    if (useGradientBackground) {
                        Modifier.background(brush = backgroundBrush!!)
                    } else {
                        Modifier
                    }
                )
        ) {
            // Use responsive content padding if none was provided
            content(
                if (contentPadding == PaddingValues(
                        horizontal = ScreenPaddingHorizontal,
                        vertical = ScreenPaddingVertical
                    )
                ) {
                    responsiveContentPadding
                } else {
                    contentPadding
                }
            )

            // Show loading indicator if needed
            if (isLoading) {
                LoadingIndicator(
                    isFullScreen = true,
                    backgroundColor = Color.Black,
                    contentAlpha = 0.3f
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoScaffoldPreview() {
    MountainSunriseTheme {
        DingoScaffold(
            title = "TRAVELER'S JOURNEY",
            userProfile = null
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Screen content goes here",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoScaffoldGradientPreview() {
    MountainSunriseTheme {
        DingoScaffold(
            title = "TRAVELER'S JOURNEY",
            useGradientBackground = true,
            userProfile = null
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Screen with gradient background",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoScaffoldWithUserMenuPreview() {
    MountainSunriseTheme {
        DingoScaffold(
            title = "TRAVELER'S JOURNEY",
            showUserMenu = true,
            isAuthenticated = true,
            userProfile = null,
            currentLanguage = SupportedLanguages[0]
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Screen with user menu",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DingoScaffoldLoadingPreview() {
    MountainSunriseTheme {
        DingoScaffold(
            title = "JOURNEY DETAILS",
            isLoading = true,
            userProfile = null
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "This content is behind the loading indicator",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
