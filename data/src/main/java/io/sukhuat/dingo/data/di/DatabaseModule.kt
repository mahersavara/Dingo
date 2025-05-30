package io.sukhuat.dingo.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.local.AppDatabase
import io.sukhuat.dingo.data.local.DummyDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dingo_database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideDummyDao(database: AppDatabase): DummyDao {
        return database.dummyDao()
    }
}
