package io.sukhuat.dingo.common.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import io.sukhuat.dingo.common.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class MediaType {
    IMAGE, GIF, STICKER
}

@Composable
fun BubbleComponent(
    id: Int,
    text: String,
    imageResId: Int? = null,
    customImage: String? = null,
    createdAt: Long,
    position: Pair<Float, Float>,
    onDismiss: () -> Unit,
    onTextChange: (String) -> Unit,
    onMediaUpload: (Uri, MediaType) -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Animation for bubble appearance
    var isVisible by remember { mutableStateOf(false) }
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        ),
        label = "bubble_scale"
    )
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    // Calculate if bubble is editable (within 30 minutes of creation)
    val isEditable = remember(createdAt) {
        val fifteenMinutesInMillis = 15 * 60 * 1000
        (System.currentTimeMillis() - createdAt) < fifteenMinutesInMillis
    }
    
    // Calculate time remaining for editing
    val timeRemainingMillis = remember(createdAt) {
        val fifteenMinutesInMillis = 15 * 60 * 1000
        val elapsedMillis = System.currentTimeMillis() - createdAt
        maxOf(0, fifteenMinutesInMillis - elapsedMillis)
    }
    
    // Format time remaining
    val timeRemainingText = remember(timeRemainingMillis) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeRemainingMillis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeRemainingMillis) - TimeUnit.MINUTES.toSeconds(minutes)
        if (minutes > 0) {
            "$minutes min ${seconds}s remaining"
        } else {
            "${seconds}s remaining"
        }
    }
    
    // Calculate bubble dimensions and constraints
    val bubbleWidth = 280.dp
    val bubbleHeight = 220.dp
    val bubbleWidthPx = with(density) { bubbleWidth.roundToPx() }
    val bubbleHeightPx = with(density) { bubbleHeight.roundToPx() }
    val screenWidthPx = with(density) { 360.dp.roundToPx() }
    val screenHeightPx = with(density) { 640.dp.roundToPx() }
    
    // Calculate position, ensuring bubble stays within screen bounds
    val xPos = position.first.coerceIn(0f, (screenWidthPx - bubbleWidthPx).toFloat())
    val yPos = position.second.coerceIn(0f, (screenHeightPx - bubbleHeightPx).toFloat())
    
    // Track text input
    var textValue by remember { mutableStateOf(text) }
    
    // Track uploaded media
    var uploadedImageUri by remember(customImage) { 
        mutableStateOf<Uri?>(
            if (customImage != null) Uri.parse(customImage) else null
        ) 
    }
    var isUploading by remember { mutableStateOf(false) }
    
    // Track selected media tab
    var selectedMediaTab by remember { mutableStateOf(0) }
    val mediaTabs = listOf("Image", "GIF", "Sticker")
    
    // Sticker selection
    var showStickerSelector by remember { mutableStateOf(false) }
    val stickers = listOf(
        R.drawable.ic_sticker_happy,
        R.drawable.ic_sticker_sad,
        R.drawable.ic_sticker_love,
        R.drawable.ic_sticker_cool,
        R.drawable.ic_sticker_angry,
        R.drawable.ic_sticker_thinking
    )
    
    // GIF selection
    var showGifSelector by remember { mutableStateOf(false) }
    val gifs = listOf(
        R.drawable.ic_gif_celebration,
        R.drawable.ic_gif_thumbsup,
        R.drawable.ic_gif_clapping,
        R.drawable.ic_gif_party,
        R.drawable.ic_gif_dance,
        R.drawable.ic_gif_smile
    )
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            android.util.Log.d("BubbleComponent", "Image selected: $uri")
            // We'll simulate a short delay for the upload process
            coroutineScope.launch {
                kotlinx.coroutines.delay(500) // Simulate processing time
                uploadedImageUri = uri
                android.util.Log.d("BubbleComponent", "Setting uploadedImageUri to: $uri")
                onMediaUpload(uri, MediaType.IMAGE)
                isUploading = false
            }
        }
    }
    
    // Dismiss on outside click
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() }
    ) {
        // Bubble editor
        Popup(
            alignment = Alignment.TopStart,
            offset = IntOffset(xPos.toInt(), yPos.toInt()),
            properties = PopupProperties(
                focusable = true,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
            onDismissRequest = onDismiss
        ) {
            Card(
                modifier = Modifier
                    .width(bubbleWidth)
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp))
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEditable) "Edit Goal" else "Goal Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Text input
                    if (isEditable) {
                        TextField(
                            value = textValue,
                            onValueChange = { 
                                if (it.length <= 30) {
                                    textValue = it
                                    onTextChange(it)
                                }
                            },
                            placeholder = { Text("Enter goal text") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                        
                        // Show time remaining for editing
                        Text(
                            text = timeRemainingText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.End)
                        )
                    } else {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Text(
                            text = "(Created more than 15 minutes ago)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Media type selector tabs
                    if (isEditable) {
                        TabRow(
                            selectedTabIndex = selectedMediaTab,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            mediaTabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedMediaTab == index,
                                    onClick = { selectedMediaTab = index },
                                    text = { Text(title) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Media preview or upload button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable(enabled = isEditable && !isUploading) {
                                when (selectedMediaTab) {
                                    0 -> {
                                        // Launch real image picker
                                        imagePickerLauncher.launch("image/*")
                                    }
                                    1 -> {
                                        // Show GIF selector
                                        showGifSelector = true
                                    }
                                    2 -> {
                                        // Show sticker selector
                                        showStickerSelector = true
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) {
                            // Show loading indicator while uploading
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Processing image...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else if (uploadedImageUri != null) {
                            // Show uploaded image
                            AsyncImage(
                                model = uploadedImageUri,
                                contentDescription = "Uploaded media",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Show cloud upload indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_upload),
                                    contentDescription = "Saved to cloud",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else if (customImage != null) {
                            // This branch should not be reached normally since uploadedImageUri is initialized with customImage
                            // But kept as a fallback
                            AsyncImage(
                                model = customImage,
                                contentDescription = "Goal image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Show cloud indicator
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(4.dp)
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_upload),
                                    contentDescription = "Saved to cloud",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else if (imageResId != null) {
                            // Show icon
                            Icon(
                                painter = painterResource(id = imageResId),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Unspecified
                            )
                        } else {
                            // Show upload button
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(
                                        id = when (selectedMediaTab) {
                                            0 -> R.drawable.ic_upload
                                            1 -> R.drawable.ic_gif
                                            else -> R.drawable.ic_sticker
                                        }
                                    ),
                                    contentDescription = when (selectedMediaTab) {
                                        0 -> "Upload image"
                                        1 -> "Browse GIFs"
                                        else -> "Select sticker"
                                    },
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                
                                Text(
                                    text = when (selectedMediaTab) {
                                        0 -> "Upload Image"
                                        1 -> "Browse GIFs"
                                        else -> "Select Sticker"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                // Add cloud storage indication
                                Text(
                                    text = "Saved to Cloud",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        // Show upload overlay if editable
                        if (isEditable && !isUploading && (uploadedImageUri != null || imageResId != null)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_photo_camera),
                                    contentDescription = "Change media",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
            
            // Action buttons outside the bubble with more space
            Row(
                modifier = Modifier
                    .offset(
                        x = with(density) { bubbleWidth / 2 - 60.dp },
                        y = with(density) { bubbleHeight + 24.dp }  // Increased spacing
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Archive button
                IconButton(
                    onClick = onArchive,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_archive),
                        contentDescription = "Archive goal",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))  // Increased spacing between buttons
                
                // Delete button (shown as permanent archive)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(4.dp, CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete_goal),
                        contentDescription = "Archive goal permanently",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        // Sticker selector popup
        if (showStickerSelector) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showStickerSelector = false }
            ) {
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .padding(8.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select a Sticker",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        ) {
                            items(stickers) { stickerId ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(64.dp)
                                        .clickable {
                                            // Create a "fake" URI for the sticker
                                            val uri = Uri.parse("android.resource://${context.packageName}/$stickerId")
                                            uploadedImageUri = uri
                                            onMediaUpload(uri, MediaType.STICKER)
                                            showStickerSelector = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = stickerId),
                                        contentDescription = "Sticker",
                                        modifier = Modifier.size(48.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // GIF selector popup
        if (showGifSelector) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { showGifSelector = false }
            ) {
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .padding(8.dp)
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select a GIF",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        ) {
                            items(gifs) { gifId ->
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable {
                                            // Create a "fake" URI for the GIF
                                            val uri = Uri.parse("android.resource://${context.packageName}/$gifId")
                                            uploadedImageUri = uri
                                            onMediaUpload(uri, MediaType.GIF)
                                            showGifSelector = false
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = gifId),
                                        contentDescription = "GIF",
                                        modifier = Modifier.size(80.dp),
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 