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
     * Get the current user ID or return null if not logged in
     */
    private fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Get a reference to the user's goals collection
     * Returns null if user is not authenticated
     */
    private fun getUserGoalsCollection() = getCurrentUserId()?.let { userId ->
        firestore.collection("users")
            .document(userId)
            .collection(goalsCollection)
    }

    /**
     * Get all goals as a Flow
     */
    fun getAllGoals(): Flow<List<Goal>> = callbackFlow {
        try {
            android.util.Log.d("FirebaseGoalService", "=== getAllGoals() - Setting up listener ===")

            // Check if user is authenticated before proceeding
            val goalsCollection = getUserGoalsCollection()
            if (goalsCollection == null) {
                android.util.Log.w("FirebaseGoalService", "User not authenticated, returning empty goals list")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            android.util.Log.d("FirebaseGoalService", "User authenticated, setting up Firestore listener...")

            // CRITICAL FIX: Send empty list immediately to unblock HomeViewModel.loadGoals()
            // This ensures the Flow emits at least once, preventing infinite waiting
            android.util.Log.d("FirebaseGoalService", "🚀 Sending initial empty goals to unblock UI...")
            trySend(emptyList())

            val listenerRegistration = goalsCollection
                .orderBy("position", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    android.util.Log.d("FirebaseGoalService", "📡 Firestore listener callback triggered")

                    if (error != null) {
                        android.util.Log.e("FirebaseGoalService", "Error in goals listener", error)
                        trySend(emptyList()) // Send empty list instead of closing the flow
                        return@addSnapshotListener
                    }

                    android.util.Log.d("FirebaseGoalService", "Processing snapshot with ${snapshot?.documents?.size ?: 0} documents")

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
                            android.util.Log.w("FirebaseGoalService", "Error parsing goal document", e)
                            null
                        }
                    } ?: emptyList()

                    android.util.Log.d("FirebaseGoalService", "✅ Sending ${goals.size} goals to Flow")
                    trySend(goals)
                }

            awaitClose {
                android.util.Log.d("FirebaseGoalService", "🔚 Removing Firestore listener")
                listenerRegistration.remove()
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseGoalService", "Error setting up goals listener", e)
            trySend(emptyList())
            awaitClose { }
        }
    }

    /**
     * Get goals by status
     */
    fun getGoalsByStatus(status: GoalStatus): Flow<List<Goal>> = callbackFlow {
        try {
            // Check if user is authenticated before proceeding
            val goalsCollection = getUserGoalsCollection()
            if (goalsCollection == null) {
                android.util.Log.w("FirebaseGoalService", "User not authenticated, returning empty goals list for status ${status.name}")
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }

            val listenerRegistration = goalsCollection
                .whereEqualTo("status", status.name)
                .orderBy("position", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirebaseGoalService", "Error in goals by status listener", error)
                        trySend(emptyList())
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
                            android.util.Log.w("FirebaseGoalService", "Error parsing goal document", e)
                            null
                        }
                    } ?: emptyList()

                    trySend(goals)
                }

            awaitClose { listenerRegistration.remove() }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseGoalService", "Error setting up goals by status listener", e)
            trySend(emptyList())
            awaitClose { }
        }
    }

    /**
     * Get all goals synchronously for widget use
     * This bypasses the Flow mechanism to avoid the "handler already registered" issue
     */
    suspend fun getAllGoalsSync(): List<Goal> {
        return try {
            android.util.Log.d("FirebaseGoalService", "📦 getAllGoalsSync() - Direct Firestore query")

            // Check if user is authenticated
            val goalsCollection = getUserGoalsCollection()
            if (goalsCollection == null) {
                android.util.Log.w("FirebaseGoalService", "User not authenticated, returning empty goals list")
                return emptyList()
            }

            // Direct query without Flow
            val snapshot = goalsCollection
                .orderBy("position", Query.Direction.ASCENDING)
                .get()
                .await()

            val goals = snapshot.documents.mapNotNull { document ->
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
                    android.util.Log.w("FirebaseGoalService", "Error parsing goal document", e)
                    null
                }
            }

            android.util.Log.d("FirebaseGoalService", "✅ getAllGoalsSync() returning ${goals.size} goals")
            goals
        } catch (e: Exception) {
            android.util.Log.e("FirebaseGoalService", "❌ Error in getAllGoalsSync()", e)
            emptyList()
        }
    }

    /**
     * Get a specific goal by ID
     */
    fun getGoalById(id: String): Flow<Goal?> = callbackFlow {
        try {
            // Check if user is authenticated before proceeding
            val goalsCollection = getUserGoalsCollection()
            if (goalsCollection == null) {
                android.util.Log.w("FirebaseGoalService", "User not authenticated, returning null for goal $id")
                trySend(null)
                awaitClose { }
                return@callbackFlow
            }

            val listenerRegistration = goalsCollection
                .document(id)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("FirebaseGoalService", "Error in goal by id listener", error)
                        trySend(null)
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
                        android.util.Log.w("FirebaseGoalService", "Error parsing goal by id document", e)
                        null
                    }

                    trySend(goal)
                }

            awaitClose { listenerRegistration.remove() }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseGoalService", "Error setting up goal by id listener", e)
            trySend(null)
            awaitClose { }
        }
    }

    /**
     * Create a new goal
     */
    suspend fun createGoal(goal: Goal): String {
        val goalsCollection = getUserGoalsCollection()
            ?: throw IllegalStateException("User is not authenticated")

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
            goalsCollection.document(goal.id).set(goalData).await()
            goalsCollection.document(goal.id)
        } else {
            goalsCollection.add(goalData).await()
        }

        return documentRef.id
    }

    /**
     * Update an existing goal
     */
    suspend fun updateGoal(goal: Goal): Boolean {
        val goalsCollection = getUserGoalsCollection() ?: return false

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
            goalsCollection.document(goal.id).update(goalData.toMap()).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the status of a goal
     */
    suspend fun updateGoalStatus(goalId: String, status: GoalStatus): Boolean {
        val goalsCollection = getUserGoalsCollection() ?: return false

        return try {
            goalsCollection.document(goalId).update("status", status.name).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the text of a goal
     */
    suspend fun updateGoalText(goalId: String, text: String): Boolean {
        val goalsCollection = getUserGoalsCollection() ?: return false

        return try {
            goalsCollection.document(goalId).update("text", text).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the custom image of a goal
     */
    suspend fun updateGoalImage(goalId: String, customImage: String?): Boolean {
        val goalsCollection = getUserGoalsCollection() ?: return false

        return try {
            goalsCollection.document(goalId).update("customImage", customImage).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the image URL of a goal
     */
    suspend fun updateGoalImageUrl(goalId: String, imageUrl: String?): Boolean {
        val goalsCollection = getUserGoalsCollection() ?: return false

        return try {
            goalsCollection.document(goalId).update("imageUrl", imageUrl).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete a goal
     */
    suspend fun deleteGoal(goalId: String): Boolean {
        val goalsCollection = getUserGoalsCollection() ?: return false

        return try {
            goalsCollection.document(goalId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Update the position of a goal
     */
    suspend fun updateGoalPosition(goalId: String, position: Int): Boolean {
        val goalsCollection = getUserGoalsCollection() ?: return false

        return try {
            goalsCollection.document(goalId).update("position", position).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Batch update positions for reordering
     */
    suspend fun reorderGoals(goalIds: List<String>): Boolean {
        val goalsCollection = getUserGoalsCollection() ?: return false
        val batch = firestore.batch()

        goalIds.forEachIndexed { index, id ->
            val docRef = goalsCollection.document(id)
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
