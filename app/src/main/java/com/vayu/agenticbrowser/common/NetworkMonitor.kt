package com.vayu.agenticbrowser.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor private constructor() {

    private var context: Context? = null
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun init(ctx: Context) {
        context = ctx.applicationContext
        connectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        checkCurrentConnection()
        registerNetworkCallback()
    }

    private fun checkCurrentConnection() {
        val cm = connectivityManager ?: return
        val activeNetwork = cm.activeNetwork
        val capabilities = activeNetwork?.let { cm.getNetworkCapabilities(it) }
        _isConnected.value = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun registerNetworkCallback() {
        val cm = connectivityManager ?: return

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isConnected.value = true
                Logger.i("Network available: $network")
            }

            override fun onLost(network: Network) {
                _isConnected.value = false
                Logger.i("Network lost: $network")
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                val hasInternet = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                val isValidated = networkCapabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
                _isConnected.value = hasInternet && isValidated
                Logger.d("Network capabilities changed: hasInternet=$hasInternet, validated=$isValidated")
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        cm.registerNetworkCallback(request, callback)
        networkCallback = callback

        Logger.i("NetworkMonitor registered")
    }

    fun unregister() {
        val cm = connectivityManager ?: return
        networkCallback?.let { cm.unregisterNetworkCallback(it) }
        networkCallback = null
        Logger.i("NetworkMonitor unregistered")
    }

    fun isConnectedNow(): Boolean {
        val cm = connectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    companion object {
        @Volatile
        private var instance: NetworkMonitor? = null

        fun getInstance(): NetworkMonitor {
            return instance ?: synchronized(this) {
                instance ?: NetworkMonitor().also { instance = it }
            }
        }
    }
}
