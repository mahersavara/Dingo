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
import io.sukhuat.dingo.domain.model.LoginRecord
import io.sukhuat.dingo.domain.model.UserProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDateTime

@RunWith(AndroidJUnit4::class)
class AccountSecurityTest {

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

    private val testLoginHistory = listOf(
        LoginRecord(
            timestamp = LocalDateTime.of(2024, 7, 18, 10, 0),
            deviceInfo = "Android Phone",
            location = "New York, NY",
            ipAddress = "192.168.1.1"
        ),
        LoginRecord(
            timestamp = LocalDateTime.of(2024, 7, 17, 15, 30),
            deviceInfo = "Web Browser",
            location = "New York, NY",
            ipAddress = "192.168.1.1"
        )
    )

    @Test
    fun accountSecurity_displaysPasswordSection_forEmailAuth() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = false,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Verify password section is displayed for email auth
        composeTestRule.onNodeWithText("Password & Security").assertIsDisplayed()
        composeTestRule.onNodeWithText("Change Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Change Password").assertHasClickAction()
    }

    @Test
    fun accountSecurity_hidesPasswordSection_forGoogleAuth() {
        val googleProfile = testUserProfile.copy(authProvider = AuthProvider.GOOGLE)

        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = googleProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = false,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Verify password section is hidden for Google auth
        composeTestRule.onNodeWithText("Change Password").assertDoesNotExist()
        composeTestRule.onNodeWithText("Connected Accounts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Google").assertIsDisplayed()
    }

    @Test
    fun accountSecurity_displaysLoginHistory() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = false,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Verify login history is displayed
        composeTestRule.onNodeWithText("Login History").assertIsDisplayed()
        composeTestRule.onNodeWithText("Android Phone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Web Browser").assertIsDisplayed()
        composeTestRule.onNodeWithText("New York, NY").assertIsDisplayed()
    }

    @Test
    fun accountSecurity_changePasswordDialog_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = true,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Verify password change dialog is displayed
        composeTestRule.onNodeWithText("Change Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Current Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm New Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Update Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun accountSecurity_changePasswordDialog_triggersCallback() {
        var currentPassword = ""
        var newPassword = ""

        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = true,
                    passwordChangeError = null,
                    onChangePassword = { current, new ->
                        currentPassword = current
                        newPassword = new
                    },
                    onDeleteAccount = { }
                )
            }
        }

        // Enter passwords and submit
        composeTestRule.onNodeWithText("Current Password").performTextInput("oldpass123")
        composeTestRule.onNodeWithText("New Password").performTextInput("newpass123")
        composeTestRule.onNodeWithText("Confirm New Password").performTextInput("newpass123")
        composeTestRule.onNodeWithText("Update Password").performClick()

        // Verify callback was triggered with correct values
        assert(currentPassword == "oldpass123")
        assert(newPassword == "newpass123")
    }

    @Test
    fun accountSecurity_passwordChangeError_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = true,
                    passwordChangeError = "Current password is incorrect",
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Current password is incorrect").assertIsDisplayed()
    }

    @Test
    fun accountSecurity_deleteAccountSection_displaysCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = false,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Verify delete account section is displayed
        composeTestRule.onNodeWithText("Danger Zone").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Permanently delete your account and all data").assertIsDisplayed()
        composeTestRule.onNodeWithText("Delete Account").assertHasClickAction()
    }

    @Test
    fun accountSecurity_deleteAccount_triggersCallback() {
        var deleteTriggered = false

        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = false,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { deleteTriggered = true }
                )
            }
        }

        // Click delete account button
        composeTestRule.onNodeWithText("Delete Account").performClick()

        // Verify callback was triggered
        assert(deleteTriggered)
    }

    @Test
    fun accountSecurity_hasAccessibilitySupport() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = false,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Verify accessibility content descriptions are present
        composeTestRule.onNodeWithContentDescription("Change password").assertExists()
        composeTestRule.onNodeWithContentDescription("Delete account").assertExists()
        composeTestRule.onNodeWithContentDescription("Login history").assertExists()
    }

    @Test
    fun accountSecurity_emptyLoginHistory_displaysMessage() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = emptyList(),
                    isChangingPassword = false,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Verify empty state message is displayed
        composeTestRule.onNodeWithText("No login history available").assertIsDisplayed()
    }

    @Test
    fun accountSecurity_passwordValidation_worksCorrectly() {
        composeTestRule.setContent {
            MountainSunriseTheme {
                AccountSecurity(
                    profile = testUserProfile,
                    loginHistory = testLoginHistory,
                    isChangingPassword = true,
                    passwordChangeError = null,
                    onChangePassword = { _, _ -> },
                    onDeleteAccount = { }
                )
            }
        }

        // Enter mismatched passwords
        composeTestRule.onNodeWithText("New Password").performTextInput("newpass123")
        composeTestRule.onNodeWithText("Confirm New Password").performTextInput("differentpass")

        // Verify update button is disabled for mismatched passwords
        composeTestRule.onNodeWithText("Update Password").assertIsNotEnabled()

        // Enter matching passwords
        composeTestRule.onNodeWithText("Confirm New Password").performTextInput("newpass123")

        // Verify update button is enabled for matching passwords
        composeTestRule.onNodeWithText("Update Password").assertIsEnabled()
    }
}
