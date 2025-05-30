package io.sukhuat.dingo.repository

import io.sukhuat.dingo.data.local.DummyDao
import io.sukhuat.dingo.data.model.DummyData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DummyRepositoryImpl @Inject constructor(
    private val dummyDao: DummyDao
) : DummyRepository {
    override fun getAllDummyData(): Flow<List<DummyData>> {
        return dummyDao.getAllDummyData()
    }

    override suspend fun insertDummyData(data: DummyData) {
        dummyDao.insertDummyData(data)
    }
}
