package io.sukhuat.dingo.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.domain.repository.AuthRepository
import io.sukhuat.dingo.usecases.auth.CheckEmailVerificationUseCase
import io.sukhuat.dingo.usecases.auth.ResendEmailVerificationUseCase
import io.sukhuat.dingo.usecases.auth.SendEmailVerificationUseCase
import io.sukhuat.dingo.usecases.auth.SendPasswordResetUseCase
import io.sukhuat.dingo.usecases.auth.ValidatePasswordStrengthUseCase
import javax.inject.Singleton

/**
 * Module that provides authentication-related use cases
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthUseCaseModule {

    @Provides
    @Singleton
    fun provideSendEmailVerificationUseCase(
        repository: AuthRepository
    ): SendEmailVerificationUseCase = SendEmailVerificationUseCase(repository)

    @Provides
    @Singleton
    fun provideCheckEmailVerificationUseCase(
        repository: AuthRepository
    ): CheckEmailVerificationUseCase = CheckEmailVerificationUseCase(repository)

    @Provides
    @Singleton
    fun provideResendEmailVerificationUseCase(
        repository: AuthRepository
    ): ResendEmailVerificationUseCase = ResendEmailVerificationUseCase(repository)

    @Provides
    @Singleton
    fun provideSendPasswordResetUseCase(
        repository: AuthRepository
    ): SendPasswordResetUseCase = SendPasswordResetUseCase(repository)

    @Provides
    @Singleton
    fun provideValidatePasswordStrengthUseCase(
        repository: AuthRepository
    ): ValidatePasswordStrengthUseCase = ValidatePasswordStrengthUseCase(repository)
}
