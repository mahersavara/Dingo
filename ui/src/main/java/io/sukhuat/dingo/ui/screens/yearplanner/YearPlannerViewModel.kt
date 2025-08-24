package io.sukhuat.dingo.ui.screens.yearplanner

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sukhuat.dingo.domain.model.yearplanner.YearPlan
import io.sukhuat.dingo.domain.usecase.yearplanner.GetAllYearsUseCase
import io.sukhuat.dingo.domain.usecase.yearplanner.LoadYearPlanUseCase
import io.sukhuat.dingo.domain.usecase.yearplanner.SaveMonthContentUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

private const val TAG = "YearPlannerViewModel"
private const val AUTO_SAVE_DELAY = 800L // PRD requirement: 800ms debounce

/**
 * ViewModel for Year Planner Screen
 * Handles state management, auto-save, and year navigation
 * Follows existing HomeViewModel patterns in the project
 */
@HiltViewModel
class YearPlannerViewModel @Inject constructor(
    private val loadYearPlanUseCase: LoadYearPlanUseCase,
    private val saveMonthContentUseCase: SaveMonthContentUseCase,
    private val getAllYearsUseCase: GetAllYearsUseCase
) : ViewModel() {
    
    // UI State
    private val _uiState = MutableStateFlow<YearPlannerUiState>(YearPlannerUiState.Loading)
    val uiState: StateFlow<YearPlannerUiState> = _uiState.asStateFlow()
    
    // Current year
    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val currentYear: StateFlow<Int> = _currentYear.asStateFlow()
    
    // Available years
    val availableYears: StateFlow<List<Int>> = getAllYearsUseCase()
        .catch { error ->
            Log.e(TAG, "Error loading available years", error)
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )
    
    // Current year plan data
    val currentYearPlan: StateFlow<YearPlan?> = _currentYear
        .flatMapLatest { year ->
            loadYearPlanUseCase(year)
                .catch { error ->
                    Log.e(TAG, "Error loading year plan for year $year", error)
                    _uiState.value = YearPlannerUiState.Error(
                        "Failed to load year plan: ${error.message ?: "Unknown error"}"
                    )
                    // Don't emit anything on error - keep the previous value
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null
        )
    
    // Auto-save jobs for different months
    private val autoSaveJobs = mutableMapOf<Int, Job>()
    
    init {
        // Observe year plan changes and update UI state
        viewModelScope.launch {
            currentYearPlan.collect { yearPlan ->
                yearPlan?.let { 
                    _uiState.value = YearPlannerUiState.Success(it)
                }
                // If yearPlan is null and we're in loading state, keep loading
                // Error state is set in the flatMapLatest catch block above
            }
        }
    }
    
    /**
     * Load a specific year
     */
    fun loadYear(year: Int) {
        if (year != _currentYear.value) {
            _uiState.value = YearPlannerUiState.Loading
            _currentYear.value = year
            Log.d(TAG, "Loading year plan for year $year")
        }
    }
    
    /**
     * Navigate to previous year
     */
    fun navigateToPreviousYear() {
        val previousYear = _currentYear.value - 1
        loadYear(previousYear)
        Log.d(TAG, "Navigated to previous year: $previousYear")
    }
    
    /**
     * Navigate to next year
     */
    fun navigateToNextYear() {
        val nextYear = _currentYear.value + 1
        loadYear(nextYear)
        Log.d(TAG, "Navigated to next year: $nextYear")
    }
    
    /**
     * Update month content with auto-save debouncing
     * PRD requirement: 800ms debounce before saving
     */
    fun updateMonthContent(monthIndex: Int, content: String) {
        // Cancel existing auto-save job for this month
        autoSaveJobs[monthIndex]?.cancel()
        
        // Update UI state immediately for responsive feeling
        val currentState = _uiState.value
        if (currentState is YearPlannerUiState.Success) {
            val updatedYearPlan = currentState.yearPlan.updateMonth(monthIndex, content)
            _uiState.value = YearPlannerUiState.Success(updatedYearPlan)
        }
        
        // Schedule auto-save with debouncing
        autoSaveJobs[monthIndex] = viewModelScope.launch {
            delay(AUTO_SAVE_DELAY)
            
            try {
                val result = saveMonthContentUseCase(
                    year = _currentYear.value,
                    monthIndex = monthIndex,
                    content = content
                )
                
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "Successfully saved month $monthIndex content for year ${_currentYear.value}")
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error saving month $monthIndex content", error)
                        // TODO: Show error message to user
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception during auto-save for month $monthIndex", e)
            } finally {
                // Clean up job reference
                autoSaveJobs.remove(monthIndex)
            }
        }
        
        Log.d(TAG, "Scheduled auto-save for month $monthIndex in ${AUTO_SAVE_DELAY}ms")
    }
    
    /**
     * Force save all pending changes
     */
    fun forceSaveAll() {
        viewModelScope.launch {
            // Wait for any pending auto-saves to complete
            autoSaveJobs.values.forEach { job ->
                job.join()
            }
            Log.d(TAG, "All pending saves completed")
        }
    }
    
    /**
     * Refresh current year plan
     */
    fun refresh() {
        val currentYear = _currentYear.value
        _uiState.value = YearPlannerUiState.Loading
        loadYear(currentYear)
        Log.d(TAG, "Refreshing year plan for year $currentYear")
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cancel all pending auto-save jobs
        autoSaveJobs.values.forEach { it.cancel() }
        autoSaveJobs.clear()
        Log.d(TAG, "ViewModel cleared, cancelled all auto-save jobs")
    }
}

/**
 * UI State for Year Planner Screen
 * Follows existing UI state patterns in the project
 */
sealed class YearPlannerUiState {
    /**
     * Loading state - fetching data
     */
    object Loading : YearPlannerUiState()
    
    /**
     * Success state - data loaded successfully
     */
    data class Success(val yearPlan: YearPlan) : YearPlannerUiState()
    
    /**
     * Error state - failed to load data
     */
    data class Error(val message: String) : YearPlannerUiState()
}