package io.sukhuat.dingo.data.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.remote.FirebaseGoalService
import io.sukhuat.dingo.data.remote.FirebaseStorageService
import javax.inject.Singleton

/**
 * Module that provides Firebase dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage

    @Provides
    @Singleton
    fun provideFirebaseGoalService(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseGoalService = FirebaseGoalService(firestore, auth)

    @Provides
    @Singleton
    fun provideFirebaseStorageService(
        storage: FirebaseStorage,
        auth: FirebaseAuth,
        @ApplicationContext context: Context
    ): FirebaseStorageService = FirebaseStorageService(context, storage, auth)
}
