package io.sukhuat.dingo.domain.usecase.account

import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case for exporting user data for GDPR compliance
 */
class ExportUserDataUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {

    /**
     * Export all user data in JSON format
     * * @return JSON string containing all user data
     * @throws ProfileError.AuthenticationExpired if user is not authenticated
     * @throws ProfileError.UnknownError if export fails
     */
    suspend operator fun invoke(): String {
        return try {
            userProfileRepository.exportUserData()
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    /**
     * Get estimated data size for export preview
     * This helps users understand what data will be exported
     */
    suspend fun getEstimatedDataSize(): DataExportInfo {
        return try {
            val exportData = userProfileRepository.exportUserData()
            val sizeInBytes = exportData.toByteArray().size
            val sizeInKB = sizeInBytes / 1024.0

            DataExportInfo(
                estimatedSizeKB = sizeInKB,
                includesProfile = true,
                includesGoals = true,
                includesPreferences = true,
                includesStatistics = true
            )
        } catch (e: Exception) {
            DataExportInfo(
                estimatedSizeKB = 0.0,
                includesProfile = false,
                includesGoals = false,
                includesPreferences = false,
                includesStatistics = false
            )
        }
    }
}

/**
 * Information about data export
 */
data class DataExportInfo(
    val estimatedSizeKB: Double,
    val includesProfile: Boolean,
    val includesGoals: Boolean,
    val includesPreferences: Boolean,
    val includesStatistics: Boolean
)
