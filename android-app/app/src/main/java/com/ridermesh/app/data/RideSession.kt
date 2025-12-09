package com.ridermesh.app.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface RideStatus {
    data object Idle : RideStatus
    data object Hosting : RideStatus
    data object Joining : RideStatus
    data object Connected : RideStatus
    data class Error(val reason: String) : RideStatus
}

data class RideSession(
    val rideId: String,
    val rideName: String,
    val hostId: String,
    val isHost: Boolean,
    val joinCode: String,
    val keyBase64: String
) {
    private val _peers = MutableStateFlow<List<PeerInfo>>(emptyList())
    val peers: StateFlow<List<PeerInfo>> = _peers

    fun updatePeers(updated: List<PeerInfo>) {
        _peers.value = updated
    }
}
