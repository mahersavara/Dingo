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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.components.ButtonType
import io.sukhuat.dingo.common.components.DingoButton
import io.sukhuat.dingo.common.components.DingoCard
import io.sukhuat.dingo.common.components.DingoScaffold
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.localization.changeAppLanguage
import io.sukhuat.dingo.common.theme.RusticGold
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage = LocalAppLanguage.current

    // Show error messages in snackbar
    LaunchedEffect(uiState) {
        if (uiState is HomeUiState.Error) {
            snackbarHostState.showSnackbar(message = (uiState as HomeUiState.Error).message)
        }
    }

    DingoScaffold(
        title = stringResource(R.string.app_name),
        useGradientBackground = true,
        snackbarHostState = snackbarHostState,
        isLoading = uiState is HomeUiState.Loading,
        // Add user dropdown menu
        showUserMenu = true,
        isAuthenticated = true, // User is authenticated on the home screen
        currentLanguage = currentLanguage,
        onProfileClick = {
            // TODO: Navigate to profile screen
        },
        onLanguageChange = { languageCode ->
            coroutineScope.launch {
                changeAppLanguage(context, languageCode)
            }
        },
        onSettingsClick = {
            // TODO: Navigate to settings screen
        },
        onLogoutClick = {
            viewModel.signOut(onSignOut)
        }
    ) { paddingValues ->
        HomeContent(
            paddingValues = paddingValues
        )
    }
}

@Composable
private fun HomeContent(
    paddingValues: PaddingValues
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
                    text = stringResource(R.string.welcome_message),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = RusticGold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.today_sunrise),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.perfect_conditions),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Featured Journey Card
        DingoCard(
            title = stringResource(R.string.featured_journey),
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
                    text = stringResource(R.string.fansipan_trek),
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
                        text = stringResource(R.string.elevation_info),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.duration_info),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.trek_description),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DingoButton(
                        text = stringResource(R.string.details),
                        onClick = { /* TODO: Navigate to details */ },
                        type = ButtonType.OUTLINED,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    DingoButton(
                        text = stringResource(R.string.save),
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
            title = stringResource(R.string.recent_adventures),
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
                    text = stringResource(R.string.no_adventures),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                DingoButton(
                    text = stringResource(R.string.start_journey),
                    onClick = { /* TODO: Navigate to journey creation */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
