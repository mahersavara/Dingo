package io.sukhuat.dingo.data.repository

import io.sukhuat.dingo.data.model.DummyData
import kotlinx.coroutines.flow.Flow

interface DummyRepository {
    fun getAllDummyData(): Flow<List<DummyData>>
    suspend fun insertDummyData(data: DummyData)
}
