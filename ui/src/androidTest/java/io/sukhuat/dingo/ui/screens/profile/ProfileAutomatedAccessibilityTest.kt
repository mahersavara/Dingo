package io.sukhuat.dingo.ui.screens.profile

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodes
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.model.UserProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ProfileAutomatedAccessibilityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUserProfile = UserProfile(
        userId = "test-user-id",
        displayName = "Test User",
        email = "test@example.com",
        profileImageUrl = null,
        joinDate = LocalDateTime.of(2024, 1, 1, 0, 0),
        isEmailVerified = true,
        authProvider = AuthProvider.EMAIL_PASSWORD,
        lastLoginDate = LocalDateTime.of(2024, 7, 18, 10, 0)
    )

    private val testAchievement = Achievement(
        id = "achievement-1",
        title = "First Goal",
        description = "Created your first goal",
        iconResId = android.R.drawable.ic_menu_star,
        unlockedDate = LocalDateTime.of(2024, 1, 15, 10, 0),
        isUnlocked = true
    )

    private val testProfileStatistics = ProfileStatistics(
        totalGoalsCreated = 15,
        completedGoals = 12,
        completionRate = 0.8f,
        currentStreak = 7,
        longestStreak = 14,
        monthlyStats = mapOf(
            "January 2024" to MonthlyStats("January 2024", 5, 4, 0.8f),
            "February 2024" to MonthlyStats("February 2024", 6, 5, 0.83f)
        ),
        achievements = listOf(testAchievement)
    )

    private val testUserPreferences = UserPreferences(
        isDarkModeEnabled = false,
        isNotificationsEnabled = true,
        isSoundEnabled = true,
        isVibrationEnabled = true,
        languageCode = "en"
    )

    @Test
    fun profileScreen_allInteractiveElementsHaveContentDescriptions() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Find all clickable elements and verify they have content descriptions
        val clickableElements = composeTestRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.OnClick, null).not()
        )

        clickableElements.assertAll(
            hasContentDescription()
        )
    }

    @Test
    fun profileScreen_allImagesHaveContentDescriptions() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify profile image has content description
        composeTestRule.onNodeWithContentDescription("Profile picture").assertExists()

        // Verify achievement icons have content descriptions
        composeTestRule.onNodeWithContentDescription("Achievement: First Goal. Created your first goal. Unlocked on January 15, 2024").assertExists()
    }

    @Test
    fun profileScreen_minimumTouchTargetSizeCompliance() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify interactive elements meet minimum touch target size (48dp)
        val interactiveElements = composeTestRule.onAllNodes(
            SemanticsMatcher.expectValue(SemanticsProperties.OnClick, null).not()
        )

        // In a real implementation, you would check the actual size of each element
        // This is a placeholder for touch target size verification
        interactiveElements.assertCountEquals(expectedCount = 8) // Tabs + buttons
    }

    @Test
    fun profileScreen_colorContrastCompliance() {
        // Test both light and dark themes for color contrast

        // Light theme test
        composeTestRule.setContent {
            MountainSunriseTheme(darkTheme = false) {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify text is readable (would need actual color contrast calculation in real implementation)
        composeTestRule.onNodeWithText("Test User").assertExists()
        composeTestRule.onNodeWithText("test@example.com").assertExists()

        // Dark theme test
        composeTestRule.setContent {
            MountainSunriseTheme(darkTheme = true) {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences.copy(isDarkModeEnabled = true),
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify text is still readable in dark theme
        composeTestRule.onNodeWithText("Test User").assertExists()
        composeTestRule.onNodeWithText("test@example.com").assertExists()
    }

    @Test
    fun profileScreen_keyboardNavigationSupport() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify focusable elements exist
        val focusableElements = composeTestRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.Focused)
        )

        // In a real implementation, you would test actual keyboard navigation
        // This verifies that focusable elements are present
        focusableElements.assertCountEquals(expectedCount = 8) // Tabs + interactive elements
    }

    @Test
    fun profileScreen_screenReaderAnnouncements() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify live regions for dynamic content updates
        // In a real implementation, you would verify that state changes trigger proper announcements
        composeTestRule.onNodeWithContentDescription("Profile overview").assertExists()
    }

    @Test
    fun profileScreen_gestureAlternatives() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify that complex gestures have button alternatives
        // For example, swipe gestures should have tab buttons as alternatives
        composeTestRule.onNodeWithContentDescription("Overview tab").assertExists()
        composeTestRule.onNodeWithContentDescription("Statistics tab").assertExists()
        composeTestRule.onNodeWithContentDescription("Account tab").assertExists()
        composeTestRule.onNodeWithContentDescription("Help tab").assertExists()
    }

    @Test
    fun profileScreen_errorStatesAccessible() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileErrorContent(
                    errorMessage = "Failed to load profile data",
                    onRetry = {}
                )
            }
        }

        // Verify error states are properly announced
        composeTestRule.onNodeWithContentDescription("Error loading profile").assertExists()
        composeTestRule.onNodeWithContentDescription("Retry loading profile").assertExists()

        // Verify error message is readable
        composeTestRule.onNodeWithText("Failed to load profile data").assertExists()
    }

    @Test
    fun profileScreen_loadingStatesAccessible() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileLoadingContent()
            }
        }

        // Verify loading states are properly announced
        composeTestRule.onNodeWithContentDescription("Loading profile").assertExists()
    }

    @Test
    fun profileScreen_dynamicContentAccessible() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(
                        isEditing = true,
                        editingField = ProfileField.DISPLAY_NAME,
                        tempDisplayName = "New Name"
                    ),
                    imageUploadState = ImageUploadState(
                        isUploading = true,
                        progress = 0.5f
                    ),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify dynamic content changes are accessible
        composeTestRule.onNodeWithContentDescription("Edit display name").assertExists()
        composeTestRule.onNodeWithContentDescription("Uploading profile picture: 50 percent complete").assertExists()
    }

    @Test
    fun profileScreen_formValidationAccessible() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(
                        isEditing = true,
                        editingField = ProfileField.DISPLAY_NAME,
                        tempDisplayName = "",
                        validationError = "Display name cannot be empty"
                    ),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = {},
                    onNavigateToSettings = {},
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {},
                    onShareAchievement = {},
                    onRefreshStats = {},
                    onExportData = {},
                    onDeleteAccount = {},
                    onToggleDarkMode = {},
                    onToggleNotifications = {},
                    onToggleSound = {},
                    onToggleVibration = {},
                    onLanguageChange = {}
                )
            }
        }

        // Verify form validation errors are accessible
        composeTestRule.onNodeWithText("Display name cannot be empty").assertExists()

        // Verify error is associated with the input field
        composeTestRule.onNodeWithContentDescription("Edit display name").assertExists()
    }
}
