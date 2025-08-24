package io.sukhuat.dingo.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.sukhuat.dingo.data.mapper.YearPlannerMapper
import io.sukhuat.dingo.data.mapper.YearPlannerMapper.toDomain
import io.sukhuat.dingo.data.mapper.YearPlannerMapper.toFirebase
import io.sukhuat.dingo.data.model.FirebaseYearPlan
import io.sukhuat.dingo.domain.model.yearplanner.YearPlan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "FirebaseYearPlannerService"
private const val COLLECTION_YEAR_PLANNERS = "yearplanners"

/**
 * Service class for interacting with Firebase Firestore for Year Planner data
 * Follows the existing FirebaseGoalService pattern
 */
@Singleton
class FirebaseYearPlannerService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    /**
     * Get the current user ID or throw an exception if not logged in
     */
    private fun getCurrentUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User is not authenticated")
    }
    
    /**
     * Get a reference to the user's year planners collection
     */
    private fun getUserYearPlannersCollection() = firestore.collection("users")
        .document(getCurrentUserId())
        .collection(COLLECTION_YEAR_PLANNERS)
    
    /**
     * Get year plan for specific year as a Flow
     */
    fun getYearPlan(year: Int): Flow<YearPlan?> = callbackFlow {
        val listenerRegistration = getUserYearPlannersCollection()
            .document(year.toString())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting year plan for year $year", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val yearPlan = snapshot?.toObject(FirebaseYearPlan::class.java)?.toDomain()
                trySend(yearPlan)
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Get all available years that have year plans
     */
    fun getAllYears(): Flow<List<Int>> = callbackFlow {
        val listenerRegistration = getUserYearPlannersCollection()
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error getting all years", error)
                    close(error)
                    return@addSnapshotListener
                }
                
                val years = snapshot?.documents?.mapNotNull { doc ->
                    doc.id.toIntOrNull()
                } ?: emptyList()
                
                trySend(years.sorted())
            }
        
        awaitClose { listenerRegistration.remove() }
    }
    
    /**
     * Save complete year plan to Firebase
     */
    suspend fun saveYearPlan(yearPlan: YearPlan): Boolean {
        return try {
            val firebaseYearPlan = yearPlan.toFirebase()
            getUserYearPlannersCollection()
                .document(yearPlan.year.toString())
                .set(firebaseYearPlan)
                .await()
            
            Log.d(TAG, "Year plan saved successfully for year ${yearPlan.year}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving year plan for year ${yearPlan.year}", e)
            false
        }
    }
    
    /**
     * Update content for a specific month
     */
    suspend fun updateMonthContent(year: Int, monthIndex: Int, content: String): Boolean {
        return try {
            val docRef = getUserYearPlannersCollection().document(year.toString())
            val currentTime = System.currentTimeMillis()
            
            // Calculate word count
            val wordCount = if (content.isBlank()) 0 else {
                content.trim()
                    .split("\\s+".toRegex())
                    .filter { it.isNotBlank() }
                    .size
            }
            
            // Update specific month fields
            val updates = mapOf(
                "months.$monthIndex.content" to content,
                "months.$monthIndex.lastModified" to currentTime,
                "months.$monthIndex.wordCount" to wordCount,
                "updatedAt" to currentTime
            )
            
            docRef.update(updates).await()
            Log.d(TAG, "Month $monthIndex content updated for year $year")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error updating month $monthIndex for year $year", e)
            false
        }
    }
    
    /**
     * Delete year plan completely
     */
    suspend fun deleteYearPlan(year: Int): Boolean {
        return try {
            getUserYearPlannersCollection()
                .document(year.toString())
                .delete()
                .await()
            
            Log.d(TAG, "Year plan deleted successfully for year $year")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting year plan for year $year", e)
            false
        }
    }
    
    /**
     * Check if year plan exists
     */
    suspend fun yearPlanExists(year: Int): Boolean {
        return try {
            val snapshot = getUserYearPlannersCollection()
                .document(year.toString())
                .get()
                .await()
            
            snapshot.exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if year plan exists for year $year", e)
            false
        }
    }
    
    /**
     * Create empty year plan if it doesn't exist
     */
    suspend fun createEmptyYearPlanIfNotExists(year: Int): Boolean {
        return try {
            val userId = getCurrentUserId()
            val docRef = getUserYearPlannersCollection().document(year.toString())
            
            // Check if document exists
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                Log.d(TAG, "Year plan already exists for year $year")
                return true
            }
            
            // Create empty year plan
            val emptyYearPlan = YearPlannerMapper.createEmptyFirebaseYearPlan(year, userId)
            docRef.set(emptyYearPlan).await()
            
            Log.d(TAG, "Empty year plan created for year $year")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating empty year plan for year $year", e)
            false
        }
    }
}