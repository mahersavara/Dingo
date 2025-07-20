package io.sukhuat.dingo.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.ui.screens.profile.components.SharingComponents

/**
 * Example integration showing how to use SharingComponents in a profile screen
 * This demonstrates how the sharing functionality would be integrated into the main profile UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharingIntegrationExample(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile - Sharing Features") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Other profile sections would go here (ProfileHeader, ProfileStatistics, etc.)

            // Sharing Components Section
            SharingComponents(
                onShareAchievement = { achievementId ->
                    // This would be handled by the parent ProfileScreen or ProfileViewModel
                    // The sharing components handle the actual sharing logic internally
                    println("Share achievement: $achievementId")
                }
            )

            // Other profile sections would continue here (AccountSecurity, DataManagement, etc.)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SharingIntegrationExamplePreview() {
    MountainSunriseTheme {
        SharingIntegrationExample()
    }
}
