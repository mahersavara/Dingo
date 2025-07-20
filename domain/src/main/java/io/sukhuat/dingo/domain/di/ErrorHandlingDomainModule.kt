package io.sukhuat.dingo.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.domain.model.ErrorRecoveryManager
import io.sukhuat.dingo.domain.model.ProfileErrorHandler
import io.sukhuat.dingo.domain.validation.ProfileValidator
import javax.inject.Singleton

/**
 * Dependency injection module for domain layer error handling components
 */
@Module
@InstallIn(SingletonComponent::class)
object ErrorHandlingDomainModule {

    @Provides
    @Singleton
    fun provideProfileErrorHandler(): ProfileErrorHandler {
        return ProfileErrorHandler()
    }

    @Provides
    @Singleton
    fun provideErrorRecoveryManager(
        profileErrorHandler: ProfileErrorHandler
    ): ErrorRecoveryManager {
        return ErrorRecoveryManager(profileErrorHandler)
    }

    @Provides
    @Singleton
    fun provideProfileValidator(): ProfileValidator {
        return ProfileValidator()
    }
}
