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
import io.sukhuat.dingo.domain.usecase.goal.MigrateGoalWeekDataUseCase
import io.sukhuat.dingo.domain.usecase.goal.ReorderGoalsUseCase
import io.sukhuat.dingo.domain.usecase.goal.UpdateGoalStatusUseCase
import io.sukhuat.dingo.domain.usecase.goal.UpdateGoalUseCase
import io.sukhuat.dingo.usecases.auth.SignOutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val migrateGoalWeekDataUseCase: MigrateGoalWeekDataUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getUserProfileUseCase: io.sukhuat.dingo.domain.usecase.profile.GetUserProfileUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _userProfile = MutableStateFlow<io.sukhuat.dingo.domain.model.UserProfile?>(null)
    val userProfile: StateFlow<io.sukhuat.dingo.domain.model.UserProfile?> = _userProfile.asStateFlow()

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

    // Drag mode state
    private val _isDragModeActive = MutableStateFlow(false)
    val isDragModeActive: StateFlow<Boolean> = _isDragModeActive.asStateFlow()

    private val _isSavingPositions = MutableStateFlow(false)
    val isSavingPositions: StateFlow<Boolean> = _isSavingPositions.asStateFlow()

    private val _lastPositionSyncTime = MutableStateFlow<Long?>(null)
    val lastPositionSyncTime: StateFlow<Long?> = _lastPositionSyncTime.asStateFlow()

    // Enhanced drag state tracking for snapshot-based operations
    private val _dragOperations = MutableStateFlow<Map<String, Int>>(emptyMap())
    val dragOperations: StateFlow<Map<String, Int>> = _dragOperations.asStateFlow()

    private val _dragSnapshot = MutableStateFlow<Map<String, Int>>(emptyMap())
    private val _dragSessionActive = MutableStateFlow(false)
    val isDragSessionActive: StateFlow<Boolean> = _dragSessionActive.asStateFlow()
    val vibrationEnabled: State<Boolean> = _vibrationEnabled

    // Week navigation state
    private val _currentWeekOffset = MutableStateFlow(0)
    val currentWeekOffset: StateFlow<Int> = _currentWeekOffset.asStateFlow()

    // Available weeks with goals
    val weeksWithGoals = getGoalsUseCase.getWeeksWithGoals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(0) // Always include current week
        )

    // Goals for current week being viewed
    val currentWeekGoals = _currentWeekOffset
        .flatMapLatest { offset -> getGoalsUseCase.getGoalsForWeek(offset) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadGoals()
        loadUserProfile()
        checkWeekChange()
        migrateOldGoalData()
    }

    /**
     * Migrate old goals that don't have proper week/year data
     */
    private fun migrateOldGoalData() {
        viewModelScope.launch {
            try {
                val result = migrateGoalWeekDataUseCase()
                result.onSuccess { count ->
                    if (count > 0) {
                        Log.d(TAG, "Migrated $count goals with week/year data")
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Failed to migrate goal week data", error)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during goal migration", e)
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting to load user profile...")
                getUserProfileUseCase().collect { profile ->
                    Log.d(TAG, "User profile loaded: $profile")
                    _userProfile.value = profile
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load user profile", e)
                _userProfile.value = null
            }
        }
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

    /**
     * Navigate to a specific week offset
     */
    fun navigateToWeek(weekOffset: Int) {
        _currentWeekOffset.value = weekOffset
    }

    /**
     * Navigate to previous week (if exists)
     */
    fun navigateToPreviousWeek() {
        val availableWeeks = weeksWithGoals.value
        val currentIndex = availableWeeks.indexOf(_currentWeekOffset.value)
        if (currentIndex > 0) {
            _currentWeekOffset.value = availableWeeks[currentIndex - 1]
        }
    }

    /**
     * Navigate to next week (if exists)
     */
    fun navigateToNextWeek() {
        val availableWeeks = weeksWithGoals.value
        val currentIndex = availableWeeks.indexOf(_currentWeekOffset.value)
        if (currentIndex < availableWeeks.size - 1) {
            _currentWeekOffset.value = availableWeeks[currentIndex + 1]
        }
    }

    /**
     * Check if current week is read-only (not current week)
     */
    fun isCurrentWeekReadOnly(): Boolean {
        return _currentWeekOffset.value < 0
    }

    /**
     * Create a goal at a specific grid position
     */
    fun createGoalAtPosition(text: String, position: Int, imageResId: Int? = null, customImage: String? = null) {
        viewModelScope.launch {
            try {
                // Create goal with specific position
                createGoalUseCase(text, imageResId, customImage, position)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to create goal at position: ${e.message}")
            }
        }
    }

    /**
     * Reorder goal to a new position (legacy method for immediate saves)
     */
    fun reorderGoal(goal: io.sukhuat.dingo.domain.model.Goal, newPosition: Int) {
        viewModelScope.launch {
            try {
                reorderGoalsUseCase.moveGoalToPosition(goal.id, newPosition)
                    .onFailure { error ->
                        _uiState.value = HomeUiState.Error("Failed to reorder goal: ${error.message}")
                    }
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error("Failed to reorder goal: ${e.message}")
            }
        }
    }

    /**
     * Enhanced drag operation tracking - accumulate drag operations without immediate save
     */
    fun updateDragPosition(goalId: String, newPosition: Int) {
        println("🔄 DRAG_STATE: updateDragPosition - goalId=$goalId, newPosition=$newPosition")

        // Only track if in drag session
        if (_dragSessionActive.value) {
            _dragOperations.value = _dragOperations.value.plus(goalId to newPosition)
            println("🔄 DRAG_STATE: dragOperations updated = ${_dragOperations.value}")
        } else {
            println("⚠️ DRAG_STATE: ignoring update - drag session not active")
        }
    }

    /**
     * Start drag session - begins accumulating drag operations
     */
    fun startDragSession() {
        println("🎯 DRAG_STATE: startDragSession")
        _dragSessionActive.value = true
        _dragOperations.value = emptyMap()
        println("🎯 DRAG_STATE: drag session started, operations cleared")
    }

    /**
     * End drag session - creates snapshot and prepares for save
     */
    fun endDragSession() {
        println("🎯 DRAG_STATE: endDragSession")
        if (_dragSessionActive.value) {
            _dragSnapshot.value = _dragOperations.value.toMap()
            _dragSessionActive.value = false
            println("🎯 DRAG_STATE: snapshot created = ${_dragSnapshot.value}")
        } else {
            println("⚠️ DRAG_STATE: endDragSession called but session not active")
        }
    }

    /**
     * Get current drag state for a goal
     */
    fun getDragPosition(goalId: String): Int? {
        return _dragOperations.value[goalId]
    }

    /**
     * Check if there are pending drag operations to save
     */
    fun hasPendingDragOperations(): Boolean {
        return _dragSnapshot.value.isNotEmpty()
    }

    /**
     * Clear all drag operations (for cancellation)
     */
    fun clearDragOperations() {
        println("🧹 DRAG_STATE: clearDragOperations")
        _dragOperations.value = emptyMap()
        _dragSnapshot.value = emptyMap()
        _dragSessionActive.value = false
        println("🧹 DRAG_STATE: all drag state cleared")
    }

    /**
     * Toggle drag mode on/off with enhanced snapshot-based batch save logic
     */
    fun toggleDragMode() {
        val currentState = _isDragModeActive.value
        println("🔥 DRAG_DEBUG: ViewModel.toggleDragMode() called")
        println("🔥 DRAG_DEBUG: Current drag mode state: $currentState")

        if (currentState) {
            // Exiting drag mode -> End session and save positions
            println("🔥 DRAG_DEBUG: Exiting drag mode")
            endDragSession()
            exitDragModeAndSave()
        } else {
            // Entering drag mode -> Enable and start session
            println("🔥 DRAG_DEBUG: Entering drag mode")
            _isDragModeActive.value = true
            startDragSession()
            println("🔥 DRAG_DEBUG: isDragModeActive set to: ${_isDragModeActive.value}")
        }
    }

    /**
     * Exit drag mode and batch save all goal positions using snapshot-based approach
     */
    private fun exitDragModeAndSave() {
        viewModelScope.launch {
            try {
                println("💾 SAVE_DEBUG: exitDragModeAndSave started")

                // 1. Disable drag mode immediately for UI responsiveness
                _isDragModeActive.value = false
                println("💾 SAVE_DEBUG: drag mode disabled")

                // 2. Show saving indicator
                _isSavingPositions.value = true

                // 3. Use snapshot for consistent state during save
                val dragSnapshot = _dragSnapshot.value.toMap()
                println("💾 SAVE_DEBUG: using snapshot = $dragSnapshot")

                // 4. Batch update positions if we have drag operations
                if (dragSnapshot.isNotEmpty()) {
                    println("💾 SAVE_DEBUG: processing ${dragSnapshot.size} drag operations")

                    // Validate and apply drag operations
                    val result = saveDragOperations(dragSnapshot)

                    result.fold(
                        onSuccess = {
                            println("💾 SAVE_DEBUG: ✅ save successful")
                            // Clear drag operations after successful save
                            clearDragOperations()

                            // Success feedback
                            _lastPositionSyncTime.value = System.currentTimeMillis()

                            // Haptic feedback for success
                            if (vibrationEnabled.value) {
                                vibrateOnGoalCompleted(intensity = 50, duration = 200)
                            }
                        },
                        onFailure = { error ->
                            println("💾 SAVE_DEBUG: ❌ save failed - ${error.message}")
                            _uiState.value = HomeUiState.Error("Failed to save positions: ${error.message}")
                            // Keep drag operations for potential retry
                        }
                    )
                } else {
                    println("💾 SAVE_DEBUG: no drag operations to save")
                    clearDragOperations()
                }

                // 5. Hide saving indicator
                _isSavingPositions.value = false
            } catch (e: Exception) {
                // 6. Error handling with detailed logging
                println("💾 SAVE_DEBUG: ❌ exception during save - ${e.message}")
                _isSavingPositions.value = false
                _uiState.value = HomeUiState.Error("Failed to save positions: ${e.message}")
                Log.e(TAG, "Error saving goal positions", e)
            }
        }
    }

    /**
     * Save drag operations atomically with proper validation and rollback
     */
    private suspend fun saveDragOperations(operations: Map<String, Int>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            println("💾 SAVE_ATOMIC: starting atomic save of ${operations.size} operations")

            // 1. Validate operations before applying
            val validatedOperations = validateDragOperations(operations)
            if (validatedOperations.isEmpty()) {
                println("💾 SAVE_ATOMIC: no valid operations after validation")
                return@withContext Result.success(Unit)
            }

            // 2. Get current goals to create final position list
            val currentGoals = currentWeekGoals.value
            if (currentGoals.isEmpty()) {
                println("💾 SAVE_ATOMIC: no current goals available")
                return@withContext Result.failure(Exception("No goals available for reordering"))
            }

            // 3. Create final goal positions incorporating drag operations
            val finalPositions = createFinalPositionList(currentGoals, validatedOperations)
            println("💾 SAVE_ATOMIC: final positions = $finalPositions")

            // 4. Apply operations atomically using existing use case
            reorderGoalsUseCase(finalPositions)

            println("💾 SAVE_ATOMIC: ✅ atomic save completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("💾 SAVE_ATOMIC: ❌ atomic save failed - ${e.message}")
            Log.e(TAG, "Failed to save drag operations atomically", e)
            Result.failure(e)
        }
    }

    /**
     * Validate drag operations to ensure they're within valid bounds
     */
    private fun validateDragOperations(operations: Map<String, Int>): Map<String, Int> {
        println("🔍 VALIDATION: validating ${operations.size} operations")

        return operations.filter { (goalId, position) ->
            val isValid = position in 0..11 && goalId.isNotEmpty()
            if (!isValid) {
                println("🔍 VALIDATION: ❌ invalid operation - goalId=$goalId, position=$position")
            }
            isValid
        }.also {
            println("🔍 VALIDATION: ${it.size}/${operations.size} operations valid")
        }
    }

    /**
     * Create final goal ID list with drag operations applied
     */
    private fun createFinalPositionList(goals: List<io.sukhuat.dingo.domain.model.Goal>, operations: Map<String, Int>): List<String> {
        println("📋 POSITION_LIST: creating final positions for ${goals.size} goals with ${operations.size} operations")

        // Create array to hold final positions
        val finalArray = arrayOfNulls<String>(12)

        // First, place goals with drag operations in their new positions
        operations.forEach { (goalId, newPosition) ->
            if (newPosition in 0..11) {
                finalArray[newPosition] = goalId
                println("📋 POSITION_LIST: placed dragged goal $goalId at position $newPosition")
            }
        }

        // Then, place remaining goals in their current positions if available
        goals.filter { it.id !in operations.keys }.forEach { goal ->
            var targetPosition = goal.position.coerceIn(0, 11)

            // If position is occupied by a dragged goal, find next available
            while (targetPosition < 12 && finalArray[targetPosition] != null) {
                targetPosition++
            }

            if (targetPosition < 12) {
                finalArray[targetPosition] = goal.id
                println("📋 POSITION_LIST: placed non-dragged goal ${goal.id} at position $targetPosition")
            }
        }

        // Convert to list, filtering out nulls
        return finalArray.filterNotNull().also {
            println("📋 POSITION_LIST: final list = $it")
        }
    }

    /**
     * Check if goal completion should be disabled (drag mode active)
     */
    fun isGoalCompletionDisabled(): Boolean = _isDragModeActive.value
}
