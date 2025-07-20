package io.sukhuat.dingo.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.sukhuat.dingo.domain.repository.ProfileStatisticsRepository
import io.sukhuat.dingo.domain.repository.SharingRepository
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import io.sukhuat.dingo.domain.usecase.profile.GetAchievementsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetProfileStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetUserProfileUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageProfileImageUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageReferralUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageSharingPrivacyUseCase
import io.sukhuat.dingo.domain.usecase.profile.RefreshStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.ShareAchievementUseCase
import io.sukhuat.dingo.domain.usecase.profile.ShareProfileUseCase
import io.sukhuat.dingo.domain.usecase.profile.UpdateProfileUseCase
import javax.inject.Singleton

/**
 * Hilt module that provides profile-related use cases
 */
@Module
@InstallIn(SingletonComponent::class)
object ProfileUseCaseModule {

    @Provides
    @Singleton
    fun provideGetUserProfileUseCase(
        userProfileRepository: UserProfileRepository
    ): GetUserProfileUseCase {
        return GetUserProfileUseCase(userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideUpdateProfileUseCase(
        userProfileRepository: UserProfileRepository
    ): UpdateProfileUseCase {
        return UpdateProfileUseCase(userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideManageProfileImageUseCase(
        userProfileRepository: UserProfileRepository
    ): ManageProfileImageUseCase {
        return ManageProfileImageUseCase(userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideGetProfileStatisticsUseCase(
        profileStatisticsRepository: ProfileStatisticsRepository
    ): GetProfileStatisticsUseCase {
        return GetProfileStatisticsUseCase(profileStatisticsRepository)
    }

    @Provides
    @Singleton
    fun provideRefreshStatisticsUseCase(
        profileStatisticsRepository: ProfileStatisticsRepository
    ): RefreshStatisticsUseCase {
        return RefreshStatisticsUseCase(profileStatisticsRepository)
    }

    @Provides
    @Singleton
    fun provideGetAchievementsUseCase(
        profileStatisticsRepository: ProfileStatisticsRepository
    ): GetAchievementsUseCase {
        return GetAchievementsUseCase(profileStatisticsRepository)
    }

    @Provides
    @Singleton
    fun provideShareAchievementUseCase(
        sharingRepository: SharingRepository
    ): ShareAchievementUseCase {
        return ShareAchievementUseCase(sharingRepository)
    }

    @Provides
    @Singleton
    fun provideShareProfileUseCase(
        sharingRepository: SharingRepository,
        userProfileRepository: UserProfileRepository
    ): ShareProfileUseCase {
        return ShareProfileUseCase(sharingRepository, userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideManageReferralUseCase(
        sharingRepository: SharingRepository,
        userProfileRepository: UserProfileRepository
    ): ManageReferralUseCase {
        return ManageReferralUseCase(sharingRepository, userProfileRepository)
    }

    @Provides
    @Singleton
    fun provideManageSharingPrivacyUseCase(
        sharingRepository: SharingRepository
    ): ManageSharingPrivacyUseCase {
        return ManageSharingPrivacyUseCase(sharingRepository)
    }
}
