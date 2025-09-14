package io.sukhuat.dingo.data.repository

import android.util.Log
import io.sukhuat.dingo.data.remote.FirebaseGoalService
import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.domain.service.WidgetNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    private val firebaseGoalService: FirebaseGoalService,
    private val widgetNotificationService: WidgetNotificationService
) : GoalRepository {

    // Coroutine scope for background operations
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        Log.d(TAG, "🏗️ GoalRepositoryImpl initialized with widgetNotificationService: $widgetNotificationService")
    }

    override fun getAllGoals(): Flow<List<Goal>> {
        // Return goals directly from Firebase
        return firebaseGoalService.getAllGoals().catch { error ->
            Log.e(TAG, "Error getting all goals", error)
            emit(emptyList())
        }
    }

    override suspend fun getAllGoalsSync(): List<Goal> {
        // Return goals directly from Firebase using sync method
        return try {
            firebaseGoalService.getAllGoalsSync()
        } catch (error: Exception) {
            Log.e(TAG, "Error getting all goals sync", error)
            emptyList()
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
            val goalId = firebaseGoalService.createGoal(newGoal)

            // Notify widgets about goal creation
            try {
                Log.d(TAG, "📱 About to notify widgets about goal creation, service: $widgetNotificationService")
                widgetNotificationService.notifyGoalCreated(goalId)
                Log.d(TAG, "📱 Widget notification sent for goal creation: $goalId")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal creation", e)
            }

            return goalId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating goal", e)
            throw e
        }
    }

    override suspend fun updateGoal(goal: Goal): Boolean {
        try {
            // Update directly in Firebase
            firebaseGoalService.updateGoal(goal)

            // Notify widgets about goal update
            try {
                Log.d(TAG, "📱 About to notify widgets about goal update, service: $widgetNotificationService")
                widgetNotificationService.notifyGoalUpdated(goal.id)
                Log.d(TAG, "📱 Widget notification sent for goal update: ${goal.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal update", e)
            }

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

            // Notify widgets about goal status change
            try {
                widgetNotificationService.notifyGoalStatusChanged(goalId, status.name)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal status change", e)
            }

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

            // Notify widgets about goal text update
            try {
                widgetNotificationService.notifyGoalUpdated(goalId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal text update", e)
            }

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

            // Notify widgets about goal image update
            try {
                widgetNotificationService.notifyGoalUpdated(goalId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal image update", e)
            }

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

            // Notify widgets about goal image URL update
            try {
                widgetNotificationService.notifyGoalUpdated(goalId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal image URL update", e)
            }

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

            // Notify widgets about goal deletion
            try {
                widgetNotificationService.notifyGoalDeleted(goalId)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal deletion", e)
            }

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

            // Notify widgets about goal reordering (treat as update)
            try {
                // Notify about the first goal to trigger widget update
                if (goalIds.isNotEmpty()) {
                    widgetNotificationService?.notifyGoalUpdated(goalIds.first())
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal reordering", e)
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error reordering goals", e)
            return false
        }
    }

    override suspend fun moveGoalToPosition(goalId: String, newPosition: Int): Result<Unit> {
        return try {
            require(newPosition in 0..11) { "Grid position must be between 0 and 11" }

            // Get current goals to check for conflicts
            val currentGoals = getAllGoals().first()
            val goalToMove = currentGoals.find { it.id == goalId }
                ?: return Result.failure(Exception("Goal not found"))

            // Check if target position is occupied
            val goalAtTargetPosition = currentGoals.find { it.position == newPosition }

            if (goalAtTargetPosition != null && goalAtTargetPosition.id != goalId) {
                // Swap positions
                val updatedGoalToMove = goalToMove.copy(position = newPosition)
                val updatedGoalAtTarget = goalAtTargetPosition.copy(position = goalToMove.position)

                firebaseGoalService.updateGoal(updatedGoalToMove)
                firebaseGoalService.updateGoal(updatedGoalAtTarget)

                // Notify widgets about goal position changes
                try {
                    widgetNotificationService.notifyGoalUpdated(goalId)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to notify widgets about goal move", e)
                }
            } else {
                // Just move to empty position
                val updatedGoal = goalToMove.copy(position = newPosition)
                firebaseGoalService.updateGoal(updatedGoal)

                // Notify widgets about goal position change
                try {
                    widgetNotificationService.notifyGoalUpdated(goalId)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to notify widgets about goal move", e)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error moving goal to position", e)
            Result.failure(e)
        }
    }

    override suspend fun swapGoalPositions(goalId1: String, goalId2: String): Result<Unit> {
        return try {
            val currentGoals = getAllGoals().first()
            val goal1 = currentGoals.find { it.id == goalId1 }
                ?: return Result.failure(Exception("Goal 1 not found"))
            val goal2 = currentGoals.find { it.id == goalId2 }
                ?: return Result.failure(Exception("Goal 2 not found"))

            // Swap positions
            val updatedGoal1 = goal1.copy(position = goal2.position)
            val updatedGoal2 = goal2.copy(position = goal1.position)

            firebaseGoalService.updateGoal(updatedGoal1)
            firebaseGoalService.updateGoal(updatedGoal2)

            // Notify widgets about goal position swaps
            try {
                widgetNotificationService?.notifyGoalUpdated(goalId1)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to notify widgets about goal swap", e)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error swapping goal positions", e)
            Result.failure(e)
        }
    }

    override fun getGoalsByGridPosition(): Flow<List<Goal>> {
        return firebaseGoalService.getAllGoals().catch { error ->
            Log.e(TAG, "Error getting goals by grid position", error)
            emit(emptyList())
        }.map { goals ->
            // Sort by grid position (0-11), putting goals with invalid positions at the end
            goals.sortedWith(
                compareBy<Goal> {
                    if (it.position in 0..11) it.position else Int.MAX_VALUE
                }.thenBy { it.createdAt }
            )
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
