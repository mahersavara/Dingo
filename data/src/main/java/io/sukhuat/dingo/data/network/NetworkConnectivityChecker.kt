package io.sukhuat.dingo.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Network connectivity checker for monitoring network status
 * Used to trigger sync operations when network becomes available
 * Requirements: 5.4 - Background sync when network available
 */
@Singleton
class NetworkConnectivityChecker @Inject constructor(
    @ApplicationContext private val context: Context
) : io.sukhuat.dingo.domain.model.NetworkConnectivityChecker {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /**
     * Check if network is currently available
     */
    override fun isConnected(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected == true
        }
    }

    /**
     * Check if network is metered (mobile data)
     */
    fun isNetworkMetered(): Boolean {
        return connectivityManager.isActiveNetworkMetered
    }

    /**
     * Check if WiFi is connected
     */
    override fun isWifiConnected(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            return networkInfo?.isConnected == true
        }
    }

    /**
     * Check if mobile data is connected
     */
    override fun isMobileConnected(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            return networkInfo?.isConnected == true
        }
    }

    /**
     * Get current network type
     */
    fun getNetworkType(): NetworkType {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)

            return when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
                else -> NetworkType.NONE
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return when (networkInfo?.type) {
                ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
                ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
                ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
                else -> NetworkType.NONE
            }
        }
    }

    /**
     * Observe network connectivity changes
     */
    fun observeNetworkConnectivity(): Flow<NetworkStatus> = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(NetworkStatus.AVAILABLE)
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.LOST)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                if (hasInternet && isValidated) {
                    trySend(NetworkStatus.AVAILABLE)
                } else {
                    trySend(NetworkStatus.LIMITED)
                }
            }

            override fun onUnavailable() {
                trySend(NetworkStatus.UNAVAILABLE)
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Send initial state
        trySend(
            if (isConnected()) {
                NetworkStatus.AVAILABLE
            } else {
                NetworkStatus.UNAVAILABLE
            }
        )

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }.distinctUntilChanged()

    /**
     * Observe network connectivity as boolean
     */
    fun observeIsConnected(): Flow<Boolean> = observeNetworkConnectivity()
        .distinctUntilChanged()
        .map { status ->
            status == NetworkStatus.AVAILABLE
        }

    /**
     * Check if sync should be allowed based on network conditions
     */
    fun shouldAllowSync(allowMetered: Boolean = false): Boolean {
        return isConnected() && (allowMetered || !isNetworkMetered())
    }

    /**
     * Get network quality estimation
     */
    fun getNetworkQuality(): NetworkQuality {
        if (!isConnected()) return NetworkQuality.NO_CONNECTION

        return when (getNetworkType()) {
            NetworkType.WIFI -> NetworkQuality.EXCELLENT
            NetworkType.ETHERNET -> NetworkQuality.EXCELLENT
            NetworkType.CELLULAR -> {
                if (isNetworkMetered()) NetworkQuality.GOOD else NetworkQuality.EXCELLENT
            }
            NetworkType.NONE -> NetworkQuality.NO_CONNECTION
        }
    }
}

/**
 * Network status enumeration
 */
enum class NetworkStatus {
    AVAILABLE,
    LIMITED,
    LOST,
    UNAVAILABLE
}

/**
 * Network type enumeration
 */
enum class NetworkType {
    WIFI,
    CELLULAR,
    ETHERNET,
    NONE
}

/**
 * Network quality enumeration
 */
enum class NetworkQuality {
    EXCELLENT,
    GOOD,
    POOR,
    NO_CONNECTION
}
