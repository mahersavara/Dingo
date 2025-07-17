package io.sukhuat.dingo.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.domain.repository.UserPreferencesRepository
import io.sukhuat.dingo.domain.usecase.preferences.GetAudioFeedbackPreferencesUseCase
import io.sukhuat.dingo.domain.usecase.preferences.GetNotificationPreferencesUseCase
import io.sukhuat.dingo.domain.usecase.preferences.GetUserPreferencesUseCase
import io.sukhuat.dingo.domain.usecase.preferences.ToggleNotificationSettingsUseCase
import io.sukhuat.dingo.domain.usecase.preferences.UpdatePreferencesUseCase
import javax.inject.Singleton

/**
 * Module that provides preferences-related use cases
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesUseCaseModule {

    @Provides
    @Singleton
    fun provideGetUserPreferencesUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): GetUserPreferencesUseCase {
        return GetUserPreferencesUseCase(userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideUpdatePreferencesUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): UpdatePreferencesUseCase {
        return UpdatePreferencesUseCase(userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideGetNotificationPreferencesUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): GetNotificationPreferencesUseCase {
        return GetNotificationPreferencesUseCase(userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideGetAudioFeedbackPreferencesUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): GetAudioFeedbackPreferencesUseCase {
        return GetAudioFeedbackPreferencesUseCase(userPreferencesRepository)
    }

    @Provides
    @Singleton
    fun provideToggleNotificationSettingsUseCase(
        userPreferencesRepository: UserPreferencesRepository
    ): ToggleNotificationSettingsUseCase {
        return ToggleNotificationSettingsUseCase(userPreferencesRepository)
    }
}
