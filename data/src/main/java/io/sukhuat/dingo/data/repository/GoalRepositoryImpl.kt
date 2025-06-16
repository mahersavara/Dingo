package io.sukhuat.dingo.data.repository

import android.util.Log
import io.sukhuat.dingo.data.remote.FirebaseGoalService
import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus
import io.sukhuat.dingo.domain.repository.GoalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoalRepositoryImpl"

/**
 * Implementation of GoalRepository that uses Firebase Firestore as the main database
 * This implementation has been updated to use Firebase as the source of truth
 * instead of the previous offline-first approach with Room
 */
@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val firebaseGoalService: FirebaseGoalService
) : GoalRepository {
    
    // Coroutine scope for background operations
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun getAllGoals(): Flow<List<Goal>> {
        // Return goals directly from Firebase
        return firebaseGoalService.getAllGoals().catch { error ->
            Log.e(TAG, "Error getting all goals", error)
            emit(emptyList())
        }
    }
    
    override fun getGoalsByStatus(status: GoalStatus): Flow<List<Goal>> {
        // Return goals with specific status from Firebase
        return firebaseGoalService.getGoalsByStatus(status).catch { error ->
            Log.e(TAG, "Error getting goals by status", error)
            emit(emptyList())
        }
    }
    
    override fun getGoalById(id: String): Flow<Goal?> {
        // Return specific goal from Firebase
        return firebaseGoalService.getGoalById(id).catch { error ->
            Log.e(TAG, "Error getting goal by id", error)
            emit(null)
        }
    }
    
    override suspend fun createGoal(goal: Goal): String {
        try {
            // Assign position if not set
            val newGoal = if (goal.position == -1) {
                // Get the count from Firebase
                val goals = getAllGoals().first()
                val count = goals.size
                goal.copy(position = count)
            } else {
                goal
            }
            
            // Save directly to Firebase
            return firebaseGoalService.createGoal(newGoal)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating goal", e)
            throw e
        }
    }
    
    override suspend fun updateGoal(goal: Goal): Boolean {
        try {
            // Update directly in Firebase
            firebaseGoalService.updateGoal(goal)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal", e)
            return false
        }
    }
    
    override suspend fun updateGoalStatus(goalId: String, status: GoalStatus): Boolean {
        try {
            // Update directly in Firebase
            firebaseGoalService.updateGoalStatus(goalId, status)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal status", e)
            return false
        }
    }
    
    override suspend fun updateGoalText(goalId: String, text: String): Boolean {
        try {
            // Update directly in Firebase
            firebaseGoalService.updateGoalText(goalId, text)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal text", e)
            return false
        }
    }
    
    override suspend fun updateGoalImage(goalId: String, customImage: String?): Boolean {
        try {
            // Update directly in Firebase
            firebaseGoalService.updateGoalImage(goalId, customImage)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal image", e)
            return false
        }
    }
    
    override suspend fun updateGoalImageUrl(goalId: String, imageUrl: String?): Boolean {
        try {
            // Update directly in Firebase
            firebaseGoalService.updateGoalImageUrl(goalId, imageUrl)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating goal imageUrl", e)
            return false
        }
    }
    
    override suspend fun deleteGoal(goalId: String): Boolean {
        try {
            // Delete directly from Firebase
            firebaseGoalService.deleteGoal(goalId)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting goal", e)
            return false
        }
    }
    
    override suspend fun reorderGoals(goalIds: List<String>): Boolean {
        try {
            // Update directly in Firebase
            firebaseGoalService.reorderGoals(goalIds)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error reordering goals", e)
            return false
        }
    }
    
    /**
     * Clears all goals from the local database
     * This should be called when a user logs out or switches accounts
     * With Firebase, we don't need to clear anything locally as data is user-scoped
     */
    override suspend fun clearAllGoals() {
        // No need to clear anything as we're using Firebase directly
        // Firebase handles user scoping of data automatically
        Log.d(TAG, "clearAllGoals called - no action needed with Firebase implementation")
    }
} 