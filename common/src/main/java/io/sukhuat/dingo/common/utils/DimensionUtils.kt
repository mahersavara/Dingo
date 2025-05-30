package io.sukhuat.dingo.common.utils

import android.content.res.Resources
import kotlin.math.roundToInt

object DimensionUtils {
    /**
     * Converts DP to PX
     * @param dp the value in dp
     * @return the value in pixels
     */
    fun dpToPx(dp: Float): Int {
        return (dp * Resources.getSystem().displayMetrics.density).roundToInt()
    }

    /**
     * Converts DP to PX
     * @param dp the value in dp
     * @return the value in pixels
     */
    fun dpToPx(dp: Int): Int {
        return dpToPx(dp.toFloat())
    }

    /**
     * Converts PX to DP
     * @param px the value in pixels
     * @return the value in dp
     */
    fun pxToDp(px: Int): Float {
        return px / Resources.getSystem().displayMetrics.density
    }
}
