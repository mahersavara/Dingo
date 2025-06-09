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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.CloudGray
import io.sukhuat.dingo.common.theme.DeepIndigo
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.common.theme.ScreenPaddingHorizontal
import io.sukhuat.dingo.common.theme.ScreenPaddingVertical

/**
 * A reusable scaffold component with Mountain Sunrise design system
 * @param title Optional title for the top app bar
 * @param showTopBar Whether to show the top app bar
 * @param topBarActions Optional actions for the top app bar
 * @param navigationIcon Optional navigation icon for the top app bar
 * @param useGradientBackground Whether to use a gradient background
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
    useGradientBackground: Boolean = false,
    isLoading: Boolean = false,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    floatingActionButton: @Composable () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(
        horizontal = ScreenPaddingHorizontal,
        vertical = ScreenPaddingVertical
    ),
    content: @Composable (PaddingValues) -> Unit
) {
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

    // Create a gradient for the top app bar
    val topAppBarGradient = Brush.horizontalGradient(
        colors = listOf(
            DeepIndigo.copy(alpha = 0.95f),
            DeepIndigo,
            DeepIndigo.copy(alpha = 0.9f)
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
                                color = if (it.uppercase() == it) RusticGold else CloudGray,
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
                                topBarActions()
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

@Preview(showBackground = true)
@Composable
fun DingoScaffoldPreview() {
    MountainSunriseTheme {
        DingoScaffold(
            title = "ALPINE EXPLORER"
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
            title = "ALPINE EXPLORER",
            useGradientBackground = true
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
fun DingoScaffoldLoadingPreview() {
    MountainSunriseTheme {
        DingoScaffold(
            title = "JOURNEY DETAILS",
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
