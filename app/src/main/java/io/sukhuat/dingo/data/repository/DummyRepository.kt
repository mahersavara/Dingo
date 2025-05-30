package io.sukhuat.dingo.data.repository

import io.sukhuat.dingo.data.model.DummyData

interface DummyRepository {
    suspend fun getDummyData(): List<DummyData>
}
