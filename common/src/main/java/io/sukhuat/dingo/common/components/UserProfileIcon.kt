package io.sukhuat.dingo.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import io.sukhuat.dingo.common.R
import io.sukhuat.dingo.common.theme.MountainSunriseTheme
import io.sukhuat.dingo.common.theme.RusticGold
import io.sukhuat.dingo.domain.model.UserProfile

/**
 * A circular user profile icon with smart image priority logic
 * Priority: Custom uploaded image > Google profile photo > Default icon
 * * @param profileImageUrl Custom uploaded profile image URL
 * @param googlePhotoUrl Google profile photo URL
 * @param hasCustomImage Whether user has uploaded a custom image
 * @param size Size of the icon in dp
 * @param borderWidth Width of the border in dp
 * @param borderColor Color of the border
 * @param backgroundColor Background color when no image is available
 */
@Composable
fun UserProfileIcon(
    profileImageUrl: String? = null,
    googlePhotoUrl: String? = null,
    hasCustomImage: Boolean = false,
    size: Dp = 40.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = RusticGold,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    // Determine image source using priority logic
    val imageUrl = when {
        hasCustomImage && !profileImageUrl.isNullOrEmpty() -> profileImageUrl
        !googlePhotoUrl.isNullOrEmpty() -> googlePhotoUrl
        else -> null
    }
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(borderWidth, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl != null) {
            // Display user profile image if available
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.profile),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
            )
        } else {
            // Display default person icon if no image is available
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.profile),
                tint = borderColor,
                modifier = Modifier.size(size * 0.6f)
            )
        }
    }
}

/**
 * Convenience overload that accepts a UserProfile object
 * Uses the same priority logic internally
 */
@Composable
fun UserProfileIcon(
    userProfile: UserProfile,
    size: Dp = 40.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = RusticGold,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    UserProfileIcon(
        profileImageUrl = userProfile.profileImageUrl,
        googlePhotoUrl = userProfile.googlePhotoUrl,
        hasCustomImage = userProfile.hasCustomImage,
        size = size,
        borderWidth = borderWidth,
        borderColor = borderColor,
        backgroundColor = backgroundColor
    )
}

@Preview
@Composable
fun UserProfileIconPreview() {
    MountainSunriseTheme {
        UserProfileIcon()
    }
}

@Preview
@Composable
fun UserProfileIconLargePreview() {
    MountainSunriseTheme {
        UserProfileIcon(
            size = 64.dp,
            borderWidth = 2.dp
        )
    }
}
