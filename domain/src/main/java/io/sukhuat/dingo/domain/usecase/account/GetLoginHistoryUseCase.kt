package io.sukhuat.dingo.domain.usecase.account

import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.LoginRecord
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Use case for retrieving user login history for security tracking
 */
class GetLoginHistoryUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * Get user login history with formatted information
     * * @return List of formatted login records
     * @throws ProfileError.AuthenticationExpired if user is not authenticated
     * @throws ProfileError.UnknownError if retrieval fails
     */
    suspend operator fun invoke(): List<FormattedLoginRecord> {
        return try {
            val loginRecords = userProfileRepository.getLoginHistory()
            loginRecords.map { record ->
                formatLoginRecord(record)
            }
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    /**
     * Get login history summary for security overview
     */
    suspend fun getLoginSummary(): LoginSummary {
        return try {
            val loginRecords = userProfileRepository.getLoginHistory()

            val totalLogins = loginRecords.size
            val uniqueDevices = loginRecords.map { it.deviceInfo }.distinct().size
            val uniqueLocations = loginRecords.mapNotNull { it.location }.distinct().size

            val lastLogin = loginRecords.maxByOrNull { it.timestamp }
            val lastLoginFormatted = lastLogin?.let { record ->
                val date = Date(record.timestamp)
                val formatter = SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
                formatter.format(date)
            }

            // Check for suspicious activity (multiple locations in short time)
            val dayAgoInMillis = System.currentTimeMillis() - (24 * 60 * 60 * 1000L)
            val recentLogins = loginRecords.filter {
                it.timestamp > dayAgoInMillis
            }

            val suspiciousActivity = recentLogins
                .mapNotNull { it.location }
                .distinct()
                .size > 2 // More than 2 different locations in 24 hours

            LoginSummary(
                totalLogins = totalLogins,
                uniqueDevices = uniqueDevices,
                uniqueLocations = uniqueLocations,
                lastLogin = lastLoginFormatted,
                hasSuspiciousActivity = suspiciousActivity,
                recentLoginsCount = recentLogins.size
            )
        } catch (e: Exception) {
            LoginSummary(
                totalLogins = 0,
                uniqueDevices = 0,
                uniqueLocations = 0,
                lastLogin = null,
                hasSuspiciousActivity = false,
                recentLoginsCount = 0
            )
        }
    }

    private fun formatLoginRecord(record: LoginRecord): FormattedLoginRecord {
        val date = Date(record.timestamp)

        val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        val formattedDate = dateFormatter.format(date)
        val formattedTime = timeFormatter.format(date)
        val relativeTime = getRelativeTime(record.timestamp)

        return FormattedLoginRecord(
            timestamp = record.timestamp,
            formattedDate = formattedDate,
            formattedTime = formattedTime,
            relativeTime = relativeTime,
            deviceInfo = record.deviceInfo,
            ipAddress = record.ipAddress,
            location = record.location ?: "Unknown location"
        )
    }

    private fun getRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp

        val minutes = diffMillis / (60 * 1000)
        val hours = diffMillis / (60 * 60 * 1000)
        val days = diffMillis / (24 * 60 * 60 * 1000)

        return when {
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "Just now"
        }
    }
}

/**
 * Formatted login record for UI display
 */
data class FormattedLoginRecord(
    val timestamp: Long,
    val formattedDate: String,
    val formattedTime: String,
    val relativeTime: String,
    val deviceInfo: String,
    val ipAddress: String?,
    val location: String
)

/**
 * Summary of login activity for security overview
 */
data class LoginSummary(
    val totalLogins: Int,
    val uniqueDevices: Int,
    val uniqueLocations: Int,
    val lastLogin: String?,
    val hasSuspiciousActivity: Boolean,
    val recentLoginsCount: Int
)
