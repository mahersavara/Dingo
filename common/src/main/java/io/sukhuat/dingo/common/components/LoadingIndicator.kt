package io.sukhuat.dingo.common.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import io.sukhuat.dingo.common.theme.DingoTheme
import io.sukhuat.dingo.common.theme.ProgressIndicatorSize
import io.sukhuat.dingo.common.theme.ProgressIndicatorThickness

/**
 * A loading indicator that can be displayed in various contexts
 * @param modifier Modifier to be applied to the indicator
 * @param isFullScreen Whether the indicator should cover the full screen with a semi-transparent background
 * @param backgroundColor Background color when in fullscreen mode
 * @param contentAlpha Alpha value for the background when in fullscreen mode
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    backgroundColor: Color = Color.Black,
    contentAlpha: Float = 0.6f
) {
    if (isFullScreen) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor.copy(alpha = contentAlpha)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(ProgressIndicatorSize),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = ProgressIndicatorThickness
            )
        }
    } else {
        CircularProgressIndicator(
            modifier = modifier.size(ProgressIndicatorSize),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = ProgressIndicatorThickness
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingIndicatorPreview() {
    DingoTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FullScreenLoadingIndicatorPreview() {
    DingoTheme {
        LoadingIndicator(isFullScreen = true)
    }
}
