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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

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
                text = stringResource(R.string.app_name_line1),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = RusticGold,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.app_name_line2),
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

    // Check auth status and navigate accordingly with timeout protection and extensive logging
    LaunchedEffect(Unit) {
        try {
            android.util.Log.e("EMERGENCY_DEBUG", "🚨 SplashScreen - LaunchedEffect started")
            android.util.Log.d("SplashScreen", "Starting authentication check...")

            withTimeout(3000) { // Reduced to 3 second timeout for faster testing
                android.util.Log.d("SplashScreen", "Calling viewModel.checkUserAuthStatus()...")
                val isAuthenticated = viewModel.checkUserAuthStatus()
                android.util.Log.d("SplashScreen", "Authentication result: $isAuthenticated")

                if (isAuthenticated) {
                    android.util.Log.e("EMERGENCY_DEBUG", "🚨 SplashScreen - User authenticated, calling onNavigateToHome()")
                    android.util.Log.d("SplashScreen", "User authenticated, navigating to home...")
                    onNavigateToHome()
                } else {
                    android.util.Log.e("EMERGENCY_DEBUG", "🚨 SplashScreen - User NOT authenticated, calling onNavigateToAuth()")
                    android.util.Log.d("SplashScreen", "User not authenticated, navigating to auth...")
                    onNavigateToAuth()
                }
            }
        } catch (e: TimeoutCancellationException) {
            // If authentication check times out, default to auth screen
            android.util.Log.w("SplashScreen", "Authentication check timed out, navigating to auth")
            onNavigateToAuth()
        } catch (e: Exception) {
            // If any other error occurs, default to auth screen
            android.util.Log.e("SplashScreen", "Error during authentication check", e)
            onNavigateToAuth()
        }
    }
}
