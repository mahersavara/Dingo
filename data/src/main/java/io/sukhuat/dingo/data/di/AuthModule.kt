package io.sukhuat.dingo.data.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.auth.EmailVerificationManager
import io.sukhuat.dingo.data.auth.GoogleAuthService
import io.sukhuat.dingo.data.auth.PasswordResetManager
import io.sukhuat.dingo.data.repository.auth.FirebaseAuthRepositoryImpl
import io.sukhuat.dingo.domain.repository.AuthRepository
import javax.inject.Singleton

/**
 * Module that provides all authentication related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideGoogleAuthService(
        @ApplicationContext context: Context
    ): GoogleAuthService {
        return GoogleAuthService(context)
    }

    @Provides
    @Singleton
    fun provideEmailVerificationManager(
        firebaseAuth: FirebaseAuth
    ): EmailVerificationManager {
        return EmailVerificationManager(firebaseAuth)
    }

    @Provides
    @Singleton
    fun providePasswordResetManager(
        firebaseAuth: FirebaseAuth
    ): PasswordResetManager {
        return PasswordResetManager(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        googleAuthService: GoogleAuthService,
        emailVerificationManager: EmailVerificationManager,
        passwordResetManager: PasswordResetManager
    ): AuthRepository = FirebaseAuthRepositoryImpl(
        firebaseAuth,
        googleAuthService,
        emailVerificationManager,
        passwordResetManager
    )
}
