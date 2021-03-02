package com.jozzee.android.networkmonitor

import android.content.Context
import android.net.*
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData

class NetworkMonitor(context: Context) : LiveData<NetworkStatus>() {

    companion object {
        /**
         * Status of network
         */
        var isConnected: Boolean = false
            private set

        /**
         * Transport type of network from [NetworkCapabilities]
         */
        var transportType: Int = -1
            private set

        fun isTransportWiFi() = transportType == NetworkCapabilities.TRANSPORT_WIFI

        fun isTransportCellular() = transportType == NetworkCapabilities.TRANSPORT_CELLULAR
    }

    private var manager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private var networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                updateNetworkInfo(network)
            } else {
                updateNetworkInfoLegacy(manager.activeNetworkInfo)
            }
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                updateNetworkInfo(network)
            } else {
                updateNetworkInfoLegacy(manager.activeNetworkInfo)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            isConnected = false
            transportType = -1
            postValue(NetworkStatus(isConnected, transportType))
        }
    }

    private fun updateNetworkInfo(network: Network?) {
        if (network != null) {
            val nc: NetworkCapabilities? = manager.getNetworkCapabilities(network)
            if (nc != null) {
                val oldConnectedStatus = isConnected
                val oldTransportType = transportType
                var isUpdateLiveData = false

                when {
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                        if (oldConnectedStatus.not() || oldTransportType != NetworkCapabilities.TRANSPORT_WIFI) {
                            isUpdateLiveData = true
                        }
                        isConnected = true
                        transportType = NetworkCapabilities.TRANSPORT_WIFI
                    }
                    nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        if (oldConnectedStatus.not() || oldTransportType != NetworkCapabilities.TRANSPORT_CELLULAR) {
                            isUpdateLiveData = true
                        }
                        isConnected = true
                        transportType = NetworkCapabilities.TRANSPORT_CELLULAR
                    }
                    else -> {
                        isConnected = false
                        transportType = -1
                        isUpdateLiveData = true
                    }
                }

                if (isUpdateLiveData) {
                    postValue(NetworkStatus(isConnected, transportType))
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun updateNetworkInfoLegacy(networkInfo: NetworkInfo?) {
        if (networkInfo != null) {
            val oldConnectedStatus = isConnected
            val oldTransportType = transportType

            try {
                isConnected = networkInfo.isConnected
                when (networkInfo.type) {
                    ConnectivityManager.TYPE_WIFI -> transportType = NetworkCapabilities.TRANSPORT_WIFI
                    ConnectivityManager.TYPE_MOBILE -> transportType = NetworkCapabilities.TRANSPORT_CELLULAR
                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
            }

            if (oldConnectedStatus != isConnected || oldTransportType != transportType) {
                postValue(NetworkStatus(isConnected, transportType))
            }
        }
    }

    /**
     * When active register network callback.
     */
    override fun onActive() {
        super.onActive()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            manager.registerDefaultNetworkCallback(networkCallback)

        } else {
            //val builder = NetworkRequest.Builder()
            //        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            //        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            manager.registerNetworkCallback(NetworkRequest.Builder().build(), networkCallback)
        }
        Log.w("NetworkMonitor", "Register Network Callback.")

        //Update network status after register network monitor.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && manager.activeNetwork != null) {
            updateNetworkInfo(manager.activeNetwork)
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && manager.activeNetworkInfo != null) {
            updateNetworkInfoLegacy(manager.activeNetworkInfo)
        } else {
            postValue(NetworkStatus(isConnected, transportType))
        }
    }

    /**
     * When inActive un register network callback.
     */
    override fun onInactive() {
        super.onInactive()
        manager.unregisterNetworkCallback(networkCallback)
        Log.w("NetworkMonitor", "Un Register Network Callback.")
    }
}