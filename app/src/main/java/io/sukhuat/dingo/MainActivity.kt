package io.sukhuat.dingo

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import dagger.hilt.android.AndroidEntryPoint
import io.sukhuat.dingo.common.localization.LocalAppLanguage
import io.sukhuat.dingo.common.localization.LocalLanguageUpdateState
import io.sukhuat.dingo.common.theme.DingoTheme
import io.sukhuat.dingo.navigation.Screen
import io.sukhuat.dingo.ui.screens.auth.AuthScreen
import io.sukhuat.dingo.ui.screens.home.HomeScreen
import io.sukhuat.dingo.ui.screens.profile.ProfileScreen
import io.sukhuat.dingo.ui.screens.settings.SettingsScreen
import io.sukhuat.dingo.ui.screens.splash.SplashScreen
import io.sukhuat.dingo.ui.screens.yearplanner.YearPlannerScreen
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
            // Use a smoother crossfade animation
            overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            isLanguageChange = false
        }

        setContent {
            DingoTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Get the saved language code
                    val languagePreferences = remember { io.sukhuat.dingo.common.localization.LanguagePreferences(this) }
                    val languageCode = runBlocking { languagePreferences.languageCodeFlow.first() }
                    val currentLanguage = io.sukhuat.dingo.common.localization.LocaleHelper.getLanguageFromCode(languageCode)

                    // Provide the language context with the saved language
                    CompositionLocalProvider(
                        LocalAppLanguage provides currentLanguage,
                        LocalLanguageUpdateState provides androidx.compose.runtime.mutableIntStateOf(0)
                    ) {
                        DingoApp()
                    }
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
        val context = io.sukhuat.dingo.common.localization.LocaleHelper.setLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }

    // Handle configuration changes
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Re-apply the saved language
        val languagePreferences = io.sukhuat.dingo.common.localization.LanguagePreferences(this)
        val languageCode = runBlocking { languagePreferences.languageCodeFlow.first() }
        io.sukhuat.dingo.common.localization.LocaleHelper.setLocale(this, languageCode)
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
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToYearPlanner = {
                        navController.navigate(Screen.YearPlannerCurrent.route)
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }
            composable(
                route = Screen.Profile.route,
                deepLinks = listOf(navDeepLink { uriPattern = "dingo://profile" })
            ) {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(
                route = Screen.ProfileEdit.route,
                deepLinks = listOf(navDeepLink { uriPattern = "dingo://profile/edit" })
            ) {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(
                route = Screen.ProfileStatistics.route,
                deepLinks = listOf(navDeepLink { uriPattern = "dingo://profile/statistics" })
            ) {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(
                route = Screen.ProfileAccount.route,
                deepLinks = listOf(navDeepLink { uriPattern = "dingo://profile/account" })
            ) {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(
                route = Screen.ProfileHelp.route,
                deepLinks = listOf(navDeepLink { uriPattern = "dingo://profile/help" })
            ) {
                ProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    }
                )
            }
            composable(Screen.YearPlannerCurrent.route) {
                YearPlannerScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.YearPlanner.route) { backStackEntry ->
                val year = backStackEntry.arguments?.getString("year")?.toIntOrNull() 
                    ?: java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                YearPlannerScreen(
                    year = year,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
