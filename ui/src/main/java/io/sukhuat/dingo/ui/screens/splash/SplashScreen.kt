package io.sukhuat.dingo.ui.screens.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAuth: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    // Show loading indicator and logo
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // You can add your logo or branding here
        CircularProgressIndicator()
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
