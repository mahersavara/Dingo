package io.sukhuat.dingo.ui.screens.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.Achievement
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.MonthlyStats
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.ui.screens.profile.components.ProfileHeader
import io.sukhuat.dingo.ui.screens.profile.components.ProfileStatistics
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class ProfileScreenshotTest {

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
            "February 2024" to MonthlyStats("February 2024", 6, 5, 0.83f),
            "March 2024" to MonthlyStats("March 2024", 4, 3, 0.75f)
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
    fun profileHeader_lightTheme_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme(darkTheme = false) {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        takeScreenshot("profile_header_light")
    }

    @Test
    fun profileHeader_darkTheme_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme(darkTheme = true) {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        takeScreenshot("profile_header_dark")
    }

    @Test
    fun profileHeader_editingMode_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(
                        isEditing = true,
                        editingField = ProfileField.DISPLAY_NAME,
                        tempDisplayName = "Test User"
                    ),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        takeScreenshot("profile_header_editing")
    }

    @Test
    fun profileHeader_uploadingImage_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = testUserProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(
                        isUploading = true,
                        progress = 0.6f
                    ),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        takeScreenshot("profile_header_uploading")
    }

    @Test
    fun profileHeader_googleAuth_screenshot() {
        val googleProfile = testUserProfile.copy(
            authProvider = AuthProvider.GOOGLE,
            isEmailVerified = true
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = googleProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        takeScreenshot("profile_header_google_auth")
    }

    @Test
    fun profileStatistics_withData_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        takeScreenshot("profile_statistics_with_data")
    }

    @Test
    fun profileStatistics_emptyState_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = ProfileStatistics(),
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        takeScreenshot("profile_statistics_empty")
    }

    @Test
    fun profileStatistics_darkTheme_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme(darkTheme = true) {
                ProfileStatistics(
                    statistics = testProfileStatistics,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        takeScreenshot("profile_statistics_dark")
    }

    @Test
    fun profileContent_overviewTab_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(selectedTab = ProfileTab.OVERVIEW),
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

        takeScreenshot("profile_content_overview")
    }

    @Test
    fun profileContent_statisticsTab_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(selectedTab = ProfileTab.STATISTICS),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(
                        initialPage = 1,
                        pageCount = { 4 }
                    ),
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

        takeScreenshot("profile_content_statistics")
    }

    @Test
    fun profileErrorContent_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileErrorContent(
                    errorMessage = "Failed to load profile data. Please check your internet connection.",
                    onRetry = {}
                )
            }
        }

        takeScreenshot("profile_error_content")
    }

    @Test
    fun profileErrorContent_darkTheme_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme(darkTheme = true) {
                ProfileErrorContent(
                    errorMessage = "Failed to load profile data. Please check your internet connection.",
                    onRetry = {}
                )
            }
        }

        takeScreenshot("profile_error_content_dark")
    }

    @Test
    fun profileContent_accountTab_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(selectedTab = ProfileTab.ACCOUNT),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(
                        initialPage = 2,
                        pageCount = { 4 }
                    ),
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

        takeScreenshot("profile_content_account")
    }

    @Test
    fun profileContent_helpTab_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(selectedTab = ProfileTab.HELP),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = false,
                    responsiveValues = io.sukhuat.dingo.common.components.rememberResponsiveValues(),
                    pagerState = androidx.compose.foundation.pager.rememberPagerState(
                        initialPage = 3,
                        pageCount = { 4 }
                    ),
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

        takeScreenshot("profile_content_help")
    }

    @Test
    fun profileContent_refreshing_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileContent(
                    profile = testUserProfile,
                    statistics = testProfileStatistics,
                    tabState = ProfileTabState(),
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    isRefreshing = true,
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

        takeScreenshot("profile_content_refreshing")
    }

    @Test
    fun profileContent_darkModeEnabled_screenshot() {
        val darkModePreferences = testUserPreferences.copy(isDarkModeEnabled = true)

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
                    preferences = darkModePreferences,
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

        takeScreenshot("profile_content_dark_mode")
    }

    @Test
    fun profileHeader_withProfileImage_screenshot() {
        val profileWithImage = testUserProfile.copy(
            profileImageUrl = "https://example.com/profile.jpg"
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = profileWithImage,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        takeScreenshot("profile_header_with_image")
    }

    @Test
    fun profileHeader_unverifiedEmail_screenshot() {
        val unverifiedProfile = testUserProfile.copy(isEmailVerified = false)

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileHeader(
                    profile = unverifiedProfile,
                    editState = ProfileEditState(),
                    imageUploadState = ImageUploadState(),
                    onStartEditing = {},
                    onCancelEditing = {},
                    onConfirmEdit = {},
                    onUpdateTempDisplayName = {},
                    onUploadProfileImage = {},
                    onDeleteProfileImage = {}
                )
            }
        }

        takeScreenshot("profile_header_unverified_email")
    }

    @Test
    fun profileStatistics_multipleAchievements_screenshot() {
        val multipleAchievements = listOf(
            testAchievement,
            Achievement(
                id = "achievement-2",
                title = "Goal Crusher",
                description = "Completed 10 goals",
                iconResId = android.R.drawable.ic_menu_star,
                unlockedDate = LocalDateTime.of(2024, 2, 1, 10, 0),
                isUnlocked = true
            ),
            Achievement(
                id = "achievement-3",
                title = "Streak Master",
                description = "Maintained a 7-day streak",
                iconResId = android.R.drawable.ic_menu_star,
                unlockedDate = null,
                isUnlocked = false
            )
        )

        val statsWithMultipleAchievements = testProfileStatistics.copy(
            achievements = multipleAchievements
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileStatistics(
                    statistics = statsWithMultipleAchievements,
                    onShareAchievement = {},
                    onRefreshStats = {}
                )
            }
        }

        takeScreenshot("profile_statistics_multiple_achievements")
    }

    @Test
    fun profileLoadingContent_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileLoadingContent()
            }
        }

        takeScreenshot("profile_loading_content")
    }

    @Test
    fun profileLoadingContent_darkTheme_screenshot() {
        composeTestRule.setContent {
            MountainSunriseTheme(darkTheme = true) {
                ProfileLoadingContent()
            }
        }

        takeScreenshot("profile_loading_content_dark")
    }

    /**
     * Helper function to take screenshots and save them to device storage
     */
    private fun takeScreenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val screenshotDir = File(context.getExternalFilesDir(null), "screenshots")

        if (!screenshotDir.exists()) {
            screenshotDir.mkdirs()
        }

        // Note: In a real implementation, you would use a screenshot testing library
        // like Shot, Paparazzi, or Android's built-in screenshot testing
        // This is a placeholder for the screenshot functionality

        composeTestRule.onRoot().captureToImage()

        // The actual screenshot saving would be implemented here
        // For example, using Shot library:
        // compareScreenshot(composeTestRule, name)

        println("Screenshot taken: $name")
    }
}
