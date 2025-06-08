package io.sukhuat.dingo.data.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.auth.GoogleAuthService
import io.sukhuat.dingo.data.repository.auth.AuthRepository
import io.sukhuat.dingo.data.repository.auth.FirebaseAuthRepositoryImpl
import javax.inject.Singleton

/**
 * Module that provides all authentication related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideGoogleAuthService(@ApplicationContext context: Context): GoogleAuthService {
        return GoogleAuthService(context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        googleAuthService: GoogleAuthService
    ): AuthRepository = FirebaseAuthRepositoryImpl(firebaseAuth, googleAuthService)
}
