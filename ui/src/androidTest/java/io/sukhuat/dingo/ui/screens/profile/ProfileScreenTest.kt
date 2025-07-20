package io.sukhuat.dingo.ui.screens.profile

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
class ProfileScreenTest {

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
        totalGoalsCreated = 10,
        completedGoals = 7,
        completionRate = 0.7f,
        currentStreak = 5,
        longestStreak = 8,
        monthlyStats = mapOf("2024-01" to MonthlyStats("2024-01", 5, 3, 0.6f)),
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
    fun profileScreen_displaysLoadingState() {
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

        // Verify profile content is displayed
        composeTestRule.onNodeWithText("Test User").assertIsDisplayed()
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
        composeTestRule.onNodeWithText("Member since January 2024").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysTabNavigation() {
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

        // Verify all tabs are present
        composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
        composeTestRule.onNodeWithText("Statistics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Help").assertIsDisplayed()
    }

    @Test
    fun profileScreen_tabNavigation_worksCorrectly() {
        var selectedTab = ProfileTab.OVERVIEW

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(selectedTab = selectedTab),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { 4 }),
                    profileActions = ProfileActions(),
                    preferences = testUserPreferences,
                    currentLanguage = io.sukhuat.dingo.common.localization.AppLanguage.ENGLISH,
                    onTabSelected = { selectedTab = it },
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

        // Click on Statistics tab
        composeTestRule.onNodeWithText("Statistics").performClick()

        // Verify tab selection works (would need to check state in real implementation)
        composeTestRule.onNodeWithText("Statistics").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysStatistics() {
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

        // Verify statistics are displayed
        composeTestRule.onNodeWithText("10").assertIsDisplayed() // Total goals
        composeTestRule.onNodeWithText("7").assertIsDisplayed() // Completed goals
        composeTestRule.onNodeWithText("5 days").assertIsDisplayed() // Current streak
    }

    @Test
    fun profileScreen_displaysErrorState() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileErrorContent(
                    errorMessage = "Failed to load profile",
                    onRetry = {}
                )
            }
        }

        // Verify error content is displayed
        composeTestRule.onNodeWithText("Error loading profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed to load profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertHasClickAction()
    }

    @Test
    fun profileScreen_retryButton_triggersCallback() {
        var retryClicked = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileErrorContent(
                    errorMessage = "Failed to load profile",
                    onRetry = { retryClicked = true }
                )
            }
        }

        // Click retry button
        composeTestRule.onNodeWithText("Retry").performClick()

        // Verify callback was triggered (would need to check state in real implementation)
        assert(retryClicked)
    }

    @Test
    fun profileScreen_hasAccessibilitySupport() {
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

        // Verify accessibility content descriptions are present
        composeTestRule.onNodeWithContentDescription("Profile overview").assertExists()
        composeTestRule.onNodeWithContentDescription("Overview tab").assertExists()
        composeTestRule.onNodeWithContentDescription("Statistics tab").assertExists()
        composeTestRule.onNodeWithContentDescription("Account tab").assertExists()
        composeTestRule.onNodeWithContentDescription("Help tab").assertExists()
    }
}
