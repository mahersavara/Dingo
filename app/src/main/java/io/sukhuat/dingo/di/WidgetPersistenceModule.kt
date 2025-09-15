package io.sukhuat.dingo.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.widget.WeeklyGoalWidgetRepository
import io.sukhuat.dingo.widget.WidgetDataLoader
import io.sukhuat.dingo.widget.WidgetErrorHandler
import io.sukhuat.dingo.widget.persistence.EnhancedWidgetDataLoader
import io.sukhuat.dingo.widget.persistence.WidgetPersistenceManager
import javax.inject.Singleton

/**
 * Hilt module for enhanced widget persistence components
 */
@Module
@InstallIn(SingletonComponent::class)
object WidgetPersistenceModule {

    @Provides
    @Singleton
    fun provideWidgetPersistenceManager(
        @ApplicationContext context: Context
    ): WidgetPersistenceManager {
        return WidgetPersistenceManager(context)
    }

    @Provides
    @Singleton
    fun provideEnhancedWidgetDataLoader(
        @ApplicationContext context: Context,
        widgetRepository: WeeklyGoalWidgetRepository,
        persistenceManager: WidgetPersistenceManager,
        errorHandler: WidgetErrorHandler
    ): EnhancedWidgetDataLoader {
        return EnhancedWidgetDataLoader(
            context = context,
            widgetRepository = widgetRepository,
            persistenceManager = persistenceManager,
            errorHandler = errorHandler
        )
    }

    @Provides
    @Singleton
    fun provideWidgetDataMigration(
        @ApplicationContext context: Context,
        persistenceManager: WidgetPersistenceManager,
        originalDataLoader: WidgetDataLoader
    ): io.sukhuat.dingo.widget.persistence.WidgetDataMigration {
        return io.sukhuat.dingo.widget.persistence.WidgetDataMigration(
            context,
            persistenceManager,
            originalDataLoader
        )
    }

    @Provides
    @Singleton
    fun provideEnhancedWidgetDataUpdater(
        @ApplicationContext context: Context,
        enhancedDataLoader: EnhancedWidgetDataLoader,
        persistenceManager: WidgetPersistenceManager,
        originalDataLoader: WidgetDataLoader
    ): io.sukhuat.dingo.widget.EnhancedWidgetDataUpdater {
        return io.sukhuat.dingo.widget.EnhancedWidgetDataUpdater(
            context,
            enhancedDataLoader,
            persistenceManager,
            originalDataLoader
        )
    }

    @Provides
    @Singleton
    fun provideEnhancedWidgetInitializer(
        @ApplicationContext context: Context,
        persistenceManager: WidgetPersistenceManager,
        enhancedDataLoader: EnhancedWidgetDataLoader,
        widgetDataMigration: io.sukhuat.dingo.widget.persistence.WidgetDataMigration,
        enhancedUpdater: io.sukhuat.dingo.widget.EnhancedWidgetDataUpdater
    ): io.sukhuat.dingo.widget.EnhancedWidgetInitializer {
        return io.sukhuat.dingo.widget.EnhancedWidgetInitializer(
            context,
            persistenceManager,
            enhancedDataLoader,
            widgetDataMigration,
            enhancedUpdater
        )
    }
}
