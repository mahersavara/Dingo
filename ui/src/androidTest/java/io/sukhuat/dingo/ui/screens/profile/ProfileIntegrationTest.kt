package io.sukhuat.dingo.ui.screens.profile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.usecase.account.ChangePasswordUseCase
import io.sukhuat.dingo.domain.usecase.account.DeleteAccountUseCase
import io.sukhuat.dingo.domain.usecase.account.ExportUserDataUseCase
import io.sukhuat.dingo.domain.usecase.account.GetLoginHistoryUseCase
import io.sukhuat.dingo.domain.usecase.preferences.GetUserPreferencesUseCase
import io.sukhuat.dingo.domain.usecase.preferences.UpdatePreferencesUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetAchievementsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetProfileStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetUserProfileUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageProfileImageUseCase
import io.sukhuat.dingo.domain.usecase.profile.RefreshStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.ShareAchievementUseCase
import io.sukhuat.dingo.domain.usecase.profile.UpdateProfileUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ProfileIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    // Mock use cases - in real integration test these would be injected
    private lateinit var getUserProfileUseCase: GetUserProfileUseCase
    private lateinit var getProfileStatisticsUseCase: GetProfileStatisticsUseCase
    private lateinit var updateProfileUseCase: UpdateProfileUseCase
    private lateinit var manageProfileImageUseCase: ManageProfileImageUseCase
    private lateinit var refreshStatisticsUseCase: RefreshStatisticsUseCase
    private lateinit var getAchievementsUseCase: GetAchievementsUseCase
    private lateinit var shareAchievementUseCase: ShareAchievementUseCase
    private lateinit var exportUserDataUseCase: ExportUserDataUseCase
    private lateinit var deleteAccountUseCase: DeleteAccountUseCase
    private lateinit var changePasswordUseCase: ChangePasswordUseCase
    private lateinit var getLoginHistoryUseCase: GetLoginHistoryUseCase
    private lateinit var getUserPreferencesUseCase: GetUserPreferencesUseCase
    private lateinit var updatePreferencesUseCase: UpdatePreferencesUseCase

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

    private val testProfileStatistics = ProfileStatistics(
        totalGoalsCreated = 10,
        completedGoals = 7,
        completionRate = 0.7f,
        currentStreak = 5,
        longestStreak = 8,
        monthlyStats = emptyMap(),
        achievements = emptyList()
    )

    private val testUserPreferences = UserPreferences(
        isDarkModeEnabled = false,
        isNotificationsEnabled = true,
        isSoundEnabled = true,
        isVibrationEnabled = true,
        languageCode = "en"
    )

    @Before
    fun setUp() {
        hiltRule.inject()

        // Initialize mocks
        getUserProfileUseCase = mockk()
        getProfileStatisticsUseCase = mockk()
        updateProfileUseCase = mockk(relaxed = true)
        manageProfileImageUseCase = mockk(relaxed = true)
        refreshStatisticsUseCase = mockk(relaxed = true)
        getAchievementsUseCase = mockk()
        shareAchievementUseCase = mockk(relaxed = true)
        exportUserDataUseCase = mockk()
        deleteAccountUseCase = mockk(relaxed = true)
        changePasswordUseCase = mockk(relaxed = true)
        getLoginHistoryUseCase = mockk()
        getUserPreferencesUseCase = mockk()
        updatePreferencesUseCase = mockk(relaxed = true)

        // Setup default mock behaviors
        coEvery { getUserProfileUseCase() } returns flowOf(testUserProfile)
        coEvery { getProfileStatisticsUseCase() } returns flowOf(testProfileStatistics)
        coEvery { getUserPreferencesUseCase() } returns flowOf(testUserPreferences)
    }

    @Test
    fun profileIntegration_loadProfileData_displaysCorrectly() = runTest {
        // Setup
        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify profile data is loaded and displayed
        composeTestRule.onNodeWithText("Test User").assertExists()
        composeTestRule.onNodeWithText("test@example.com").assertExists()
        composeTestRule.onNodeWithText("10").assertExists() // Total goals
        composeTestRule.onNodeWithText("7").assertExists() // Completed goals

        // Verify use cases were called
        coVerify { getUserProfileUseCase() }
        coVerify { getProfileStatisticsUseCase() }
        coVerify { getUserPreferencesUseCase() }
    }

    @Test
    fun profileIntegration_refreshStatistics_callsUseCase() = runTest {
        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    viewModel = viewModel
                )
            }
        }

        // Click refresh button
        composeTestRule.onNodeWithContentDescription("Refresh profile data").performClick()

        // Verify refresh use case was called
        coVerify { refreshStatisticsUseCase() }
    }

    @Test
    fun profileIntegration_updateDisplayName_callsUseCase() = runTest {
        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    viewModel = viewModel
                )
            }
        }

        // Start editing display name
        composeTestRule.onNodeWithContentDescription("Edit name").performClick()

        // Update the name and confirm
        viewModel.updateTempDisplayName("New Name")
        composeTestRule.onNodeWithContentDescription("Confirm").performClick()

        // Verify update use case was called
        coVerify { updateProfileUseCase.updateDisplayName("New Name") }
    }

    @Test
    fun profileIntegration_toggleDarkMode_callsPreferencesUseCase() = runTest {
        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    viewModel = viewModel
                )
            }
        }

        // Toggle dark mode
        viewModel.toggleDarkMode(true)

        // Verify preferences use case was called
        coVerify { updatePreferencesUseCase.updateDarkModeEnabled(true) }
    }

    @Test
    fun profileIntegration_exportData_callsUseCase() = runTest {
        coEvery { exportUserDataUseCase() } returns "exported-data"

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    viewModel = viewModel
                )
            }
        }

        // Export user data
        viewModel.exportUserData()

        // Verify export use case was called
        coVerify { exportUserDataUseCase() }
    }

    @Test
    fun profileIntegration_deleteAccount_callsUseCase() = runTest {
        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    viewModel = viewModel
                )
            }
        }

        // Delete account with confirmation
        viewModel.deleteAccount("DELETE")

        // Verify delete use case was called
        coVerify { deleteAccountUseCase("DELETE") }
    }

    @Test
    fun profileIntegration_errorHandling_displaysErrorState() = runTest {
        // Setup error scenario
        coEvery { getUserProfileUseCase() } throws RuntimeException("Network error")

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify error state is displayed
        composeTestRule.onNodeWithText("Error loading profile").assertExists()
        composeTestRule.onNodeWithText("Retry").assertExists()
    }

    @Test
    fun profileIntegration_retryAfterError_reloadsData() = runTest {
        // Setup error scenario initially, then success
        coEvery { getUserProfileUseCase() } throws RuntimeException("Network error") andThen flowOf(testUserProfile)

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = refreshStatisticsUseCase,
            getAchievementsUseCase = getAchievementsUseCase,
            shareAchievementUseCase = shareAchievementUseCase,
            exportUserDataUseCase = exportUserDataUseCase,
            deleteAccountUseCase = deleteAccountUseCase,
            changePasswordUseCase = changePasswordUseCase,
            getLoginHistoryUseCase = getLoginHistoryUseCase,
            getUserPreferencesUseCase = getUserPreferencesUseCase,
            updatePreferencesUseCase = updatePreferencesUseCase,
            profileErrorHandler = mockk(relaxed = true)
        )

        composeTestRule.setContent {
            MountainSunriseTheme {
                ProfileScreen(
                    onNavigateBack = {},
                    onNavigateToSettings = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify error state is displayed
        composeTestRule.onNodeWithText("Error loading profile").assertExists()

        // Click retry
        composeTestRule.onNodeWithText("Retry").performClick()

        // Verify data is loaded after retry
        composeTestRule.onNodeWithText("Test User").assertExists()

        // Verify use case was called multiple times (initial + retry)
        coVerify(atLeast = 2) { getUserProfileUseCase() }
    }
}
