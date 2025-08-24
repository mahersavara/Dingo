package io.sukhuat.dingo.data.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.data.remote.FirebaseYearPlannerService
import io.sukhuat.dingo.data.repository.YearPlannerRepositoryImpl
import io.sukhuat.dingo.domain.repository.YearPlannerRepository
import javax.inject.Singleton

/**
 * Hilt module for Year Planner data layer dependencies
 * Follows the existing RepositoryModule pattern in the project
 */
@Module
@InstallIn(SingletonComponent::class)
object YearPlannerDataModule {

    /**
     * Provides the Firebase Year Planner Service
     */
    @Provides
    @Singleton
    fun provideFirebaseYearPlannerService(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth
    ): FirebaseYearPlannerService {
        return FirebaseYearPlannerService(firestore, auth)
    }

    /**
     * Provides the Year Planner Repository implementation
     */
    @Provides
    @Singleton
    fun provideYearPlannerRepository(
        firebaseService: FirebaseYearPlannerService,
        @ApplicationContext context: Context
    ): YearPlannerRepository {
        return YearPlannerRepositoryImpl(firebaseService, context)
    }
}
