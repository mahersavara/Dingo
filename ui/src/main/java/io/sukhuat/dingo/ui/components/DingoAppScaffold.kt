package io.sukhuat.dingo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.localization.AppLanguage
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * A wrapper around DingoScaffold that provides common functionality for all screens.
 * This composable handles error messages, loading states, and other common UI elements.
 *
 * @param title The title to display in the app bar
 * @param showTopBar Whether to show the top app bar
 * @param topBarActions Actions to display in the top app bar
 * @param navigationIcon Navigation icon to display in the top app bar
 * @param useGradientBackground Whether to use a gradient background
 * @param isLoading Whether to show a loading indicator
 * @param errorMessage Error message to display in a snackbar
 * @param onErrorDismiss Called when the error message is dismissed
 * @param floatingActionButton Optional floating action button
 * @param contentPadding Padding values for the content
 * @param isAuthenticated Whether the user is authenticated
 * @param userProfileImageUrl URL of the user's profile image
 * @param currentLanguage The currently selected language
 * @param showUserMenu Whether to show the user dropdown menu
 * @param onProfileClick Called when the profile option is clicked
 * @param onLanguageChange Called when a language is selected
 * @param onSettingsClick Called when the settings option is clicked
 * @param onLogoutClick Called when the logout option is clicked
 * @param content Content to display in the scaffold
 */
@Composable
fun DingoAppScaffold(
    title: String? = null,
    showTopBar: Boolean = true,
    topBarActions: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    useGradientBackground: Boolean = true,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorDismiss: () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    isAuthenticated: Boolean = false,
    userProfileImageUrl: String? = null,
    currentLanguage: AppLanguage? = null,
    showUserMenu: Boolean = false,
    onProfileClick: () -> Unit = {},
    onYearPlannerClick: () -> Unit = {},
    onLanguageChange: (String) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error message in snackbar if provided
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            val result = snackbarHostState.showSnackbar(
                message = it,
                actionLabel = "Dismiss",
                duration = SnackbarDuration.Long
            )

            if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
                onErrorDismiss()
            }
        }
    }

    DingoScaffold(
        title = title,
        showTopBar = showTopBar,
        topBarActions = { topBarActions() },
        navigationIcon = navigationIcon,
        useGradientBackground = useGradientBackground,
        isLoading = isLoading,
        snackbarHostState = snackbarHostState,
        floatingActionButton = floatingActionButton,
        contentPadding = contentPadding,
        isAuthenticated = isAuthenticated,
        userProfileImageUrl = userProfileImageUrl,
        currentLanguage = currentLanguage ?: LocalAppLanguage.current,
        showUserMenu = showUserMenu,
        onProfileClick = onProfileClick,
        onYearPlannerClick = onYearPlannerClick,
        onLanguageChange = onLanguageChange,
        onSettingsClick = onSettingsClick,
        onLogoutClick = onLogoutClick
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            content(paddingValues)
        }
    }
}

/**
 * Extension function to show an error message in a snackbar
 */
fun SnackbarHostState.showError(
    scope: CoroutineScope,
    message: String,
    actionLabel: String = "Dismiss",
    onDismiss: () -> Unit = {}
) {
    scope.launch {
        val result = showSnackbar(
            message = message,
            actionLabel = actionLabel,
            duration = SnackbarDuration.Long
        )

        if (result == SnackbarResult.Dismissed || result == SnackbarResult.ActionPerformed) {
            onDismiss()
        }
    }
}
