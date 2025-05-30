package io.sukhuat.dingo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import io.sukhuat.dingo.data.model.DummyData
import javax.inject.Singleton

@Singleton
@Database(
    entities = [DummyData::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dummyDao(): DummyDao
}
