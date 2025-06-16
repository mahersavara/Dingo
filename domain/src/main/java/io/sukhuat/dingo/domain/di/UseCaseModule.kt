package io.sukhuat.dingo.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.domain.repository.GoalRepository
import io.sukhuat.dingo.domain.usecase.auth.SignOutUseCase
import io.sukhuat.dingo.domain.usecase.goal.CreateGoalUseCase
import io.sukhuat.dingo.domain.usecase.goal.DeleteGoalUseCase
import io.sukhuat.dingo.domain.usecase.goal.GetGoalsUseCase
import io.sukhuat.dingo.domain.usecase.goal.ReorderGoalsUseCase
import io.sukhuat.dingo.domain.usecase.goal.UpdateGoalStatusUseCase
import io.sukhuat.dingo.domain.usecase.goal.UpdateGoalUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    @Singleton
    fun provideSignOutUseCase(): SignOutUseCase {
        return SignOutUseCase()
    }
    
    @Provides
    @Singleton
    fun provideGetGoalsUseCase(repository: GoalRepository): GetGoalsUseCase {
        return GetGoalsUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideCreateGoalUseCase(repository: GoalRepository): CreateGoalUseCase {
        return CreateGoalUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideUpdateGoalUseCase(repository: GoalRepository): UpdateGoalUseCase {
        return UpdateGoalUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideUpdateGoalStatusUseCase(repository: GoalRepository): UpdateGoalStatusUseCase {
        return UpdateGoalStatusUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideDeleteGoalUseCase(repository: GoalRepository): DeleteGoalUseCase {
        return DeleteGoalUseCase(repository)
    }
    
    @Provides
    @Singleton
    fun provideReorderGoalsUseCase(repository: GoalRepository): ReorderGoalsUseCase {
        return ReorderGoalsUseCase(repository)
    }
} 