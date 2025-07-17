package io.sukhuat.dingo.ui.screens.home

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.domain.model.GoalStatus
import io.sukhuat.dingo.domain.usecase.goal.CreateGoalUseCase
import io.sukhuat.dingo.domain.usecase.goal.DeleteGoalUseCase
import io.sukhuat.dingo.domain.usecase.goal.GetGoalsUseCase
import io.sukhuat.dingo.domain.usecase.goal.ReorderGoalsUseCase
import io.sukhuat.dingo.domain.usecase.goal.UpdateGoalStatusUseCase
import io.sukhuat.dingo.domain.usecase.goal.UpdateGoalUseCase
import io.sukhuat.dingo.usecases.auth.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val TAG = "HomeViewModel"

/**
 * Key for storing the last week number in preferences
 */
private const val PREF_LAST_WEEK_NUMBER = "last_week_number"
private const val PREF_LAST_YEAR = "last_year"

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getGoalsUseCase: GetGoalsUseCase,
    private val createGoalUseCase: CreateGoalUseCase,
    private val updateGoalUseCase: UpdateGoalUseCase,
    private val updateGoalStatusUseCase: UpdateGoalStatusUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val reorderGoalsUseCase: ReorderGoalsUseCase,
    private val signOutUseCase: SignOutUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Goals state
    val allGoals = getGoalsUseCase()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val goals = getGoalsUseCase()
        .map { goals -> goals.filter { it.status == GoalStatus.ACTIVE } }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val archivedGoals = getGoalsUseCase()
        .map { goals -> goals.filter { it.status == GoalStatus.ARCHIVED } }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedGoals = getGoalsUseCase()
        .map { goals -> goals.filter { it.status == GoalStatus.COMPLETED } }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val failedGoals = getGoalsUseCase()
        .map { goals -> goals.filter { it.status == GoalStatus.FAILED } }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Weekly wrap-up state
    private val _showWeeklyWrapUp = mutableStateOf(false)
    val showWeeklyWrapUp: State<Boolean> = _showWeeklyWrapUp

    // Total goals for the week (active + completed + failed)
    val totalWeeklyGoals = MutableStateFlow(0)

    // Settings for sound and vibration
    private val _soundEnabled = mutableStateOf(true)
    val soundEnabled: State<Boolean> = _soundEnabled

    private val _vibrationEnabled = mutableStateOf(true)
    val vibrationEnabled: State<Boolean> = _vibrationEnabled

    init {
        loadGoals()
        checkWeekChange()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                // The goals are already loaded via StateFlow
                _uiState.value = HomeUiState.Success
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to load goals: ${e.message}")
            }
        }
    }

    /**
     * Check if the week has changed since the last time the app was opened
     * If so, show the weekly wrap-up dialog
     */
    private fun checkWeekChange() {
        val prefs = context.getSharedPreferences("dingo_prefs", Context.MODE_PRIVATE)

        // Get current week number and year
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        // Get last recorded week number and year
        val lastWeek = prefs.getInt(PREF_LAST_WEEK_NUMBER, -1)
        val lastYear = prefs.getInt(PREF_LAST_YEAR, -1)

        // Check if week has changed
        if (lastWeek != -1 && (currentWeek != lastWeek || currentYear != lastYear)) {
            // Week has changed, show weekly wrap-up
            viewModelScope.launch {
                // Calculate total goals for the week
                val activeCount = goals.value.size
                val completedCount = completedGoals.value.size
                val failedCount = failedGoals.value.size
                totalWeeklyGoals.value = activeCount + completedCount + failedCount

                // Mark incomplete goals as failed
                goals.value.forEach { goal ->
                    updateGoalStatus(goal.id, GoalStatus.FAILED)
                }

                // Show weekly wrap-up
                _showWeeklyWrapUp.value = true
            }
        }

        // Save current week number and year
        prefs.edit()
            .putInt(PREF_LAST_WEEK_NUMBER, currentWeek)
            .putInt(PREF_LAST_YEAR, currentYear)
            .apply()
    }

    /**
     * Dismiss the weekly wrap-up dialog
     */
    fun dismissWeeklyWrapUp() {
        _showWeeklyWrapUp.value = false
    }

    fun createGoal(text: String, imageResId: Int? = null, customImage: String? = null) {
        viewModelScope.launch {
            try {
                createGoalUseCase(text, imageResId, customImage)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to create goal: ${e.message}")
            }
        }
    }

    fun updateGoalStatus(goalId: String, status: GoalStatus) {
        viewModelScope.launch {
            try {
                updateGoalStatusUseCase(goalId, status)

                // Provide haptic feedback if completing a goal
                if (status == GoalStatus.COMPLETED) {
                    vibrateOnGoalCompleted()
                }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to update goal status: ${e.message}")
            }
        }
    }

    fun updateGoalText(goalId: String, text: String) {
        viewModelScope.launch {
            try {
                updateGoalUseCase.updateText(goalId, text)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to update goal text: ${e.message}")
            }
        }
    }

    fun updateGoalImage(goalId: String, customImage: String?) {
        viewModelScope.launch {
            try {
                val result = updateGoalUseCase.updateImage(goalId, customImage)
                if (result.isSuccess) {
                    // Temporarily set loading state to trigger a UI refresh
                    val currentState = _uiState.value
                    _uiState.value = HomeUiState.Loading
                    // Restore previous state to avoid disrupting the UI
                    _uiState.value = currentState

                    Log.d(TAG, "Goal image updated successfully: $goalId with image $customImage")
                } else {
                    Log.e(TAG, "Failed to update goal image: ${result.exceptionOrNull()?.message}")
                    _uiState.value = HomeUiState.Error("Failed to update goal image")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception updating goal image", e)
                _uiState.value = HomeUiState.Error("Failed to update goal image: ${e.message}")
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            try {
                deleteGoalUseCase(goalId)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to delete goal: ${e.message}")
            }
        }
    }

    fun reorderGoals(goalIds: List<String>) {
        viewModelScope.launch {
            try {
                reorderGoalsUseCase(goalIds)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to reorder goals: ${e.message}")
            }
        }
    }

    fun signOut(onSignOut: () -> Unit) {
        viewModelScope.launch {
            try {
                // Clear all goals from local database first
                getGoalsUseCase.clearAllGoals()

                // Then sign out
                signOutUseCase()
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
        if (!vibrationEnabled.value) return

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
     */
    fun toggleVibration() {
        _vibrationEnabled.value = !_vibrationEnabled.value
    }

    /**
     * Toggle sound feedback
     */
    fun toggleSound() {
        _soundEnabled.value = !_soundEnabled.value
    }

    /**
     * Share weekly summary
     * @return A string with the weekly summary
     */
    fun getWeeklySummaryText(): String {
        val completedCount = completedGoals.value.size
        val totalCount = totalWeeklyGoals.value
        val percentage = if (totalCount > 0) (completedCount * 100 / totalCount) else 0

        return "Weekly Wrap-Up: I completed $completedCount/$totalCount goals ($percentage%)! #Dingo #WeeklyGoals"
    }
}
