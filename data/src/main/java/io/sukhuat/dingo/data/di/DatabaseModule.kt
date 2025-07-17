package io.sukhuat.dingo.data.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Module that provides database dependencies
 * Note: Room database has been removed in favor of Firebase
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // Firebase dependencies can be added here if needed
}
