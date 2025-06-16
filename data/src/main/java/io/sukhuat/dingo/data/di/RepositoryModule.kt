package io.sukhuat.dingo.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.remote.FirebaseGoalService
import io.sukhuat.dingo.data.repository.GoalRepositoryImpl
import io.sukhuat.dingo.data.repository.StorageRepositoryImpl
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.domain.repository.StorageRepository
import javax.inject.Singleton

/**
 * Module that provides all repository dependencies except auth repositories
 * (which are provided by AuthModule)
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindStorageRepository(
        storageRepositoryImpl: StorageRepositoryImpl
    ): StorageRepository
    
    companion object {
        /**
         * Provides the GoalRepository implementation
         * Note: Using @Provides instead of @Binds because we need to manually instantiate
         * the repository now that we've removed the GoalDao dependency
         */
        @Provides
        @Singleton
        fun provideGoalRepository(
            firebaseGoalService: FirebaseGoalService
        ): GoalRepository {
            return GoalRepositoryImpl(firebaseGoalService)
        }
    }
}
