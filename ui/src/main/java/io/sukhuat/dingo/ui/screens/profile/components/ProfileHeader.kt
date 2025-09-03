package io.sukhuat.dingo.ui.screens.profile.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.sukhuat.dingo.common.components.DingoTextField
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.AuthProvider
import io.sukhuat.dingo.domain.model.UserProfile
import io.sukhuat.dingo.ui.screens.profile.ImageUploadState
import io.sukhuat.dingo.ui.screens.profile.ProfileEditState
import io.sukhuat.dingo.ui.screens.profile.ProfileField
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Profile header component displaying user avatar, name, email, and join date
 * with inline editing capabilities for display name and profile image
 */
@Composable
fun ProfileHeader(
    profile: UserProfile,
    editState: ProfileEditState,
    imageUploadState: ImageUploadState,
    onStartEditing: (ProfileField) -> Unit,
    onCancelEditing: () -> Unit,
    onConfirmEdit: () -> Unit,
    onUpdateTempDisplayName: (String) -> Unit,
    onUploadProfileImage: (Uri) -> Unit,
    onDeleteProfileImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    println("ProfileHeader: Component rendered - hasCustomImage=${profile.hasCustomImage}, profileImageUrl=${profile.profileImageUrl}, googlePhotoUrl=${profile.googlePhotoUrl}")

    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }

    // Temporary image URI for camera capture
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        println("ProfileHeader: Gallery launcher callback - uri=$uri")
        uri?.let {
            println("ProfileHeader: Calling onUploadProfileImage with URI: $it")
            onUploadProfileImage(it)
        } ?: run {
            println("ProfileHeader: Gallery launcher returned null URI")
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        println("ProfileHeader: Camera launcher callback - success=$success, tempImageUri=$tempImageUri")
        if (success) {
            // The image is saved to the provided URI in tempImageUri
            tempImageUri?.let {
                println("ProfileHeader: Calling onUploadProfileImage with camera URI: $it")
                onUploadProfileImage(it)
            } ?: run {
                println("ProfileHeader: ERROR - Camera successful but tempImageUri is null")
            }
        } else {
            println("ProfileHeader: Camera capture failed or was cancelled")
        }
    }

    // Create temp file for camera capture
    LaunchedEffect(Unit) {
        try {
            println("ProfileHeader: Creating temp file for camera capture")
            val tempFile = File.createTempFile(
                "profile_image_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            )
            tempImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            println("ProfileHeader: Temp URI created successfully: $tempImageUri")
        } catch (e: Exception) {
            // Handle FileProvider configuration issues gracefully
            println("ProfileHeader: ERROR - Failed to create temp URI for camera: ${e.message}")
            e.printStackTrace()
            tempImageUri = null
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Profile"
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Image Section
            ProfileImageSection(
                profileImageUrl = profile.profileImageUrl,
                googlePhotoUrl = profile.googlePhotoUrl,
                hasCustomImage = profile.hasCustomImage,
                isUploading = imageUploadState.isUploading,
                uploadProgress = imageUploadState.progress,
                onImageClick = {
                    println("ProfileHeader: Profile image clicked, showing dialog")
                    showImageSourceDialog = true
                },
                onDeleteImage = onDeleteProfileImage
            )

            // Image source selection dialog
            if (showImageSourceDialog) {
                println("ProfileHeader: Showing image source dialog")
                AlertDialog(
                    onDismissRequest = {
                        println("ProfileHeader: Image source dialog dismissed")
                        showImageSourceDialog = false
                    },
                    title = { Text("Choose Photo Source") },
                    text = { Text("Select where to get your profile photo from:") },
                    confirmButton = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    println("ProfileHeader: Camera button clicked")
                                    showImageSourceDialog = false
                                    tempImageUri?.let { uri ->
                                        println("ProfileHeader: Launching camera with URI: $uri")
                                        cameraLauncher.launch(uri)
                                    } ?: run {
                                        // Show error if FileProvider setup failed
                                        println("ProfileHeader: ERROR - Camera feature unavailable due to FileProvider configuration")
                                    }
                                },
                                enabled = tempImageUri != null
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Camera")
                            }

                            TextButton(
                                onClick = {
                                    println("ProfileHeader: Gallery button clicked")
                                    showImageSourceDialog = false
                                    println("ProfileHeader: Launching gallery picker")
                                    galleryLauncher.launch("image/*")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gallery")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                println("ProfileHeader: Cancel button clicked")
                                showImageSourceDialog = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Name Section
            DisplayNameSection(
                displayName = profile.displayName,
                editState = editState,
                onStartEditing = { onStartEditing(ProfileField.DISPLAY_NAME) },
                onCancelEditing = onCancelEditing,
                onConfirmEdit = onConfirmEdit,
                onUpdateTempDisplayName = onUpdateTempDisplayName
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email Section
            EmailSection(
                email = profile.email,
                isEmailVerified = profile.isEmailVerified,
                authProvider = profile.authProvider
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Join Date Section
            JoinDateSection(joinDate = profile.joinDate)
        }
    }
}

/**
 * Profile image section with upload functionality
 */
@Composable
private fun ProfileImageSection(
    profileImageUrl: String?,
    googlePhotoUrl: String?,
    hasCustomImage: Boolean,
    isUploading: Boolean,
    uploadProgress: Float,
    onImageClick: () -> Unit,
    onDeleteImage: () -> Unit
) {
    // Debug: prioritize profileImageUrl to test if the URL is actually there
    val imageUrl = when {
        !profileImageUrl.isNullOrEmpty() -> profileImageUrl // Show any profileImageUrl regardless of hasCustomImage flag
        !googlePhotoUrl.isNullOrEmpty() -> googlePhotoUrl
        else -> null
    }

    println("ProfileImageSection: hasCustomImage=$hasCustomImage, profileImageUrl=$profileImageUrl, googlePhotoUrl=$googlePhotoUrl, selectedImageUrl=$imageUrl")

    Box(
        contentAlignment = Alignment.Center
    ) {
        // Profile Image
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(3.dp, RusticGold, CircleShape)
                .clickable { onImageClick() },
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile picture",
                    tint = RusticGold,
                    modifier = Modifier.size(60.dp)
                )
            }

            // Upload overlay
            if (isUploading) {
                Surface(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { uploadProgress },
                            color = RusticGold,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        }

        // Camera icon overlay
        if (!isUploading) {
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape),
                color = RusticGold,
                shadowElevation = 4.dp
            ) {
                IconButton(
                    onClick = onImageClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change profile picture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Upload progress bar
    AnimatedVisibility(
        visible = isUploading,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            LinearProgressIndicator(
                progress = { uploadProgress },
                color = RusticGold,
                modifier = Modifier
                    .width(120.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
            Text(
                text = "Uploading... ${(uploadProgress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .semantics {
                        contentDescription = "Uploading profile picture: ${(uploadProgress * 100).toInt()} percent complete"
                    }
            )
        }
    }
}

@Composable
private fun DisplayNameSection(
    displayName: String,
    editState: ProfileEditState,
    onStartEditing: () -> Unit,
    onCancelEditing: () -> Unit,
    onConfirmEdit: () -> Unit,
    onUpdateTempDisplayName: (String) -> Unit
) {
    println("ProfileHeader: DisplayNameSection - displayName='$displayName', isEditing=${editState.isEditing}, editingField=${editState.editingField}, tempDisplayName='${editState.tempDisplayName}'")

    val isEditingName = editState.isEditing && editState.editingField == ProfileField.DISPLAY_NAME

    println("ProfileHeader: DisplayNameSection - isEditingName=$isEditingName")

    if (isEditingName) {
        println("ProfileHeader: DisplayNameSection - Rendering editing mode")
        // Editing mode
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DingoTextField(
                value = editState.tempDisplayName,
                onValueChange = { newValue ->
                    println("ProfileHeader: DisplayNameSection - DingoTextField onValueChange called with: '$newValue'")
                    onUpdateTempDisplayName(newValue)
                },
                placeholder = "Enter display name",
                isError = editState.validationError != null,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = "Edit display name"
                    }
            )

            // Confirm button
            IconButton(
                onClick = {
                    println("ProfileHeader: DisplayNameSection - Confirm button clicked")
                    onConfirmEdit()
                },
                enabled = !editState.isValidating && editState.tempDisplayName.isNotBlank(),
                modifier = Modifier.semantics {
                    contentDescription = "Confirm name change"
                }
            ) {
                if (editState.isValidating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = RusticGold,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = RusticGold
                    )
                }
            }

            // Cancel button
            IconButton(
                onClick = {
                    println("ProfileHeader: DisplayNameSection - Cancel button clicked")
                    onCancelEditing()
                },
                enabled = !editState.isValidating,
                modifier = Modifier.semantics {
                    contentDescription = "Cancel editing"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Show validation error
        if (editState.validationError != null) {
            println("ProfileHeader: DisplayNameSection - Showing validation error: ${editState.validationError}")
            Text(
                text = editState.validationError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .semantics {
                        contentDescription = "Error: ${editState.validationError}"
                    }
            )
        }
    } else {
        println("ProfileHeader: DisplayNameSection - Rendering display mode")
        // Display mode
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = displayName.ifBlank { "No name set" },
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    contentDescription = "Display name: $displayName"
                }
            )

            IconButton(
                onClick = {
                    println("ProfileHeader: DisplayNameSection - Edit button clicked, calling onStartEditing")
                    onStartEditing()
                },
                modifier = Modifier
                    .size(32.dp)
                    .semantics {
                        contentDescription = "Edit display name"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = RusticGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Email section with verification status
 */
@Composable
private fun EmailSection(
    email: String,
    isEmailVerified: Boolean,
    authProvider: AuthProvider
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )

        // Verification badge
        if (isEmailVerified) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RusticGold.copy(alpha = 0.1f),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "Verified",
                    style = MaterialTheme.typography.labelSmall,
                    color = RusticGold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Auth provider indicator
        if (authProvider == AuthProvider.GOOGLE) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Text(
                    text = "Google",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

/**
 * Join date section with formatted date
 */
@Composable
private fun JoinDateSection(joinDate: Long) {
    val formattedDate = remember(joinDate) {
        val date = Date(joinDate)
        val formatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        formatter.format(date)
    }

    Text(
        text = "Member since $formattedDate",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileHeaderPreview() {
    MountainSunriseTheme {
        ProfileHeader(
            profile = UserProfile(
                userId = "123",
                displayName = "John Doe",
                email = "john.doe@example.com",
                profileImageUrl = null,
                joinDate = System.currentTimeMillis() - (6 * 30 * 24 * 60 * 60 * 1000L),
                isEmailVerified = true,
                authProvider = AuthProvider.EMAIL_PASSWORD,
                lastLoginDate = System.currentTimeMillis()
            ),
            editState = ProfileEditState(),
            imageUploadState = ImageUploadState(),
            onStartEditing = {},
            onCancelEditing = {},
            onConfirmEdit = {},
            onUpdateTempDisplayName = {},
            onUploadProfileImage = {},
            onDeleteProfileImage = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileHeaderEditingPreview() {
    MountainSunriseTheme {
        ProfileHeader(
            profile = UserProfile(
                userId = "123",
                displayName = "John Doe",
                email = "john.doe@example.com",
                profileImageUrl = null,
                joinDate = System.currentTimeMillis() - (6 * 30 * 24 * 60 * 60 * 1000L),
                isEmailVerified = true,
                authProvider = AuthProvider.GOOGLE,
                lastLoginDate = System.currentTimeMillis()
            ),
            editState = ProfileEditState(
                isEditing = true,
                editingField = ProfileField.DISPLAY_NAME,
                tempDisplayName = "John Doe"
            ),
            imageUploadState = ImageUploadState(),
            onStartEditing = {},
            onCancelEditing = {},
            onConfirmEdit = {},
            onUpdateTempDisplayName = {},
            onUploadProfileImage = {},
            onDeleteProfileImage = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileHeaderUploadingPreview() {
    MountainSunriseTheme {
        ProfileHeader(
            profile = UserProfile(
                userId = "123",
                displayName = "John Doe",
                email = "john.doe@example.com",
                profileImageUrl = "https://example.com/avatar.jpg",
                joinDate = System.currentTimeMillis() - (6 * 30 * 24 * 60 * 60 * 1000L),
                isEmailVerified = false,
                authProvider = AuthProvider.EMAIL_PASSWORD,
                lastLoginDate = System.currentTimeMillis()
            ),
            editState = ProfileEditState(),
            imageUploadState = ImageUploadState(
                isUploading = true,
                progress = 0.6f
            ),
            onStartEditing = {},
            onCancelEditing = {},
            onConfirmEdit = {},
            onUpdateTempDisplayName = {},
            onUploadProfileImage = {},
            onDeleteProfileImage = {}
        )
    }
}
