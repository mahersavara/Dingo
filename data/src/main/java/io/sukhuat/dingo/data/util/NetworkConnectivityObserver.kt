package io.sukhuat.dingo.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enum representing the network connection status
 */
enum class ConnectionStatus {
    AVAILABLE, UNAVAILABLE
}

/**
 * Observer for network connectivity changes
 */
@Singleton
class NetworkConnectivityObserver @Inject constructor(
    private val context: Context
) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Observe network connectivity changes
     * @return Flow of ConnectionStatus
     */
    fun observe(): Flow<ConnectionStatus> = callbackFlow {
        // Initial status check
        val initialStatus = getCurrentConnectionStatus()
        trySend(initialStatus)

        // Callback for network changes
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                launch { trySend(ConnectionStatus.AVAILABLE) }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                launch { trySend(ConnectionStatus.UNAVAILABLE) }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val status = if (
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    ConnectionStatus.AVAILABLE
                } else {
                    ConnectionStatus.UNAVAILABLE
                }
                launch { trySend(status) }
            }
        }

        // Register the callback
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Clean up when the flow is cancelled
        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()

    /**
     * Get the current connection status
     */
    fun getCurrentConnectionStatus(): ConnectionStatus {
        return if (isNetworkAvailable()) {
            ConnectionStatus.AVAILABLE
        } else {
            ConnectionStatus.UNAVAILABLE
        }
    }

    /**
     * Check if the network is currently available
     */
    fun isNetworkAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }
}
