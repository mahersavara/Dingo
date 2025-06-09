package io.sukhuat.dingo.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.components.ButtonType
import io.sukhuat.dingo.common.components.DingoButton
import io.sukhuat.dingo.common.components.DingoCard
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.theme.RusticGold

@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show error messages in snackbar
    LaunchedEffect(uiState) {
        if (uiState is HomeUiState.Error) {
            snackbarHostState.showSnackbar(message = (uiState as HomeUiState.Error).message)
        }
    }

    DingoScaffold(
        title = "ALPINE EXPLORER",
        useGradientBackground = true,
        topBarActions = {
            IconButton(onClick = { viewModel.signOut(onSignOut) }) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Sign out"
                )
            }
        },
        snackbarHostState = snackbarHostState,
        isLoading = uiState is HomeUiState.Loading
    ) { paddingValues ->
        HomeContent(
            paddingValues = paddingValues,
            onSignOut = { viewModel.signOut(onSignOut) }
        )
    }
}

@Composable
private fun HomeContent(
    paddingValues: PaddingValues,
    onSignOut: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Welcome Card
        DingoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            accentBorder = true,
            useGradientBackground = false
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Good morning, Adventurer",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = RusticGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Today's sunrise: 6:24 AM",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Perfect conditions for mountain exploration",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Featured Journey Card
        DingoCard(
            title = "FEATURED JOURNEY",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            accentBorder = false,
            useGradientBackground = true
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Fansipan Sunrise Trek",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = RusticGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = RusticGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Elevation: 3,143m",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Duration: 2 days",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Experience the breathtaking sunrise over the Hoang Lien Son mountain range. This challenging trek rewards you with panoramic views and unforgettable memories.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DingoButton(
                        text = "Details",
                        onClick = { /* TODO: Navigate to details */ },
                        type = ButtonType.OUTLINED,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    DingoButton(
                        text = "Save",
                        onClick = { /* TODO: Save journey */ },
                        type = ButtonType.FILLED,
                        modifier = Modifier.weight(1f),
                        leadingIcon = Icons.Default.Favorite
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent Adventures Card
        DingoCard(
            title = "RECENT ADVENTURES",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            accentBorder = true,
            useGradientBackground = false
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "No recent adventures yet",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                DingoButton(
                    text = "Start Your Journey",
                    onClick = { /* TODO: Navigate to journey creation */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        DingoButton(
            text = "Sign Out",
            onClick = onSignOut,
            type = ButtonType.OUTLINED,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}
