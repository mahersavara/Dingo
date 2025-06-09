package io.sukhuat.dingo.common.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.sukhuat.dingo.common.theme.DingoTheme
import io.sukhuat.dingo.common.theme.ScreenPaddingHorizontal
import io.sukhuat.dingo.common.theme.ScreenPaddingVertical

/**
 * A reusable scaffold component for the Dingo app
 * @param title Optional title for the top app bar
 * @param showTopBar Whether to show the top app bar
 * @param topBarActions Optional actions for the top app bar
 * @param navigationIcon Optional navigation icon for the top app bar
 * @param backgroundColor Background color for the screen
 * @param isLoading Whether to show a loading indicator
 * @param snackbarHostState SnackbarHostState for displaying snackbars
 * @param floatingActionButton Optional floating action button
 * @param contentPadding Padding values for the content
 * @param content Content to display in the scaffold
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DingoScaffold(
    title: String? = null,
    showTopBar: Boolean = true,
    topBarActions: @Composable RowScope.() -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    floatingActionButton: @Composable () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(
        horizontal = ScreenPaddingHorizontal,
        vertical = ScreenPaddingVertical
    ),
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        title?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    },
                    navigationIcon = navigationIcon,
                    actions = topBarActions,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = backgroundColor,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = floatingActionButton,
        containerColor = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                content(contentPadding)

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
}

@Preview(showBackground = true)
@Composable
fun DingoScaffoldPreview() {
    DingoTheme {
        DingoScaffold(
            title = "Screen Title"
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
fun DingoScaffoldLoadingPreview() {
    DingoTheme {
        DingoScaffold(
            title = "Loading Screen",
            isLoading = true
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
