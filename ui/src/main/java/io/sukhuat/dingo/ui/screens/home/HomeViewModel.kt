package io.sukhuat.dingo.ui.screens.home

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.data.repository.DummyRepository
import io.sukhuat.dingo.usecases.auth.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "HomeViewModel"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: DummyRepository,
    private val signOutUseCase: SignOutUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Initial)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Settings for sound and vibration
    private var _soundEnabled = true
    private var _vibrationEnabled = true

    fun signOut(onSignOut: () -> Unit) {
        viewModelScope.launch {
            try {
                // Perform sign out logic here
                onSignOut()
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to sign out: ${e.message}")
            }
        }
    }

    /**
     * Provides haptic feedback when a goal is completed
     * @param intensity The intensity of the vibration (0-255)
     * @param duration The duration of the vibration in milliseconds
     */
    fun vibrateOnGoalCompleted(intensity: Int = 100, duration: Long = 100) {
        if (!_vibrationEnabled) return

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(
                            duration,
                            intensity
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error providing haptic feedback", e)
        }
    }

    /**
     * Toggle vibration feedback
     * @return The new state of vibration feedback
     */
    fun toggleVibration(): Boolean {
        _vibrationEnabled = !_vibrationEnabled
        return _vibrationEnabled
    }

    /**
     * Toggle sound feedback
     * @return The new state of sound feedback
     */
    fun toggleSound(): Boolean {
        _soundEnabled = !_soundEnabled
        return _soundEnabled
    }

    /**
     * Check if vibration is enabled
     * @return True if vibration is enabled
     */
    fun isVibrationEnabled(): Boolean = _vibrationEnabled

    /**
     * Check if sound is enabled
     * @return True if sound is enabled
     */
    fun isSoundEnabled(): Boolean = _soundEnabled
}
