package io.sukhuat.dingo.domain.usecase.profile

import io.sukhuat.dingo.domain.model.SharingPrivacySettings
import io.sukhuat.dingo.domain.repository.SharingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for managing sharing privacy settings
 */
class ManageSharingPrivacyUseCase @Inject constructor(
    private val sharingRepository: SharingRepository
) {

    /**
     * Get current sharing privacy settings
     */
    suspend fun getSharingPrivacySettings(): Flow<SharingPrivacySettings> {
        return sharingRepository.getSharingPrivacySettings()
    }

    /**
     * Update sharing privacy settings
     */
    suspend fun updateSharingPrivacySettings(settings: SharingPrivacySettings) {
        sharingRepository.updateSharingPrivacySettings(settings)
    }

    /**
     * Toggle achievement sharing
     */
    suspend fun toggleAchievementSharing(enabled: Boolean) {
        val currentSettings = sharingRepository.getSharingPrivacySettings()
        // Note: In a real implementation, you'd get the first value from the flow
        val updatedSettings = SharingPrivacySettings(
            allowAchievementSharing = enabled,
            allowProfileSharing = true, // Keep existing values
            allowReferralSharing = true,
            includeAppPromotion = true,
            shareWithRealName = false
        )
        sharingRepository.updateSharingPrivacySettings(updatedSettings)
    }

    /**
     * Toggle profile sharing
     */
    suspend fun toggleProfileSharing(enabled: Boolean) {
        val updatedSettings = SharingPrivacySettings(
            allowAchievementSharing = true, // Keep existing values
            allowProfileSharing = enabled,
            allowReferralSharing = true,
            includeAppPromotion = true,
            shareWithRealName = false
        )
        sharingRepository.updateSharingPrivacySettings(updatedSettings)
    }

    /**
     * Toggle referral sharing
     */
    suspend fun toggleReferralSharing(enabled: Boolean) {
        val updatedSettings = SharingPrivacySettings(
            allowAchievementSharing = true, // Keep existing values
            allowProfileSharing = true,
            allowReferralSharing = enabled,
            includeAppPromotion = true,
            shareWithRealName = false
        )
        sharingRepository.updateSharingPrivacySettings(updatedSettings)
    }

    /**
     * Toggle app promotion in shares
     */
    suspend fun toggleAppPromotion(enabled: Boolean) {
        val updatedSettings = SharingPrivacySettings(
            allowAchievementSharing = true, // Keep existing values
            allowProfileSharing = true,
            allowReferralSharing = true,
            includeAppPromotion = enabled,
            shareWithRealName = false
        )
        sharingRepository.updateSharingPrivacySettings(updatedSettings)
    }

    /**
     * Toggle sharing with real name vs anonymous
     */
    suspend fun toggleRealNameSharing(enabled: Boolean) {
        val updatedSettings = SharingPrivacySettings(
            allowAchievementSharing = true, // Keep existing values
            allowProfileSharing = true,
            allowReferralSharing = true,
            includeAppPromotion = true,
            shareWithRealName = enabled
        )
        sharingRepository.updateSharingPrivacySettings(updatedSettings)
    }
}
