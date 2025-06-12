package io.sukhuat.dingo

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.sukhuat.dingo.common.localization.LanguageProvider
import io.sukhuat.dingo.common.localization.LocaleHelper
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.navigation.Screen
import io.sukhuat.dingo.ui.screens.auth.AuthScreen
import io.sukhuat.dingo.ui.screens.home.HomeScreen
import io.sukhuat.dingo.ui.screens.splash.SplashScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Track if this is a recreation due to language change
    private var isLanguageChange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Apply smooth transition if this is a recreation due to language change
        if (isLanguageChange) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            isLanguageChange = false
        }

        setContent {
            // Wrap the app in the language provider to enable multilanguage support
            LanguageProvider {
                MountainSunriseTheme {
                    DingoApp()
                }
            }
        }
    }

    // Called when activity is recreated due to language change
    override fun recreate() {
        isLanguageChange = true
        super.recreate()
    }

    // Override attachBaseContext to apply the saved language
    override fun attachBaseContext(newBase: Context) {
        // Get the saved language code
        val languagePreferences = io.sukhuat.dingo.common.localization.LanguagePreferences(newBase)
        val languageCode = runBlocking { languagePreferences.languageCodeFlow.first() }

        // Apply the saved language
        val context = LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    // Handle configuration changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply the saved language
        val languagePreferences = io.sukhuat.dingo.common.localization.LanguagePreferences(this)
        val languageCode = runBlocking { languagePreferences.languageCodeFlow.first() }
        LocaleHelper.setLocale(this, languageCode)
    }
}

@Composable
fun DingoApp() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToAuth = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Auth.route) {
                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onSignOut = {
                        navController.navigate(Screen.Auth.route) {
                            // Clear the back stack so user can't go back to Home after signing out
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
