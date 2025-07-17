package io.sukhuat.dingo.domain.usecase.preferences

import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting audio and feedback preferences
 */
class GetAudioFeedbackPreferencesUseCase @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) {
    /**
     * Get audio and feedback preferences as a flow
     */
    operator fun invoke(): Flow<AudioFeedbackPreferences> {
        return userPreferencesRepository.getUserPreferences().map { preferences ->
            AudioFeedbackPreferences(
                soundEnabled = preferences.soundEnabled,
                vibrationEnabled = preferences.vibrationEnabled
            )
        }
    }
}

/**
 * Data class representing audio and feedback preferences
 */
data class AudioFeedbackPreferences(
    val soundEnabled: Boolean,
    val vibrationEnabled: Boolean
) {
    /**
     * Check if any feedback is enabled
     */
    val hasFeedbackEnabled: Boolean
        get() = soundEnabled || vibrationEnabled
}
