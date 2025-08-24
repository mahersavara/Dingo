package io.sukhuat.dingo.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import io.sukhuat.dingo.data.remote.FirebaseYearPlannerService
import io.sukhuat.dingo.domain.model.yearplanner.YearPlan
import io.sukhuat.dingo.domain.repository.YearPlannerRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "YearPlannerRepositoryImpl"
private const val PREFERENCES_NAME = "year_planner_prefs"

// DataStore extension
private val Context.yearPlannerDataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME
)

/**
 * Implementation of YearPlannerRepository that uses Firebase Firestore
 * Follows the existing GoalRepositoryImpl pattern with Firebase-first approach
 */
@Singleton
class YearPlannerRepositoryImpl @Inject constructor(
    private val firebaseService: FirebaseYearPlannerService,
    private val context: Context
) : YearPlannerRepository {

    // Coroutine scope for background operations
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // DataStore keys
    private val LAST_ACCESSED_YEAR_KEY = intPreferencesKey("last_accessed_year")

    override fun getYearPlan(year: Int): Flow<YearPlan?> {
        return firebaseService.getYearPlan(year).catch { error ->
            Log.e(TAG, "Error getting year plan for year $year", error)
            emit(null)
        }
    }

    override fun getAllYears(): Flow<List<Int>> {
        return firebaseService.getAllYears().catch { error ->
            Log.e(TAG, "Error getting all years", error)
            emit(emptyList())
        }
    }

    override suspend fun saveYearPlan(yearPlan: YearPlan): Boolean {
        return try {
            val success = firebaseService.saveYearPlan(yearPlan)
            if (success) {
                // Update last accessed year
                updateLastAccessedYear(yearPlan.year)
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error saving year plan for year ${yearPlan.year}", e)
            false
        }
    }

    override suspend fun updateMonthContent(year: Int, monthIndex: Int, content: String): Boolean {
        return try {
            // Ensure year plan exists (create empty if needed)
            val exists = firebaseService.yearPlanExists(year)
            if (!exists) {
                Log.d(TAG, "Creating empty year plan for year $year")
                firebaseService.createEmptyYearPlanIfNotExists(year)
            }

            // Update month content
            val success = firebaseService.updateMonthContent(year, monthIndex, content)
            if (success) {
                // Update last accessed year
                updateLastAccessedYear(year)
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error updating month $monthIndex for year $year", e)
            false
        }
    }

    override suspend fun deleteYearPlan(year: Int): Boolean {
        return try {
            firebaseService.deleteYearPlan(year)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting year plan for year $year", e)
            false
        }
    }

    override suspend fun yearPlanExists(year: Int): Boolean {
        return try {
            firebaseService.yearPlanExists(year)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if year plan exists for year $year", e)
            false
        }
    }

    override suspend fun getLastAccessedYear(): Int? {
        return try {
            context.yearPlannerDataStore.data
                .map { preferences ->
                    preferences[LAST_ACCESSED_YEAR_KEY]
                }
                .first()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting last accessed year", e)
            null
        }
    }

    override suspend fun updateLastAccessedYear(year: Int) {
        try {
            context.yearPlannerDataStore.edit { preferences ->
                preferences[LAST_ACCESSED_YEAR_KEY] = year
            }
            Log.d(TAG, "Updated last accessed year to $year")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating last accessed year", e)
        }
    }
}
