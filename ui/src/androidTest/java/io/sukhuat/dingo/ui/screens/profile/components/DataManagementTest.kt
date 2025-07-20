package io.sukhuat.dingo.ui.screens.profile.components

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class DataManagementTest {

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

    @Test
    fun dataManagement_displaysDataExportSection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify data export section is displayed
        composeTestRule.onNodeWithText("Data Management").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export Your Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Download a copy of your goals, preferences, and profile data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export Data").assertHasClickAction()
    }

    @Test
    fun dataManagement_exportData_triggersCallback() {
        var exportTriggered = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = { exportTriggered = true },
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Click export data button
        composeTestRule.onNodeWithText("Export Data").performClick()

        // Verify callback was triggered
        assert(exportTriggered)
    }

    @Test
    fun dataManagement_exportInProgress_showsLoadingState() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = true,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify loading state is displayed
        composeTestRule.onNodeWithText("Exporting...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Export Data").assertIsNotEnabled()
    }

    @Test
    fun dataManagement_exportError_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = "Failed to export data. Please try again.",
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Failed to export data. Please try again.").assertIsDisplayed()
    }

    @Test
    fun dataManagement_displaysPrivacySection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify privacy section is displayed
        composeTestRule.onNodeWithText("Privacy & Data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your data is stored securely and never shared without permission").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Privacy Policy").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Terms of Service").assertIsDisplayed()
    }

    @Test
    fun dataManagement_displaysAccountDeletionSection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify account deletion section is displayed
        composeTestRule.onNodeWithText("Account Deletion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Permanently delete your account and all associated data").assertIsDisplayed()
        composeTestRule.onNodeWithText("This action cannot be undone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Account").assertHasClickAction()
    }

    @Test
    fun dataManagement_deleteAccount_showsConfirmationDialog() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Click delete account button
        composeTestRule.onNodeWithText("Delete Account").performClick()

        // Verify confirmation dialog is displayed
        composeTestRule.onNodeWithText("Delete Account?").assertIsDisplayed()
        composeTestRule.onNodeWithText("This will permanently delete your account and all data. This action cannot be undone.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Type DELETE to confirm").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Forever").assertIsDisplayed()
    }

    @Test
    fun dataManagement_deleteConfirmation_requiresCorrectText() {
        var deleteTriggered = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = { deleteTriggered = true },
                    onSignOut = {}
                )
            }
        }

        // Click delete account button to show dialog
        composeTestRule.onNodeWithText("Delete Account").performClick()

        // Initially delete button should be disabled
        composeTestRule.onNodeWithText("Delete Forever").assertIsNotEnabled()

        // Enter incorrect confirmation text
        composeTestRule.onNodeWithText("Type DELETE to confirm").performTextInput("delete")
        composeTestRule.onNodeWithText("Delete Forever").assertIsNotEnabled()

        // Enter correct confirmation text
        composeTestRule.onNodeWithText("Type DELETE to confirm").performTextInput("DELETE")
        composeTestRule.onNodeWithText("Delete Forever").assertIsEnabled()

        // Click delete button
        composeTestRule.onNodeWithText("Delete Forever").performClick()

        // Verify callback was triggered
        assert(deleteTriggered)
    }

    @Test
    fun dataManagement_deleteInProgress_showsLoadingState() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = true,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify loading state is displayed
        composeTestRule.onNodeWithText("Deleting account...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Account").assertIsNotEnabled()
    }

    @Test
    fun dataManagement_deleteError_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = "Failed to delete account. Please try again.",
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Failed to delete account. Please try again.").assertIsDisplayed()
    }

    @Test
    fun dataManagement_displaysSignOutSection() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify sign out section is displayed
        composeTestRule.onNodeWithText("Session Management").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign out of your account on this device").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertHasClickAction()
    }

    @Test
    fun dataManagement_signOut_triggersCallback() {
        var signOutTriggered = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = { signOutTriggered = true }
                )
            }
        }

        // Click sign out button
        composeTestRule.onNodeWithText("Sign Out").performClick()

        // Verify callback was triggered
        assert(signOutTriggered)
    }

    @Test
    fun dataManagement_hasAccessibilitySupport() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify accessibility content descriptions are present
        composeTestRule.onNodeWithContentDescription("Export user data").assertExists()
        composeTestRule.onNodeWithContentDescription("Delete account permanently").assertExists()
        composeTestRule.onNodeWithContentDescription("Sign out of account").assertExists()
    }

    @Test
    fun dataManagement_gdprCompliance_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                DataManagement(
                    profile = testUserProfile,
                    isExporting = false,
                    exportError = null,
                    isDeleting = false,
                    deleteError = null,
                    onExportData = {},
                    onDeleteAccount = {},
                    onSignOut = {}
                )
            }
        }

        // Verify GDPR compliance information is displayed
        composeTestRule.onNodeWithText("Data Rights").assertIsDisplayed()
        composeTestRule.onNodeWithText("You have the right to access, export, and delete your personal data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Learn more about your data rights").assertIsDisplayed()
    }
}
