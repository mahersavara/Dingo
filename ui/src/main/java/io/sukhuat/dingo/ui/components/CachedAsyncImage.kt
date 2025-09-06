package io.sukhuat.dingo.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import io.sukhuat.dingo.ui.screens.home.FirebaseStorageUtil
import kotlinx.coroutines.launch

/**
 * Cached async image component that automatically uses local cache for Firebase Storage URLs
 */
@Composable
fun CachedAsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    error: Painter? = null,
    onLoading: ((AsyncImagePainter.State.Loading) -> Unit)? = null,
    onSuccess: ((AsyncImagePainter.State.Success) -> Unit)? = null,
    onError: ((AsyncImagePainter.State.Error) -> Unit)? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var imageModel by remember(model) { mutableStateOf(model) }
    var isLoadingCache by remember(model) { mutableStateOf(false) }
    
    // Try to get cached image for Firebase URLs
    LaunchedEffect(model) {
        val modelString = model?.toString()
        
        if (modelString != null && modelString.startsWith("https://firebasestorage.googleapis.com")) {
            isLoadingCache = true
            coroutineScope.launch {
                try {
                    val cachedUri = FirebaseStorageUtil.getCachedImageUri(context, modelString)
                    if (cachedUri != null) {
                        imageModel = cachedUri
                    } else {
                        // If cache fails, use original URL
                        imageModel = model
                    }
                } catch (e: Exception) {
                    // If cache fails, use original URL
                    imageModel = model
                } finally {
                    isLoadingCache = false
                }
            }
        } else {
            imageModel = model
            isLoadingCache = false
        }
    }
    
    AsyncImage(
        model = imageModel,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        error = error,
        onLoading = onLoading,
        onSuccess = onSuccess,
        onError = onError,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}

/**
 * Get the appropriate image model (cached URI or original)
 * This is a suspend function that can be used in ViewModels or other suspend contexts
 */
suspend fun getCachedImageModel(originalModel: Any?, context: android.content.Context): Any? {
    val modelString = originalModel?.toString()
    
    return if (modelString != null && modelString.startsWith("https://firebasestorage.googleapis.com")) {
        try {
            FirebaseStorageUtil.getCachedImageUri(context, modelString) ?: originalModel
        } catch (e: Exception) {
            originalModel
        }
    } else {
        originalModel
    }
}