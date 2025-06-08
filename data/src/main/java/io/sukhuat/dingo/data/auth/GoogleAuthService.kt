package io.sukhuat.dingo.data.auth

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import io.sukhuat.dingo.common.utils.ToastHelper
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "GoogleAuthService"

@Singleton
class GoogleAuthService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var googleSignInClient: GoogleSignInClient? = null
    private var isInitialized = false

    fun initialize(webClientId: String) {
        try {
            // Create Google Sign In options
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(context, gso)
            isInitialized = true
            Log.d(TAG, "Google Sign-In initialized successfully with client ID: ${webClientId.takeLast(8)}...")
        } catch (e: Exception) {
            val errorMsg = "Failed to initialize Google Sign-In: ${e.message}"
            Log.e(TAG, errorMsg, e)
            ToastHelper.showLong(context, errorMsg)
            isInitialized = false
        }
    }

    fun getSignInIntent(): Intent {
        if (!isInitialized || googleSignInClient == null) {
            val errorMsg = "Google Sign-In is not properly initialized. Check your Web Client ID."
            Log.e(TAG, errorMsg)
            ToastHelper.showLong(context, errorMsg)
            throw IllegalStateException(errorMsg)
        }
        return googleSignInClient!!.signInIntent
    }

    fun getLastSignedInAccount() = GoogleSignIn.getLastSignedInAccount(context)

    suspend fun signOut() {
        if (!isInitialized || googleSignInClient == null) {
            val errorMsg = "Cannot sign out: Google Sign-In is not properly initialized"
            Log.e(TAG, errorMsg)
            ToastHelper.showMedium(context, errorMsg)
            return
        }

        try {
            googleSignInClient!!.signOut().await()
            Log.d(TAG, "Successfully signed out from Google")
        } catch (e: Exception) {
            val errorMsg = "Error signing out from Google: ${e.message}"
            Log.e(TAG, errorMsg, e)
            ToastHelper.showMedium(context, errorMsg)
            throw e
        }
    }

    suspend fun revokeAccess() {
        if (!isInitialized || googleSignInClient == null) {
            val errorMsg = "Cannot revoke access: Google Sign-In is not properly initialized"
            Log.e(TAG, errorMsg)
            ToastHelper.showMedium(context, errorMsg)
            return
        }

        try {
            googleSignInClient!!.revokeAccess().await()
            Log.d(TAG, "Successfully revoked Google access")
        } catch (e: Exception) {
            val errorMsg = "Error revoking Google access: ${e.message}"
            Log.e(TAG, errorMsg, e)
            ToastHelper.showMedium(context, errorMsg)
            throw e
        }
    }

    fun isInitialized() = isInitialized
}
