package com.jozzee.android.networkmonitor.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Observer
import com.github.jozzee.android.networkmonitor.NetworkMonitor
import com.github.jozzee.android.networkmonitor.NetworkStatus

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NetworkMonitor(this).observe(this, onNetworkChanged)
    }

    private val onNetworkChanged: Observer<NetworkStatus> by lazy {
        Observer<NetworkStatus> {
            Log.d("NetworkMonitor", it.toString())
        }
    }
}