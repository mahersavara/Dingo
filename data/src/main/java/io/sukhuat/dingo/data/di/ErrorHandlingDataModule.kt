package io.sukhuat.dingo.data.di

import com.google.gson.Gson
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.cache.ProfileCacheManagerImpl
import io.sukhuat.dingo.data.network.NetworkConnectivityCheckerImpl
import io.sukhuat.dingo.domain.model.NetworkConnectivityChecker
import io.sukhuat.dingo.domain.model.ProfileCacheManager
import javax.inject.Singleton

/**
 * Dependency injection module for data layer error handling implementations
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class ErrorHandlingDataModule {

    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityChecker(
        networkConnectivityCheckerImpl: NetworkConnectivityCheckerImpl
    ): NetworkConnectivityChecker

    @Binds
    @Singleton
    abstract fun bindProfileCacheManager(
        profileCacheManagerImpl: ProfileCacheManagerImpl
    ): ProfileCacheManager

    companion object {
        @Provides
        @Singleton
        fun provideGson(): Gson {
            return Gson()
        }
    }
}
