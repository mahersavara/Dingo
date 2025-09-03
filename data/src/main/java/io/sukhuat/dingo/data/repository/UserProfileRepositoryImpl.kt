package io.sukhuat.dingo.data.repository

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import io.sukhuat.dingo.data.mapper.ProfileMapper
import io.sukhuat.dingo.data.model.FirebaseUserProfile
import io.sukhuat.dingo.data.storage.ProfileImageStorageService
import io.sukhuat.dingo.data.storage.ProfileImageUploadResult
import io.sukhuat.dingo.domain.model.AuthCapabilities
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.repository.LoginRecord
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserProfileRepository using Firebase services
 */
@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val profileImageStorageService: ProfileImageStorageService
) : UserProfileRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val LOGIN_HISTORY_COLLECTION = "login_history"
        private const val PROFILE_IMAGES_PATH = "profile_images"
    }

    override suspend fun getUserProfile(): Flow<UserProfile> = callbackFlow {
        println("UserProfileRepositoryImpl: getUserProfile called")
        val userId = getCurrentUserId()
            ?: throw ProfileError.AuthenticationExpired

        println("UserProfileRepositoryImpl: getUserProfile - userId: $userId")

        // First, check if profile exists and create if it doesn't
        try {
            val profileSnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (!profileSnapshot.exists()) {
                println("UserProfileRepositoryImpl: getUserProfile - profile doesn't exist, creating initial profile")
                createInitialProfile(userId)
            } else {
                println("UserProfileRepositoryImpl: getUserProfile - profile exists")
            }
        } catch (e: Exception) {
            println("UserProfileRepositoryImpl: getUserProfile - error checking profile existence: ${e.message}")
            val profileError = mapFirebaseException(e)
            close(profileError)
            return@callbackFlow
        }

        val profileRef = firestore
            .collection(USERS_COLLECTION)
            .document(userId)

        println("UserProfileRepositoryImpl: getUserProfile - setting up Firestore listener")

        val listener: ListenerRegistration = profileRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("UserProfileRepositoryImpl: getUserProfile - listener error: ${error.message}")
                val profileError = mapFirebaseException(error)
                close(profileError)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    println("UserProfileRepositoryImpl: getUserProfile - snapshot received, data: ${snapshot.data}")
                    val firebaseProfile = snapshot.toObject(FirebaseUserProfile::class.java)
                    if (firebaseProfile != null) {
                        println("UserProfileRepositoryImpl: getUserProfile - parsed FirebaseUserProfile: userId=${firebaseProfile.userId}, displayName='${firebaseProfile.displayName}', email=${firebaseProfile.email}")
                        val domainProfile = ProfileMapper.toDomain(firebaseProfile)
                        println("UserProfileRepositoryImpl: getUserProfile - mapped to domain UserProfile: userId=${domainProfile.userId}, displayName='${domainProfile.displayName}', email=${domainProfile.email}")
                        trySend(domainProfile)
                    } else {
                        println("UserProfileRepositoryImpl: getUserProfile - failed to parse FirebaseUserProfile from snapshot")
                        close(ProfileError.DataCorruption("profile_data"))
                    }
                } catch (e: Exception) {
                    println("UserProfileRepositoryImpl: getUserProfile - exception parsing snapshot: ${e.message}")
                    e.printStackTrace()
                    close(ProfileError.DataCorruption("profile_parsing"))
                }
            } else {
                println("UserProfileRepositoryImpl: getUserProfile - snapshot is null or doesn't exist")
                // This shouldn't happen since we created the profile above
                close(ProfileError.DataCorruption("profile_missing"))
            }
        }

        awaitClose {
            println("UserProfileRepositoryImpl: getUserProfile - removing Firestore listener")
            listener.remove()
        }
    }

    override suspend fun updateDisplayName(name: String) {
        try {
            println("UserProfileRepositoryImpl: updateDisplayName called with: '$name'")

            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            println("UserProfileRepositoryImpl: Current userId: $userId")

            if (name.isBlank()) {
                throw ProfileError.ValidationError("displayName", "Display name cannot be empty")
            }

            val profileRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)

            println("UserProfileRepositoryImpl: Updating Firestore document")

            // Update with camelCase field name matching our model
            val updates = hashMapOf<String, Any?>(
                "displayName" to name,
                "display_name" to com.google.firebase.firestore.FieldValue.delete() // Remove the old snake_case field that's causing conflicts
            )

            profileRef.update(updates).await()

            println("UserProfileRepositoryImpl: Firestore update successful")

            // Also update Firebase Auth profile
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                println("UserProfileRepositoryImpl: Updating Firebase Auth profile")
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                currentUser.updateProfile(profileUpdates).await()
                println("UserProfileRepositoryImpl: Firebase Auth profile update successful")
            } else {
                println("UserProfileRepositoryImpl: Warning - No current Firebase Auth user, skipping Auth profile update")
            }
        } catch (e: ProfileError) {
            println("UserProfileRepositoryImpl: ProfileError: ${e.message}")
            throw e
        } catch (e: Exception) {
            println("UserProfileRepositoryImpl: Exception: ${e.message}")
            e.printStackTrace()
            throw mapFirebaseException(e)
        }
    }

    override suspend fun updateProfileImage(imageUri: Uri): String {
        println("UserProfileRepositoryImpl: updateProfileImage called with URI: $imageUri")
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            println("UserProfileRepositoryImpl: User authenticated - userId=$userId")

            // Use Firebase Storage for profile image upload
            println("UserProfileRepositoryImpl: Uploading image to Firebase Storage")
            val uploadResult = profileImageStorageService.uploadProfileImage(userId, imageUri)

            when (uploadResult) {
                is ProfileImageUploadResult.Success -> {
                    println("UserProfileRepositoryImpl: Firebase Storage upload successful")
                    val imageUrl = uploadResult.originalImageUrl

                    // Update profile with new image URL
                    val profileRef = firestore
                        .collection(USERS_COLLECTION)
                        .document(userId)

                    val updateMap = mapOf(
                        "profileImageUrl" to imageUrl,
                        "has_custom_image" to true,
                        "last_image_update" to com.google.firebase.Timestamp.now()
                    )

                    println("UserProfileRepositoryImpl: Updating Firestore profile document")
                    try {
                        profileRef.update(updateMap).await()
                        println("UserProfileRepositoryImpl: Firestore profile update successful")
                    } catch (e: Exception) {
                        println("UserProfileRepositoryImpl: ERROR updating Firestore profile: ${e.message}")
                        throw mapFirebaseException(e)
                    }

                    // Update Firebase Auth profile (non-critical)
                    try {
                        println("UserProfileRepositoryImpl: Updating Firebase Auth profile photo")
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setPhotoUri(Uri.parse(imageUrl))
                            .build()
                        firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()
                        println("UserProfileRepositoryImpl: Firebase Auth profile photo updated")
                    } catch (e: Exception) {
                        // Auth profile update is non-critical, just log the error
                        println("UserProfileRepositoryImpl: Failed to update auth profile image (non-critical): ${e.message}")
                    }

                    println("UserProfileRepositoryImpl: Profile image update completed successfully")
                    return imageUrl
                }
                is ProfileImageUploadResult.Error -> {
                    println("UserProfileRepositoryImpl: Firebase Storage upload failed: ${uploadResult.message}")
                    throw ProfileError.StorageError("upload", Exception(uploadResult.message))
                }
            }
        } catch (e: ProfileError) {
            println("UserProfileRepositoryImpl: ProfileError during upload: ${e.message}")
            throw e
        } catch (e: Exception) {
            println("UserProfileRepositoryImpl: Unexpected error during upload: ${e.message}")
            e.printStackTrace()
            throw mapFirebaseException(e)
        }
    }

    override suspend fun deleteProfileImage() {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            // Delete all image variants using the storage service
            val deletionSuccess = profileImageStorageService.deleteProfileImages(userId)

            if (!deletionSuccess) {
                println("Warning: Some profile images may not have been deleted from storage")
            }

            // Update profile to remove custom image flags and URLs
            val profileRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)

            val updateMap = mapOf(
                "profileImageUrl" to null,
                "has_custom_image" to false,
                "last_image_update" to com.google.firebase.Timestamp.now()
            )

            profileRef.update(updateMap).await()

            // Update Firebase Auth profile (keep Google photo if available)
            try {
                val user = firebaseAuth.currentUser
                val googlePhotoUrl = getGooglePhotoUrlFromUser(user)

                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setPhotoUri(googlePhotoUrl?.let { Uri.parse(it) })
                    .build()
                user?.updateProfile(profileUpdates)?.await()
            } catch (e: Exception) {
                println("Failed to update auth profile: ${e.message}")
            }
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.StorageError("delete", e)
        }
    }

    override suspend fun exportUserData(): String {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            // Get user profile
            val profileSnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            // Get user goals (if they exist)
            val goalsSnapshot = firestore
                .collection("goals")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Get user preferences
            val preferencesSnapshot = firestore
                .collection("user_preferences")
                .document(userId)
                .get()
                .await()

            // Create JSON export
            val exportData = JSONObject().apply {
                put("userId", userId)
                put("exportDate", System.currentTimeMillis())

                // Convert profile data
                val profileData = JSONObject()
                profileSnapshot.data?.forEach { entry ->
                    profileData.put(entry.key, entry.value)
                }
                put("profile", profileData)

                // Convert goals data
                val goalsArray = goalsSnapshot.documents.map { document ->
                    val goalData = JSONObject()
                    document.data?.forEach { entry ->
                        goalData.put(entry.key, entry.value)
                    }
                    goalData
                }
                put("goals", goalsArray)

                // Convert preferences data
                val preferencesData = JSONObject()
                preferencesSnapshot.data?.forEach { entry ->
                    preferencesData.put(entry.key, entry.value)
                }
                put("preferences", preferencesData)
            }

            return exportData.toString(2)
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    override suspend fun deleteUserAccount() {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            // Delete user data from Firestore
            val batch = firestore.batch()

            // Delete profile
            val profileRef = firestore.collection(USERS_COLLECTION).document(userId)
            batch.delete(profileRef)

            // Delete goals
            val goalsSnapshot = firestore
                .collection("goals")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            goalsSnapshot.documents.forEach { document ->
                batch.delete(document.reference)
            }

            // Delete preferences
            val preferencesRef = firestore.collection("user_preferences").document(userId)
            batch.delete(preferencesRef)

            // Delete statistics
            val statisticsRef = firestore.collection("user_statistics").document(userId)
            batch.delete(statisticsRef)

            batch.commit().await()

            // Delete profile image from storage
            try {
                storage.reference
                    .child(PROFILE_IMAGES_PATH)
                    .child(userId)
                    .listAll()
                    .await()
                    .items
                    .forEach { it.delete().await() }
            } catch (e: Exception) {
                // Images might not exist, continue with account deletion
            }

            // Finally delete Firebase Auth account
            firebaseAuth.currentUser?.delete()?.await()
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    override suspend fun getLoginHistory(): List<LoginRecord> {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(LOGIN_HISTORY_COLLECTION)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .await()

            return snapshot.documents.mapNotNull { document ->
                try {
                    LoginRecord(
                        timestamp = document.getLong("timestamp") ?: 0L,
                        deviceInfo = document.getString("deviceInfo") ?: "Unknown",
                        ipAddress = document.getString("ipAddress"),
                        location = document.getString("location")
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw ProfileError.UnknownError(e)
        }
    }

    override suspend fun updateGooglePhotoUrl(photoUrl: String?) {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            // Update profile with Google photo URL
            val profileRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)

            profileRef.update("google_photo_url", photoUrl).await()

            // Also update Firebase Auth profile if setting a new photo
            photoUrl?.let { url ->
                try {
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(url))
                        .build()
                    firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()
                } catch (e: Exception) {
                    println("Failed to update auth profile with Google photo: ${e.message}")
                }
            }
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    override suspend fun getAuthCapabilities(): AuthCapabilities {
        try {
            val user = firebaseAuth.currentUser
                ?: throw ProfileError.AuthenticationExpired

            val providerIds = user.providerData.map { it.providerId }
            val hasGoogleAuth = providerIds.contains("google.com")
            val hasPasswordAuth = providerIds.contains("password") || providerIds.contains("firebase")

            // Can change password only if user has password authentication
            val canChangePassword = hasPasswordAuth && !user.email.isNullOrEmpty()

            return AuthCapabilities(
                hasGoogleAuth = hasGoogleAuth,
                hasPasswordAuth = hasPasswordAuth,
                canChangePassword = canChangePassword
            )
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        try {
            val user = firebaseAuth.currentUser
                ?: throw ProfileError.AuthenticationExpired

            // Check if user can change password
            val authCapabilities = getAuthCapabilities()
            if (!authCapabilities.canChangePassword) {
                throw ProfileError.ValidationError(
                    "password",
                    "Password change not available for this account type"
                )
            }

            if (currentPassword.isBlank() || newPassword.isBlank()) {
                throw ProfileError.ValidationError("password", "Password cannot be empty")
            }

            if (newPassword.length < 6) {
                throw ProfileError.ValidationError("password", "Password must be at least 6 characters")
            }

            // Validate password strength
            if (!isPasswordStrong(newPassword)) {
                throw ProfileError.ValidationError(
                    "password",
                    "Password must contain at least one uppercase letter, one lowercase letter, and one number"
                )
            }

            // Re-authenticate user with current password
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential).await()

            // Update password
            user.updatePassword(newPassword).await()
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            when {
                e.message?.contains("wrong-password") == true -> {
                    throw ProfileError.ValidationError("currentPassword", "Current password is incorrect")
                }
                e.message?.contains("weak-password") == true -> {
                    throw ProfileError.ValidationError("newPassword", "Password is too weak")
                }
                e.message?.contains("requires-recent-login") == true -> {
                    throw ProfileError.ValidationError("auth", "Please sign out and sign in again to change your password")
                }
                else -> throw ProfileError.UnknownError(e)
            }
        }
    }

    private fun getCurrentUserId(): String? {
        return firebaseAuth.currentUser?.uid
    }

    private fun isPasswordStrong(password: String): Boolean {
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        return hasUpperCase && hasLowerCase && hasDigit && password.length >= 8
    }

    private suspend fun createInitialProfile(userId: String) {
        try {
            val user = firebaseAuth.currentUser ?: return

            // Determine auth provider and capabilities
            val providerIds = user.providerData.map { it.providerId }
            val hasGoogleAuth = providerIds.contains("google.com")
            val hasPasswordAuth = providerIds.contains("password") || providerIds.contains("firebase")

            val authProvider = when {
                hasGoogleAuth && hasPasswordAuth -> "MULTIPLE"
                hasGoogleAuth -> "GOOGLE"
                hasPasswordAuth -> "EMAIL_PASSWORD"
                else -> "EMAIL_PASSWORD" // Fallback
            }

            // Extract Google photo URL if available
            val googlePhotoUrl = getGooglePhotoUrlFromUser(user)

            val initialProfile = FirebaseUserProfile(
                userId = userId,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                profileImageUrl = null, // Start with no custom image
                googlePhotoUrl = googlePhotoUrl,
                hasCustomImage = false,
                lastImageUpdate = null,
                joinDate = com.google.firebase.Timestamp.now(),
                isEmailVerified = user.isEmailVerified,
                authProvider = authProvider,
                hasGoogleAuth = hasGoogleAuth,
                hasPasswordAuth = hasPasswordAuth,
                canChangePassword = hasPasswordAuth && !user.email.isNullOrEmpty(),
                lastLoginDate = com.google.firebase.Timestamp.now()
            )

            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .set(initialProfile)
                .await()
        } catch (e: Exception) {
            // Log error but don't throw to avoid breaking the flow
            println("Failed to create initial profile: ${e.message}")
        }
    }

    /**
     * Extract Google photo URL from Firebase User if available
     */
    private fun getGooglePhotoUrlFromUser(user: com.google.firebase.auth.FirebaseUser?): String? {
        user ?: return null

        // First check if user has Google provider data with photo URL
        val googleProviderData = user.providerData.find { it.providerId == "google.com" }
        googleProviderData?.photoUrl?.let { return it.toString() }

        // Fallback to general photo URL if it looks like a Google URL
        val photoUrl = user.photoUrl?.toString()
        return if (photoUrl?.contains("googleusercontent.com") == true || photoUrl?.contains("googleapis.com") == true) {
            photoUrl
        } else {
            null
        }
    }

    /**
     * Map Firebase exceptions to ProfileError types
     */
    private fun mapFirebaseException(exception: Throwable): ProfileError {
        return when {
            exception.message?.contains("PERMISSION_DENIED") == true -> ProfileError.PermissionDenied("firebase_firestore")
            exception.message?.contains("UNAUTHENTICATED") == true -> ProfileError.AuthenticationExpired
            exception.message?.contains("UNAVAILABLE") == true -> ProfileError.ServerUnavailable
            exception.message?.contains("RESOURCE_EXHAUSTED") == true -> ProfileError.RateLimitExceeded
            exception.message?.contains("QUOTA_EXCEEDED") == true -> ProfileError.QuotaExceeded("firebase_storage")
            exception.message?.contains("DATA_LOSS") == true -> ProfileError.DataCorruption("firebase_data")
            exception.message?.contains("NETWORK_ERROR") == true -> ProfileError.NetworkUnavailable
            exception.message?.contains("TIMEOUT") == true -> ProfileError.ServerUnavailable
            exception is java.net.UnknownHostException -> ProfileError.NetworkUnavailable
            exception is java.net.SocketTimeoutException -> ProfileError.ServerUnavailable
            exception is java.io.IOException -> ProfileError.NetworkUnavailable
            else -> ProfileError.UnknownError(exception)
        }
    }
}
