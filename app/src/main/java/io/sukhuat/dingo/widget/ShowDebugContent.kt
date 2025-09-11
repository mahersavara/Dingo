package io.sukhuat.dingo.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import io.sukhuat.dingo.MainActivity

@Composable
fun ShowDebugContent(message: String) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFFFDF2E9)))
            .padding(8.dp)
    ) {
        Text(
            text = "🔧 Widget Debug",
            style = TextStyle(
                fontSize = 12.sp,
                color = ColorProvider(Color(0xFF92400E))
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = message,
            style = TextStyle(
                fontSize = 9.sp,
                color = ColorProvider(Color(0xFF666666))
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Time: ${System.currentTimeMillis()}",
            style = TextStyle(
                fontSize = 8.sp,
                color = ColorProvider(Color(0xFF999999))
            )
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = "Tap to open app",
            style = TextStyle(
                fontSize = 8.sp,
                color = ColorProvider(Color.Blue)
            ),
            modifier = GlanceModifier.clickable(actionStartActivity(MainActivity::class.java))
        )
    }
}
