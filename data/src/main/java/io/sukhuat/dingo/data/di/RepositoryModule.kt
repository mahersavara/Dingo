package io.sukhuat.dingo.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.local.DummyDao
import io.sukhuat.dingo.data.repository.DummyRepository
import io.sukhuat.dingo.data.repository.DummyRepositoryImpl
import javax.inject.Singleton

/**
 * Module that provides all repository dependencies except auth repositories
 * (which are provided by AuthModule)
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideDummyRepository(
        dummyDao: DummyDao
    ): DummyRepository {
        return DummyRepositoryImpl(dummyDao)
    }
}
