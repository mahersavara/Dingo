package io.sukhuat.dingo.domain.usecase.preferences

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for managing notification permissions and settings
 */
class ManageNotificationPermissionsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Check if the app has notification permission
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Check if notifications are enabled at the system level
     */
    fun areNotificationsEnabled(): Boolean {
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Get an intent to open the app's notification settings
     */
    fun getNotificationSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
    }

    /**
     * Get notification permission status with detailed information
     */
    fun getNotificationPermissionStatus(): NotificationPermissionStatus {
        val hasPermission = hasNotificationPermission()
        val areEnabled = areNotificationsEnabled()

        return when {
            !hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                NotificationPermissionStatus.PERMISSION_DENIED
            }
            !areEnabled -> {
                NotificationPermissionStatus.NOTIFICATIONS_DISABLED
            }
            else -> {
                NotificationPermissionStatus.GRANTED
            }
        }
    }
}

/**
 * Enum representing different notification permission states
 */
enum class NotificationPermissionStatus {
    GRANTED,
    PERMISSION_DENIED,
    NOTIFICATIONS_DISABLED
}
