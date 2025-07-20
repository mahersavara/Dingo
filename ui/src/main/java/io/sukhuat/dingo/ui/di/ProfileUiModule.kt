package io.sukhuat.dingo.ui.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.ui.screens.profile.FallbackUiStateManager
import io.sukhuat.dingo.ui.screens.profile.LazyLoadingManager
import io.sukhuat.dingo.ui.screens.profile.ProfilePerformanceMonitor
import javax.inject.Singleton

/**
 * Dependency injection module for profile UI components
 */
@Module
@InstallIn(SingletonComponent::class)
object ProfileUiModule {

    @Provides
    @Singleton
    fun provideFallbackUiStateManager(): FallbackUiStateManager {
        return FallbackUiStateManager()
    }

    @Provides
    @Singleton
    fun provideLazyLoadingManager(): LazyLoadingManager {
        return LazyLoadingManager()
    }

    @Provides
    @Singleton
    fun provideProfilePerformanceMonitor(): ProfilePerformanceMonitor {
        return ProfilePerformanceMonitor()
    }
}
