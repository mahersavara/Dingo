package io.sukhuat.dingo.common.localization

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

// Create a static composition local for language updates
val LocalLanguageUpdate = staticCompositionLocalOf { 0L }

/**
 * A composable that provides language configuration throughout the app
 * @param content The content to provide the language configuration to
 */
@Composable
fun LanguageProvider(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val languagePreferences = remember { LanguagePreferences(context) }

    // Track language updates with a timestamp to force recomposition
    var languageUpdateTime by remember { mutableStateOf(System.currentTimeMillis()) }

    // Get the initial language code - we need to do this synchronously
    val initialLanguageCode = runBlocking { languagePreferences.languageCodeFlow.first() }

    // Set up state for the current language
    var currentLanguage by remember { mutableStateOf(LocaleHelper.getLanguageFromCode(initialLanguageCode)) }

    // Update the app's locale when language changes
    LaunchedEffect(currentLanguage) {
        val newContext = LocaleHelper.setLocale(context, currentLanguage.code)
        val config = newContext.resources.configuration
        configuration.setTo(config)
        languageUpdateTime = System.currentTimeMillis()
    }

    // Observe language changes from preferences
    val languageCode by languagePreferences.languageCodeFlow.collectAsState(initial = initialLanguageCode)

    // Update the current language when preferences change
    LaunchedEffect(languageCode) {
        currentLanguage = LocaleHelper.getLanguageFromCode(languageCode)
    }

    // Provide the current language to the composition
    CompositionLocalProvider(
        LocalAppLanguage provides currentLanguage,
        LocalLanguageUpdate provides languageUpdateTime
    ) {
        content()
    }
}

/**
 * A function that allows changing the app language
 * @param context The Android context
 * @param languageCode ISO language code to change to
 */
suspend fun changeAppLanguage(context: Context, languageCode: String) {
    val languagePreferences = LanguagePreferences(context)

    // Only update if the language is actually changing
    val currentLanguage = languagePreferences.languageCodeFlow.first()
    if (currentLanguage != languageCode) {
        // Save the new language preference
        languagePreferences.setLanguageCode(languageCode)

        // If in an activity context, recreate the activity with animation
        if (context is android.app.Activity) {
            context.runOnUiThread {
                // Use a fade animation for smoother transition
                context.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

                // Add a small delay to allow the preference to be saved
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    // Recreate with animation
                    context.recreate()
                }, 50) // Reduced delay for faster response
            }
        }
    }
}
