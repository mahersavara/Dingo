package io.sukhuat.dingo.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.repository.GoalRepositoryImpl
import io.sukhuat.dingo.data.sync.SyncManager
import io.sukhuat.dingo.data.util.NetworkConnectivityObserver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ): NetworkConnectivityObserver {
        return NetworkConnectivityObserver(context)
    }

    @Provides
    @Singleton
    fun provideSyncManager(
        goalRepository: GoalRepositoryImpl,
        networkConnectivityObserver: NetworkConnectivityObserver
    ): SyncManager {
        return SyncManager(goalRepository, networkConnectivityObserver)
    }
}
