package io.sukhuat.dingo.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dummy_data")
data class DummyData(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
