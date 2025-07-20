package io.sukhuat.dingo.data.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.notification.NotificationScheduler
import io.sukhuat.dingo.data.notification.NotificationService
import io.sukhuat.dingo.data.preferences.UserPreferencesDataStore
import io.sukhuat.dingo.data.remote.FirebaseGoalService
import io.sukhuat.dingo.data.repository.GoalRepositoryImpl
import io.sukhuat.dingo.data.repository.ProfileStatisticsRepositoryImpl
import io.sukhuat.dingo.data.repository.SharingRepositoryImpl
import io.sukhuat.dingo.data.repository.StorageRepositoryImpl
import io.sukhuat.dingo.data.repository.UserPreferencesRepositoryImpl
import io.sukhuat.dingo.data.repository.UserProfileRepositoryImpl
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import io.sukhuat.dingo.domain.repository.SharingRepository
import io.sukhuat.dingo.domain.repository.StorageRepository
import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import io.sukhuat.dingo.domain.repository.UserProfileRepository
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

    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        userPreferencesRepositoryImpl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository

    @Binds
    @Singleton
    abstract fun bindProfileStatisticsRepository(
        profileStatisticsRepositoryImpl: ProfileStatisticsRepositoryImpl
    ): ProfileStatisticsRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindSharingRepository(
        sharingRepositoryImpl: SharingRepositoryImpl
    ): SharingRepository

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

        /**
         * Provides the UserPreferencesDataStore
         */
        @Provides
        @Singleton
        fun provideUserPreferencesDataStore(
            @ApplicationContext context: Context
        ): UserPreferencesDataStore {
            return UserPreferencesDataStore(context)
        }

        /**
         * Provides the NotificationService
         */
        @Provides
        @Singleton
        fun provideNotificationService(
            @ApplicationContext context: Context,
            userPreferencesDataStore: UserPreferencesDataStore
        ): NotificationService {
            return NotificationService(context, userPreferencesDataStore)
        }

        /**
         * Provides the NotificationScheduler
         */
        @Provides
        @Singleton
        fun provideNotificationScheduler(
            @ApplicationContext context: Context
        ): NotificationScheduler {
            return NotificationScheduler(context)
        }
    }
}
