package com.ridermesh.app.network

import android.content.Context
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class WifiDirectCoordinator(context: Context) {
    private val manager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private val channel = manager.initialize(context, context.mainLooper, null)

    fun createGroup(): Flow<WifiP2pInfo> = callbackFlow {
        manager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                manager.requestConnectionInfo(channel) { info -> trySend(info) }
            }

            override fun onFailure(reason: Int) {
                close(IllegalStateException("Wi-Fi Direct group failure: $reason"))
            }
        })
        awaitClose { manager.removeGroup(channel, null) }
    }

    fun connect(config: WifiP2pConfig): Flow<WifiP2pInfo> = callbackFlow {
        manager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                manager.requestConnectionInfo(channel) { info -> trySend(info) }
            }

            override fun onFailure(reason: Int) {
                close(IllegalStateException("Wi-Fi Direct connect failure: $reason"))
            }
        })
        awaitClose { manager.cancelConnect(channel, null) }
    }
}
