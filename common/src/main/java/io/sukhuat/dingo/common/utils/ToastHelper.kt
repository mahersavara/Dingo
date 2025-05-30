package io.sukhuat.dingo.common.utils

import android.content.Context
import android.widget.Toast

object ToastHelper {
    private var currentToast: Toast? = null

    /**
     * Shows a toast message with custom duration, cancelling any existing toast
     * @param context The context
     * @param message The message to show
     * @param durationMs Custom duration in milliseconds
     */
    fun showToast(context: Context, message: String, durationMs: Long) {
        currentToast?.cancel()
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        currentToast?.duration = (durationMs / 1000).toInt().coerceAtMost(Toast.LENGTH_LONG)
        currentToast?.show()
    }

    /**
     * Shows a short toast message (2 seconds)
     */
    fun showShort(context: Context, message: String) {
        showToast(context, message, 2000)
    }

    /**
     * Shows a medium toast message (3.5 seconds)
     */
    fun showMedium(context: Context, message: String) {
        showToast(context, message, 3500)
    }

    /**
     * Shows a long toast message (5 seconds)
     */
    fun showLong(context: Context, message: String) {
        showToast(context, message, 5000)
    }

    /**
     * Cancels any currently showing toast
     */
    fun cancel() {
        currentToast?.cancel()
        currentToast = null
    }
}
