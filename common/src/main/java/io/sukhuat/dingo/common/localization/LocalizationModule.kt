package io.sukhuat.dingo.common.localization

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.LaunchedEffect
import io.sukhuat.dingo.common.R
import java.util.Locale
import kotlinx.coroutines.launch

/**
 * Represents a language supported by the application
 * @param code ISO language code (e.g., "en", "es")
 * @param displayName Name of the language in its native form
 * @param flagResId Resource ID of the flag image
 */
data class AppLanguage(
    val code: String,
    val displayName: String,
    val flagResId: Int
)

/**
 * List of languages supported by the application
 */
val SupportedLanguages = listOf(
    AppLanguage("en", "English", R.drawable.uk_flag),
    AppLanguage("vi", "Tiếng Việt", R.drawable.vn_flag)
)

/**
 * CompositionLocal to provide the current app language throughout the composition
 */
val LocalAppLanguage = compositionLocalOf { SupportedLanguages[0] }

/**
 * CompositionLocal to track language updates and trigger recomposition
 */
val LocalLanguageUpdateState = compositionLocalOf { mutableIntStateOf(0) }

/**
 * Helper class for locale/language operations
 */
object LocaleHelper {
    /**
     * Sets the app's locale based on the provided language code
     * @param context The application context
     * @param languageCode ISO language code (e.g., "en", "es")
     * @return A new context with the updated configuration
     */
    fun setLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        // Create a new context with the updated configuration
        return context.createConfigurationContext(config)
    }

    /**
     * Gets the AppLanguage object for the given language code
     * @param code ISO language code (e.g., "en", "es")
     * @return The corresponding AppLanguage, or the default language if not found
     */
    fun getLanguageFromCode(code: String): AppLanguage {
        return SupportedLanguages.find { it.code == code } ?: SupportedLanguages[0]
    }

    /**
     * Gets the current system language code
     * @return ISO language code of the current system locale
     */
    fun getSystemLanguageCode(): String {
        return Locale.getDefault().language
    }
}

/**
 * Changes the app language and triggers a recomposition
 * @param context The application context
 * @param languageCode ISO language code (e.g., "en", "es")
 */
@Composable
fun changeAppLanguage(context: Context, languageCode: String) {
    val state = LocalLanguageUpdateState.current
    
    // Update the language preferences in a coroutine
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(languageCode) {
        coroutineScope.launch {
            val languagePreferences = LanguagePreferences(context)
            languagePreferences.setLanguageCode(languageCode)
            
            // Apply the new locale
            LocaleHelper.setLocale(context, languageCode)
            
            // Recreate the activity to apply the language change
            if (context is android.app.Activity) {
                context.recreate()
            }
        }
    }
    
    // Update the state to trigger recomposition
    state.intValue = state.intValue + 1
} 