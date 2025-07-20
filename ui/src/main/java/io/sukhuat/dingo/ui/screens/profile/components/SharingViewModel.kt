package io.sukhuat.dingo.ui.screens.profile.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.SharingPrivacySettings
import io.sukhuat.dingo.domain.model.SocialPlatform
import io.sukhuat.dingo.domain.usecase.profile.GetAchievementsUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageReferralUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageSharingPrivacyUseCase
import io.sukhuat.dingo.domain.usecase.profile.ShareAchievementUseCase
import io.sukhuat.dingo.domain.usecase.profile.ShareProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing sharing functionality
 */
@HiltViewModel
class SharingViewModel @Inject constructor(
    private val shareAchievementUseCase: ShareAchievementUseCase,
    private val shareProfileUseCase: ShareProfileUseCase,
    private val manageReferralUseCase: ManageReferralUseCase,
    private val manageSharingPrivacyUseCase: ManageSharingPrivacyUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SharingUiState())
    val uiState: StateFlow<SharingUiState> = _uiState.asStateFlow()

    private val _dialogState = MutableStateFlow(SharingDialogState())
    val dialogState: StateFlow<SharingDialogState> = _dialogState.asStateFlow()

    init {
        loadSharingData()
    }

    /**
     * Load sharing data including privacy settings and referral data
     */
    private fun loadSharingData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load sharing data sequentially for now
                val privacySettings = manageSharingPrivacyUseCase.getSharingPrivacySettings().first()
                val referralData = manageReferralUseCase.getReferralData().first()
                val sharingStats = shareAchievementUseCase.getSharingStats()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    privacySettings = privacySettings,
                    referralData = referralData,
                    sharingStats = sharingStats,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load sharing data"
                )
            }
        }
    }

    /**
     * Share an achievement
     */
    fun shareAchievement(achievementId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGeneratingContent = true)

                val achievements = getAchievementsUseCase()
                val achievement = achievements.find { it.id == achievementId }

                if (achievement == null) {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingContent = false,
                        error = "Achievement not found"
                    )
                    return@launch
                }

                if (!_uiState.value.privacySettings.allowAchievementSharing) {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingContent = false,
                        error = "Achievement sharing is disabled in privacy settings"
                    )
                    return@launch
                }

                val shareableContent = shareAchievementUseCase(
                    achievement = achievement,
                    includeAppPromotion = _uiState.value.privacySettings.includeAppPromotion
                )

                _dialogState.value = SharingDialogState(
                    isVisible = true,
                    title = "Share Achievement",
                    content = shareableContent,
                    availablePlatforms = listOf(
                        SocialPlatform.TWITTER,
                        SocialPlatform.FACEBOOK,
                        SocialPlatform.INSTAGRAM,
                        SocialPlatform.LINKEDIN,
                        SocialPlatform.WHATSAPP,
                        SocialPlatform.GENERIC
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isGeneratingContent = false,
                    generatedContent = shareableContent,
                    showSharingDialog = true
                )
            } catch (e: ProfileError.ValidationError) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingContent = false,
                    error = e.message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingContent = false,
                    error = "Failed to generate sharing content"
                )
            }
        }
    }

    /**
     * Share user profile
     */
    fun shareProfile() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGeneratingContent = true)

                if (!_uiState.value.privacySettings.allowProfileSharing) {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingContent = false,
                        error = "Profile sharing is disabled in privacy settings"
                    )
                    return@launch
                }

                val shareableContent = shareProfileUseCase(
                    includeStats = true,
                    includeAppPromotion = _uiState.value.privacySettings.includeAppPromotion
                )

                _dialogState.value = SharingDialogState(
                    isVisible = true,
                    title = "Share Profile",
                    content = shareableContent,
                    availablePlatforms = listOf(
                        SocialPlatform.TWITTER,
                        SocialPlatform.FACEBOOK,
                        SocialPlatform.INSTAGRAM,
                        SocialPlatform.LINKEDIN,
                        SocialPlatform.WHATSAPP,
                        SocialPlatform.GENERIC
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isGeneratingContent = false,
                    generatedContent = shareableContent,
                    showSharingDialog = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingContent = false,
                    error = "Failed to generate profile sharing content"
                )
            }
        }
    }

    /**
     * Share referral invitation
     */
    fun shareReferral(personalMessage: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGeneratingContent = true)

                if (!_uiState.value.privacySettings.allowReferralSharing) {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingContent = false,
                        error = "Referral sharing is disabled in privacy settings"
                    )
                    return@launch
                }

                val shareableContent = manageReferralUseCase.createReferralShareContent(
                    personalMessage = personalMessage,
                    includeIncentive = true
                )

                _dialogState.value = SharingDialogState(
                    isVisible = true,
                    title = "Invite Friends",
                    content = shareableContent,
                    availablePlatforms = listOf(
                        SocialPlatform.WHATSAPP,
                        SocialPlatform.FACEBOOK,
                        SocialPlatform.TWITTER,
                        SocialPlatform.INSTAGRAM,
                        SocialPlatform.LINKEDIN,
                        SocialPlatform.GENERIC
                    )
                )

                _uiState.value = _uiState.value.copy(
                    isGeneratingContent = false,
                    generatedContent = shareableContent,
                    showSharingDialog = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingContent = false,
                    error = "Failed to generate referral sharing content"
                )
            }
        }
    }

    /**
     * Update privacy settings
     */
    fun updatePrivacySettings(settings: SharingPrivacySettings) {
        viewModelScope.launch {
            try {
                manageSharingPrivacyUseCase.updateSharingPrivacySettings(settings)
                _uiState.value = _uiState.value.copy(
                    privacySettings = settings,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update privacy settings"
                )
            }
        }
    }

    /**
     * Generate new referral code
     */
    fun generateReferralCode() {
        viewModelScope.launch {
            try {
                val newCode = manageReferralUseCase.generateNewReferralCode()
                // Reload referral data to get updated information
                loadSharingData()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to generate new referral code"
                )
            }
        }
    }

    /**
     * Select platform for sharing
     */
    fun selectPlatform(platform: SocialPlatform) {
        _dialogState.value = _dialogState.value.copy(selectedPlatform = platform)
        _uiState.value = _uiState.value.copy(selectedPlatform = platform)
    }

    /**
     * Confirm sharing with selected platform
     */
    fun confirmShare() {
        val platform = _dialogState.value.selectedPlatform ?: return
        val content = _dialogState.value.content ?: return

        viewModelScope.launch {
            try {
                _dialogState.value = _dialogState.value.copy(isLoading = true)

                // Generate platform-specific content
                val platformContent = when {
                    content.achievementTitle != null -> {
                        // This is an achievement share
                        val achievements = getAchievementsUseCase()
                        val achievement = achievements.find { it.title == content.achievementTitle }
                        if (achievement != null) {
                            shareAchievementUseCase.generatePlatformSpecificContent(
                                achievement = achievement,
                                platform = platform,
                                includeAppPromotion = _uiState.value.privacySettings.includeAppPromotion
                            )
                        } else {
                            content
                        }
                    }
                    content.profileLink != null && content.achievementTitle == null -> {
                        // This is a profile share
                        shareProfileUseCase.generatePlatformSpecificContent(
                            platform = platform,
                            includeStats = true,
                            includeAppPromotion = _uiState.value.privacySettings.includeAppPromotion
                        )
                    }
                    else -> {
                        // This is a referral share
                        manageReferralUseCase.generatePlatformSpecificReferralContent(
                            platform = platform,
                            personalMessage = null,
                            includeIncentive = true
                        )
                    }
                }

                // Here you would integrate with Android's sharing system
                // For now, we'll just update the state to indicate success
                _dialogState.value = SharingDialogState()
                _uiState.value = _uiState.value.copy(
                    showSharingDialog = false,
                    generatedContent = null,
                    selectedPlatform = null
                )
            } catch (e: Exception) {
                _dialogState.value = _dialogState.value.copy(
                    isLoading = false
                )
                _uiState.value = _uiState.value.copy(
                    error = "Failed to share content"
                )
            }
        }
    }

    /**
     * Dismiss sharing dialog
     */
    fun dismissDialog() {
        _dialogState.value = SharingDialogState()
        _uiState.value = _uiState.value.copy(
            showSharingDialog = false,
            generatedContent = null,
            selectedPlatform = null
        )
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
