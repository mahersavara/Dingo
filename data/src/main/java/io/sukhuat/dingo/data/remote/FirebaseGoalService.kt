package io.sukhuat.dingo.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.sukhuat.dingo.data.model.GoalEntity
import io.sukhuat.dingo.domain.model.Goal
import io.sukhuat.dingo.domain.model.GoalStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service class for interacting with Firebase Firestore for goals
 */
@Singleton
class FirebaseGoalService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val goalsCollection = "goals"

    /**
     * Get the current user ID or throw an exception if not logged in
     */
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User is not authenticated")
    }

    /**
     * Get a reference to the user's goals collection
     */
    private fun getUserGoalsCollection() = firestore.collection("users")
        .document(getCurrentUserId())
        .collection(goalsCollection)

    /**
     * Get all goals as a Flow
     */
    fun getAllGoals(): Flow<List<Goal>> = callbackFlow {
        val listenerRegistration = getUserGoalsCollection()
            .orderBy("position", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val id = document.id
                        val text = document.getString("text") ?: ""
                        val imageResId = document.getLong("imageResId")?.toInt()
                        val statusStr = document.getString("status") ?: GoalStatus.ACTIVE.name
                        val status = try {
                            GoalStatus.valueOf(statusStr)
                        } catch (e: Exception) {
                            GoalStatus.ACTIVE
                        }
                        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                        val customImage = document.getString("customImage")
                        val imageUrl = document.getString("imageUrl")
                        val position = document.getLong("position")?.toInt() ?: 0
                        val weekOfYear = document.getLong("weekOfYear")?.toInt()
                        val yearCreated = document.getLong("yearCreated")?.toInt()

                        // Create GoalEntity and convert to domain model to ensure proper week calculation
                        val goalEntity = GoalEntity(
                            id = id,
                            text = text,
                            imageResId = imageResId,
                            status = status.name,
                            createdAt = createdAt,
                            customImage = customImage,
                            imageUrl = imageUrl,
                            position = position,
                            weekOfYear = weekOfYear,
                            yearCreated = yearCreated
                        )
                        goalEntity.toDomainModel()
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Get goals by status
     */
    fun getGoalsByStatus(status: GoalStatus): Flow<List<Goal>> = callbackFlow {
        val listenerRegistration = getUserGoalsCollection()
            .whereEqualTo("status", status.name)
            .orderBy("position", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val goals = snapshot?.documents?.mapNotNull { document ->
                    try {
                        val id = document.id
                        val text = document.getString("text") ?: ""
                        val imageResId = document.getLong("imageResId")?.toInt()
                        val createdAt = document.getLong("createdAt") ?: System.currentTimeMillis()
                        val customImage = document.getString("customImage")
                        val imageUrl = document.getString("imageUrl")
                        val position = document.getLong("position")?.toInt() ?: 0
                        val weekOfYear = document.getLong("weekOfYear")?.toInt()
                        val yearCreated = document.getLong("yearCreated")?.toInt()

                        // Create GoalEntity and convert to domain model to ensure proper week calculation
                        val goalEntity = GoalEntity(
                            id = id,
                            text = text,
                            imageResId = imageResId,
                            status = status.name,
                            createdAt = createdAt,
                            customImage = customImage,
                            imageUrl = imageUrl,
                            position = position,
                            weekOfYear = weekOfYear,
                            yearCreated = yearCreated
                        )
                        goalEntity.toDomainModel()
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(goals)
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Get a specific goal by ID
     */
    fun getGoalById(id: String): Flow<Goal?> = callbackFlow {
        val listenerRegistration = getUserGoalsCollection()
            .document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val goal = try {
                    if (snapshot != null && snapshot.exists()) {
                        val text = snapshot.getString("text") ?: ""
                        val imageResId = snapshot.getLong("imageResId")?.toInt()
                        val statusStr = snapshot.getString("status") ?: GoalStatus.ACTIVE.name
                        val status = try {
                            GoalStatus.valueOf(statusStr)
                        } catch (e: Exception) {
                            GoalStatus.ACTIVE
                        }
                        val createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis()
                        val customImage = snapshot.getString("customImage")
                        val imageUrl = snapshot.getString("imageUrl")
                        val position = snapshot.getLong("position")?.toInt() ?: 0

                        Goal(
                            id = id,
                            text = text,
                            imageResId = imageResId,
                            status = status,
                            createdAt = createdAt,
                            customImage = customImage,
                            imageUrl = imageUrl,
                            position = position
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }

                trySend(goal)
            }

        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Create a new goal
     */
    suspend fun createGoal(goal: Goal): String {
        val goalData = hashMapOf(
            "text" to goal.text,
            "imageResId" to goal.imageResId,
            "status" to goal.status.name,
            "createdAt" to goal.createdAt,
            "customImage" to goal.customImage,
            "imageUrl" to goal.imageUrl,
            "position" to goal.position,
            "weekOfYear" to goal.weekOfYear,
            "yearCreated" to goal.yearCreated
        )

        // Use the goal's ID if it has one, otherwise let Firestore generate an ID
        val documentRef = if (goal.id.isNotEmpty()) {
            getUserGoalsCollection().document(goal.id).set(goalData).await()
            getUserGoalsCollection().document(goal.id)
        } else {
            getUserGoalsCollection().add(goalData).await()
        }

        return documentRef.id
    }

    /**
     * Update an existing goal
     */
    suspend fun updateGoal(goal: Goal): Boolean {
        val goalData = hashMapOf(
            "text" to goal.text,
            "imageResId" to goal.imageResId,
            "status" to goal.status.name,
            "createdAt" to goal.createdAt,
            "customImage" to goal.customImage,
            "imageUrl" to goal.imageUrl,
            "position" to goal.position,
            "weekOfYear" to goal.weekOfYear,
            "yearCreated" to goal.yearCreated
        )

        return try {
            getUserGoalsCollection().document(goal.id).update(goalData.toMap()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the status of a goal
     */
    suspend fun updateGoalStatus(goalId: String, status: GoalStatus): Boolean {
        return try {
            getUserGoalsCollection().document(goalId).update("status", status.name).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the text of a goal
     */
    suspend fun updateGoalText(goalId: String, text: String): Boolean {
        return try {
            getUserGoalsCollection().document(goalId).update("text", text).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the custom image of a goal
     */
    suspend fun updateGoalImage(goalId: String, customImage: String?): Boolean {
        return try {
            getUserGoalsCollection().document(goalId).update("customImage", customImage).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the image URL of a goal
     */
    suspend fun updateGoalImageUrl(goalId: String, imageUrl: String?): Boolean {
        return try {
            getUserGoalsCollection().document(goalId).update("imageUrl", imageUrl).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete a goal
     */
    suspend fun deleteGoal(goalId: String): Boolean {
        return try {
            getUserGoalsCollection().document(goalId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the position of a goal
     */
    suspend fun updateGoalPosition(goalId: String, position: Int): Boolean {
        return try {
            getUserGoalsCollection().document(goalId).update("position", position).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Batch update positions for reordering
     */
    suspend fun reorderGoals(goalIds: List<String>): Boolean {
        val batch = firestore.batch()

        goalIds.forEachIndexed { index, id ->
            val docRef = getUserGoalsCollection().document(id)
            batch.update(docRef, "position", index)
        }

        return try {
            batch.commit().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
