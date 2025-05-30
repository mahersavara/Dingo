package io.sukhuat.dingo.data.repository

import io.sukhuat.dingo.data.model.DummyData

class DummyRepositoryImpl : DummyRepository {
    override suspend fun getDummyData(): List<DummyData> {
        // This is just a placeholder implementation
        return listOf(
            DummyData(1, "First item"),
            DummyData(2, "Second item")
        )
    }
}
