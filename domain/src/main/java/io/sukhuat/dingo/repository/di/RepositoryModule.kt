package io.sukhuat.dingo.repository.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.local.DummyDao
import io.sukhuat.dingo.data.repository.DummyRepository
import io.sukhuat.dingo.data.repository.DummyRepositoryImpl
import javax.inject.Singleton

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
