package io.sukhuat.dingo.common.localization

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore instance for language preferences
 */
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_preferences")

/**
 * Key for storing the language code preference
 */
private val LANGUAGE_CODE_KEY = stringPreferencesKey("language_code")

/**
 * Manages language preferences using DataStore
 */
@Singleton
class LanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    /**
     * Flow of the current language code
     */
    val languageCodeFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[LANGUAGE_CODE_KEY] ?: "en" // Default to English
        }

    /**
     * Sets the language code preference
     * @param code ISO language code (e.g., "en", "vi")
     */
    suspend fun setLanguageCode(code: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE_KEY] = code
        }
    }

    /**
     * Gets the current AppLanguage object
     * @return Flow of the current AppLanguage
     */
    fun getCurrentLanguage(): Flow<AppLanguage> = languageCodeFlow
        .map { code ->
            LocaleHelper.getLanguageFromCode(code)
        }
}
