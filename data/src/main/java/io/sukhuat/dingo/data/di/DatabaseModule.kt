package io.sukhuat.dingo.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Module that provides database dependencies
 * Note: Room database has been removed in favor of Firebase
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    // Firebase dependencies can be added here if needed
}
