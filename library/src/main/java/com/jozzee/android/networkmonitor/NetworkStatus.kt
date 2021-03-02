package com.jozzee.android.networkmonitor

import android.net.NetworkCapabilities

/**
 * Detail of network.
 * [isConnected] status network
 * [transportType] Transport type of network from [NetworkCapabilities]
 */
data class NetworkStatus(val isConnected: Boolean = false,
                         val transportType: Int = -1) {

    fun isTransportWiFi() = transportType == NetworkCapabilities.TRANSPORT_WIFI

    fun isTransportCellular() = transportType == NetworkCapabilities.TRANSPORT_CELLULAR

    override fun toString(): String {
        return "NetworkStatus: { isConnected: $isConnected, transportType: ${getTransportTypeName()} }"
    }

    private fun getTransportTypeName(): String {
        return when (transportType) {
            //-1 -> ""
            NetworkCapabilities.TRANSPORT_WIFI -> "Wifi"
            NetworkCapabilities.TRANSPORT_CELLULAR -> "Cellular"
            else -> "Unknown"
        }
    }
}