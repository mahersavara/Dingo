package io.sukhuat.dingo.data.repository

import android.net.Uri
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import io.sukhuat.dingo.data.mapper.ProfileMapper
import io.sukhuat.dingo.data.model.FirebaseUserProfile
import io.sukhuat.dingo.domain.model.ProfileError
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.domain.repository.LoginRecord
import io.sukhuat.dingo.domain.repository.UserProfileRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of UserProfileRepository using Firebase services
 */
@Singleton
class UserProfileRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : UserProfileRepository {

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val LOGIN_HISTORY_COLLECTION = "login_history"
        private const val PROFILE_IMAGES_PATH = "profile_images"
    }

    override suspend fun getUserProfile(): Flow<UserProfile> = callbackFlow {
        val userId = getCurrentUserId()
            ?: throw ProfileError.AuthenticationExpired

        // First, check if profile exists and create if it doesn't
        try {
            val profileSnapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (!profileSnapshot.exists()) {
                createInitialProfile(userId)
            }
        } catch (e: Exception) {
            val profileError = mapFirebaseException(e)
            close(profileError)
            return@callbackFlow
        }

        val profileRef = firestore
            .collection(USERS_COLLECTION)
            .document(userId)

        val listener: ListenerRegistration = profileRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                val profileError = mapFirebaseException(error)
                close(profileError)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                try {
                    val firebaseProfile = snapshot.toObject(FirebaseUserProfile::class.java)
                    if (firebaseProfile != null) {
                        val domainProfile = ProfileMapper.toDomain(firebaseProfile)
                        trySend(domainProfile)
                    } else {
                        close(ProfileError.DataCorruption("profile_data"))
                    }
                } catch (e: Exception) {
                    close(ProfileError.DataCorruption("profile_parsing"))
                }
            } else {
                // This shouldn't happen since we created the profile above
                close(ProfileError.DataCorruption("profile_missing"))
            }
        }

        awaitClose { listener.remove() }
    }

    override suspend fun updateDisplayName(name: String) {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            if (name.isBlank()) {
                throw ProfileError.ValidationError("displayName", "Display name cannot be empty")
            }

            val profileRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)

            profileRef.update("display_name", name).await()

            // Also update Firebase Auth profile
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    override suspend fun updateProfileImage(imageUri: Uri): String {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            // Upload image to Firebase Storage with error handling
            val imageRef = storage.reference
                .child(PROFILE_IMAGES_PATH)
                .child(userId)
                .child("${UUID.randomUUID()}.jpg")

            val uploadTask = try {
                imageRef.putFile(imageUri).await()
            } catch (e: Exception) {
                throw when {
                    e.message?.contains("quota") == true -> ProfileError.QuotaExceeded("storage")
                    e.message?.contains("permission") == true -> ProfileError.PermissionDenied("storage")
                    e.message?.contains("network") == true -> ProfileError.NetworkUnavailable
                    else -> ProfileError.StorageError("upload", e)
                }
            }

            val downloadUrl = try {
                uploadTask.storage.downloadUrl.await().toString()
            } catch (e: Exception) {
                throw ProfileError.StorageError("get_download_url", e)
            }

            // Update profile with new image URL
            val profileRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)

            try {
                profileRef.update("profile_image_url", downloadUrl).await()
            } catch (e: Exception) {
                // If profile update fails, try to clean up the uploaded image
                try {
                    imageRef.delete().await()
                } catch (cleanupException: Exception) {
                    // Log cleanup failure but don't throw
                }
                throw mapFirebaseException(e)
            }

            // Also update Firebase Auth profile (non-critical, don't fail if this fails)
            try {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(downloadUrl))
                    .build()
                firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()
            } catch (e: Exception) {
                // Auth profile update is non-critical, just log the error
                println("Failed to update auth profile image: ${e.message}")
            }

            return downloadUrl
        } catch (e: ProfileError) {
            throw e
        } catch (e: Exception) {
            throw mapFirebaseException(e)
        }
    }

    override suspend fun deleteProfileImage() {
        try {
            val userId = getCurrentUserId()
                ?: throw ProfileError.AuthenticationExpired

            // Get current profile to find image URL
            val profileRef = firestore
                .collection(USERS_COLLECTION)
                .document(userId)

            val snapshot = profileRef.get().await()
            val currentImageUrl = snapshot.getString("profile_image_url")

            // Delete from Storage if exists
            currentImageUrl?.let { url ->
                try {
                    storage.getReferenceFromUrl(url).delete().await()
                } catch (e: Exception) {
                    // Image might not exist in storage, continue with profile update
                }
            }

            // Update profile to remove image URL
            profileRef.update("profile_image_url", null).await()

            // Also update Firebase Auth profile
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setPhotoUri(null)
                .build()
            firebaseAuth.currentUser?.updateProfile(profileUpdates)?.await()
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

    override suspend fun changePassword(currentPassword: String, newPassword: String) {
        try {
            val user = firebaseAuth.currentUser
                ?: throw ProfileError.AuthenticationExpired

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
            throw ProfileError.UnknownError(e)
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

            val initialProfile = FirebaseUserProfile(
                userId = userId,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                profileImageUrl = user.photoUrl?.toString(),
                joinDate = com.google.firebase.Timestamp.now(),
                isEmailVerified = user.isEmailVerified,
                authProvider = when {
                    user.providerData.any { it.providerId == "google.com" } -> "GOOGLE"
                    else -> "EMAIL_PASSWORD"
                },
                lastLoginDate = com.google.firebase.Timestamp.now()
            )

            firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .set(initialProfile)
                .await()
        } catch (e: Exception) {
            // Log error but don't throw to avoid breaking the flow
            // In a real app, you might want to use a proper logging framework
            println("Failed to create initial profile: ${e.message}")
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
