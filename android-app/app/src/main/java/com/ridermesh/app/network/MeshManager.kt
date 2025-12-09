package com.ridermesh.app.network

import com.ridermesh.app.data.MeshMessage
import com.ridermesh.app.data.PeerInfo
import com.ridermesh.app.data.RideSession
import kotlinx.coroutines.flow.Flow

interface MeshManager {
    val incomingMessages: Flow<MeshMessage>
    val rideSession: Flow<RideSession?>
    val peerUpdates: Flow<List<PeerInfo>>

    suspend fun startHosting(config: RideHostConfig): Result<RideSession>
    fun discoverRides(): Flow<List<RideAdvertisement>>
    suspend fun joinRide(joinRequest: RideJoinRequest): Result<RideSession>
    suspend fun sendMessage(message: MeshMessage): Result<Unit>
    suspend fun stop()
}

data class RideHostConfig(
    val rideName: String,
    val hostName: String,
    val pin: String?,
    val rideKey: ByteArray,
    val rideCode: String
)

data class RideJoinRequest(
    val rideId: String,
    val rideCode: String,
    val riderName: String,
    val pin: String?
)

data class RideAdvertisement(
    val rideId: String,
    val rideName: String,
    val hostName: String,
    val signalStrength: Int,
    val connectedPeers: Int
)
