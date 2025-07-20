package io.sukhuat.dingo.ui.screens.profile

import android.net.Uri
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.data.repository.ProfileStatisticsRepositoryImpl
import io.sukhuat.dingo.data.repository.UserProfileRepositoryImpl
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.ProfileStatistics
import io.sukhuat.dingo.domain.model.UserPreferences
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.usecase.profile.GetProfileStatisticsUseCase
import io.sukhuat.dingo.domain.usecase.profile.GetUserProfileUseCase
import io.sukhuat.dingo.domain.usecase.profile.ManageProfileImageUseCase
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
class ProfileFirebaseIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    // Mock Firebase services
    private lateinit var mockFirebaseAuth: FirebaseAuth
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockFirebaseStorage: FirebaseStorage

    // Mock repositories
    private lateinit var mockUserProfileRepository: UserProfileRepositoryImpl
    private lateinit var mockProfileStatisticsRepository: ProfileStatisticsRepositoryImpl

    // Test data
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
        mockFirebaseAuth = mockk(relaxed = true)
        mockFirestore = mockk(relaxed = true)
        mockFirebaseStorage = mockk(relaxed = true)
        mockUserProfileRepository = mockk(relaxed = true)
        mockProfileStatisticsRepository = mockk(relaxed = true)

        // Setup default mock behaviors
        coEvery { mockUserProfileRepository.getUserProfile() } returns flowOf(testUserProfile)
        coEvery { mockProfileStatisticsRepository.getProfileStatistics() } returns flowOf(testProfileStatistics)
    }

    @Test
    fun profileFirebaseIntegration_loadUserProfile_fromFirestore() = runTest {
        // Setup
        val getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository)

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = getUserProfileUseCase,
            getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository),
            updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository),
            manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository),
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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

        // Verify Firebase repository was called
        coVerify { mockUserProfileRepository.getUserProfile() }

        // Verify UI displays Firebase data
        composeTestRule.onNodeWithText("Test User").assertExists()
        composeTestRule.onNodeWithText("test@example.com").assertExists()
    }

    @Test
    fun profileFirebaseIntegration_updateDisplayName_callsFirestore() = runTest {
        // Setup
        val updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository)
        coEvery { mockUserProfileRepository.updateDisplayName(any()) } returns Unit

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository),
            getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository),
            updateProfileUseCase = updateProfileUseCase,
            manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository),
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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

        // Simulate updating display name
        viewModel.updateTempDisplayName("New Name")
        viewModel.confirmEdit()

        // Verify Firebase repository was called with correct data
        coVerify { mockUserProfileRepository.updateDisplayName("New Name") }
    }

    @Test
    fun profileFirebaseIntegration_uploadProfileImage_callsFirebaseStorage() = runTest {
        // Setup
        val mockImageUri = mockk<Uri>()
        val manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository)
        coEvery { mockUserProfileRepository.updateProfileImage(any()) } returns "https://example.com/image.jpg"

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository),
            getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository),
            updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository),
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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

        // Simulate uploading profile image
        viewModel.uploadProfileImage(mockImageUri)

        // Verify Firebase Storage repository was called
        coVerify { mockUserProfileRepository.updateProfileImage(mockImageUri) }
    }

    @Test
    fun profileFirebaseIntegration_deleteProfileImage_callsFirebaseStorage() = runTest {
        // Setup
        val manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository)
        coEvery { mockUserProfileRepository.deleteProfileImage() } returns Unit

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository),
            getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository),
            updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository),
            manageProfileImageUseCase = manageProfileImageUseCase,
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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

        // Simulate deleting profile image
        viewModel.deleteProfileImage()

        // Verify Firebase Storage repository was called
        coVerify { mockUserProfileRepository.deleteProfileImage() }
    }

    @Test
    fun profileFirebaseIntegration_loadStatistics_fromFirestore() = runTest {
        // Setup
        val getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository)

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository),
            getProfileStatisticsUseCase = getProfileStatisticsUseCase,
            updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository),
            manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository),
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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

        // Verify Firebase repository was called
        coVerify { mockProfileStatisticsRepository.getProfileStatistics() }

        // Verify UI displays Firebase statistics data
        composeTestRule.onNodeWithText("10").assertExists() // Total goals
        composeTestRule.onNodeWithText("7").assertExists() // Completed goals
    }

    @Test
    fun profileFirebaseIntegration_handleFirebaseAuthError() = runTest {
        // Setup error scenario
        coEvery { mockUserProfileRepository.getUserProfile() } throws RuntimeException("Firebase Auth error")

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository),
            getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository),
            updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository),
            manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository),
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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
    fun profileFirebaseIntegration_handleFirestoreNetworkError() = runTest {
        // Setup network error scenario
        coEvery { mockProfileStatisticsRepository.getProfileStatistics() } throws RuntimeException("Network error")

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository),
            getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository),
            updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository),
            manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository),
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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

        // Verify error handling for network issues
        composeTestRule.onNodeWithText("Error loading profile").assertExists()
    }

    @Test
    fun profileFirebaseIntegration_retryAfterFirebaseError() = runTest {
        // Setup error then success scenario
        coEvery { mockUserProfileRepository.getUserProfile() } throws RuntimeException("Firebase error") andThen flowOf(testUserProfile)

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository),
            getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository),
            updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository),
            manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository),
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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

        // Verify error state initially
        composeTestRule.onNodeWithText("Error loading profile").assertExists()

        // Click retry
        composeTestRule.onNodeWithText("Retry").performClick()

        // Verify data loads after retry
        composeTestRule.onNodeWithText("Test User").assertExists()

        // Verify Firebase repository was called multiple times
        coVerify(atLeast = 2) { mockUserProfileRepository.getUserProfile() }
    }

    @Test
    fun profileFirebaseIntegration_offlineSupport() = runTest {
        // Setup offline scenario with cached data
        val cachedProfile = testUserProfile.copy(displayName = "Cached User")
        coEvery { mockUserProfileRepository.getUserProfile() } returns flowOf(cachedProfile)

        val viewModel = ProfileViewModel(
            getUserProfileUseCase = GetUserProfileUseCase(mockUserProfileRepository),
            getProfileStatisticsUseCase = GetProfileStatisticsUseCase(mockProfileStatisticsRepository),
            updateProfileUseCase = UpdateProfileUseCase(mockUserProfileRepository),
            manageProfileImageUseCase = ManageProfileImageUseCase(mockUserProfileRepository),
            refreshStatisticsUseCase = mockk(relaxed = true),
            getAchievementsUseCase = mockk(relaxed = true),
            shareAchievementUseCase = mockk(relaxed = true),
            exportUserDataUseCase = mockk(relaxed = true),
            deleteAccountUseCase = mockk(relaxed = true),
            changePasswordUseCase = mockk(relaxed = true),
            getLoginHistoryUseCase = mockk(relaxed = true),
            getUserPreferencesUseCase = mockk { coEvery { invoke() } returns flowOf(testUserPreferences) },
            updatePreferencesUseCase = mockk(relaxed = true),
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

        // Verify cached data is displayed when offline
        composeTestRule.onNodeWithText("Cached User").assertExists()

        // Verify Firebase repository was called (would return cached data)
        coVerify { mockUserProfileRepository.getUserProfile() }
    }
}
