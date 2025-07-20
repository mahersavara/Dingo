package io.sukhuat.dingo.domain.usecase.profile

import android.net.Uri
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import javax.inject.Inject

/**
 * Use case for managing profile image upload/delete operations
 */
class ManageProfileImageUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    /**
     * Upload a new profile image
     * @param imageUri URI of the image to upload
     * @return URL of the uploaded image
     * @throws ProfileError.ValidationError if image URI is invalid
     * @throws ProfileError.StorageError if upload fails
     * @throws ProfileError.AuthenticationExpired if user is not authenticated
     */
    suspend fun uploadProfileImage(imageUri: Uri): String {
        // Validate image URI
        validateImageUri(imageUri)

        // Upload image through repository
        return userProfileRepository.updateProfileImage(imageUri)
    }

    /**
     * Delete the current profile image
     * @throws ProfileError.StorageError if deletion fails
     * @throws ProfileError.AuthenticationExpired if user is not authenticated
     */
    suspend fun deleteProfileImage() {
        userProfileRepository.deleteProfileImage()
    }

    /**
     * Validate image URI
     * @param imageUri URI to validate
     * @throws ProfileError.ValidationError if URI is invalid
     */
    private fun validateImageUri(imageUri: Uri) {
        when {
            imageUri.toString().isBlank() -> {
                throw ProfileError.ValidationError("imageUri", "Image URI cannot be empty")
            }
            imageUri.scheme == null -> {
                throw ProfileError.ValidationError("imageUri", "Invalid image URI format")
            }
            !isValidImageScheme(imageUri.scheme) -> {
                throw ProfileError.ValidationError("imageUri", "Unsupported image URI scheme")
            }
        }
    }

    /**
     * Check if URI scheme is valid for image operations
     * @param scheme URI scheme to check
     * @return true if scheme is valid for images
     */
    private fun isValidImageScheme(scheme: String?): Boolean {
        return scheme in listOf("content", "file", "android.resource", "http", "https")
    }
}
