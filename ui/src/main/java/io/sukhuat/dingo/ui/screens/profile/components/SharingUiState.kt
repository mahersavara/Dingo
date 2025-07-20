package io.sukhuat.dingo.ui.screens.profile.components

import io.sukhuat.dingo.domain.model.ReferralData
import io.sukhuat.dingo.domain.model.ShareableContent
import io.sukhuat.dingo.domain.model.SharingPrivacySettings
import io.sukhuat.dingo.domain.model.SharingStats
import io.sukhuat.dingo.domain.model.SocialPlatform

/**
 * UI state for sharing components
 */
data class SharingUiState(
    val isLoading: Boolean = false,
    val privacySettings: SharingPrivacySettings = SharingPrivacySettings(),
    val referralData: ReferralData? = null,
    val sharingStats: SharingStats? = null,
    val error: String? = null,
    val isGeneratingContent: Boolean = false,
    val generatedContent: ShareableContent? = null,
    val showSharingDialog: Boolean = false,
    val selectedPlatform: SocialPlatform? = null
)

/**
 * Actions for sharing components
 */
data class SharingActions(
    val onShareAchievement: (String) -> Unit,
    val onShareProfile: () -> Unit,
    val onShareReferral: (String?) -> Unit,
    val onUpdatePrivacySettings: (SharingPrivacySettings) -> Unit,
    val onGenerateReferralCode: () -> Unit,
    val onDismissDialog: () -> Unit,
    val onSelectPlatform: (SocialPlatform) -> Unit,
    val onConfirmShare: () -> Unit
)

/**
 * Sharing dialog state
 */
data class SharingDialogState(
    val isVisible: Boolean = false,
    val title: String = "",
    val content: ShareableContent? = null,
    val availablePlatforms: List<SocialPlatform> = emptyList(),
    val selectedPlatform: SocialPlatform? = null,
    val isLoading: Boolean = false
)
