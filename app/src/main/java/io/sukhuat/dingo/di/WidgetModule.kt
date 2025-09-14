package io.sukhuat.dingo.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.domain.service.WidgetNotificationService
import io.sukhuat.dingo.widget.WidgetNotificationServiceImpl
import javax.inject.Singleton

/**
 * Hilt module for widget-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {

    @Binds
    @Singleton
    abstract fun bindWidgetNotificationService(
        widgetNotificationServiceImpl: WidgetNotificationServiceImpl
    ): WidgetNotificationService
}
