package io.sukhuat.dingo.data.repository

import android.net.Uri
import io.sukhuat.dingo.data.remote.FirebaseStorageService
import io.sukhuat.dingo.domain.repository.StorageRepository
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of the StorageRepository using Firebase Storage
 */
@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val firebaseStorageService: FirebaseStorageService
) : StorageRepository {

    override suspend fun uploadImage(imageUri: Uri, goalId: String?): String {
        return firebaseStorageService.uploadImage(imageUri, goalId)
    }

    override suspend fun downloadImage(imageUrl: String, file: File): Boolean {
        return firebaseStorageService.downloadImage(imageUrl, file)
    }

    override suspend fun deleteImage(imageUrl: String): Boolean {
        return firebaseStorageService.deleteImage(imageUrl)
    }
}