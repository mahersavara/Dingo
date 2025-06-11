package io.sukhuat.dingo.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val extendedColors = MountainSunriseTheme.extendedColors

    // Create a gradient background
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            extendedColors.surfaceGradientStart,
            extendedColors.surfaceGradientMiddle,
            extendedColors.surfaceGradientEnd
        )
    )

    // Show loading indicator and logo
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App name with Mountain Sunrise styling
            Text(
                text = "TRAVELER'S",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = RusticGold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "JOURNEY",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = RusticGold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Loading indicator
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = RusticGold,
                strokeWidth = 3.dp
            )
        }
    }

    // Check auth status and navigate accordingly
    LaunchedEffect(Unit) {
        val isAuthenticated = viewModel.checkUserAuthStatus()
        if (isAuthenticated) {
            onNavigateToHome()
        } else {
            onNavigateToAuth()
        }
    }
}
