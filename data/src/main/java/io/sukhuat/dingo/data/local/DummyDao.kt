package io.sukhuat.dingo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.sukhuat.dingo.data.model.DummyData
import kotlinx.coroutines.flow.Flow

@Dao
interface DummyDao {
    @Query("SELECT * FROM dummy_data")
    fun getAllDummyData(): Flow<List<DummyData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDummyData(data: DummyData)
}
