package com.ridermesh.app.network

import android.content.Context
import com.ridermesh.app.data.MeshMessage
import com.ridermesh.app.data.MessageStore
import com.ridermesh.app.data.RideSession
import com.ridermesh.app.data.PeerInfo
import com.ridermesh.app.data.PeerRole
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Default MeshManager glues BLE advertisements with Wi-Fi Direct sessions.
 * The current implementation focuses on surfacing the data flow while the
 * radio plumbing is stubbed out with TODOs that can be completed on device.
 */
class DefaultMeshManager(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : MeshManager {

    private val messageStore = MessageStore()
    private val _incomingMessages = MutableSharedFlow<MeshMessage>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val _rideSession = MutableStateFlow<RideSession?>(null)
    private val _peerUpdates = MutableSharedFlow<List<PeerInfo>>(replay = 1)
    private val _advertisements = MutableStateFlow<List<RideAdvertisement>>(emptyList())
    private val rideScanner = BleRideScanner(context)

    override val incomingMessages: Flow<MeshMessage> = _incomingMessages.asSharedFlow()
    override val rideSession: Flow<RideSession?> = _rideSession
    override val peerUpdates: Flow<List<PeerInfo>> = _peerUpdates.asSharedFlow()
    private var isScanning = false

    override suspend fun startHosting(config: RideHostConfig): Result<RideSession> = runCatching {
        val session = RideSession(
            rideId = UUID.randomUUID().toString(),
            rideName = config.rideName,
            hostId = config.hostName,
            isHost = true,
            joinCode = config.rideCode,
            keyBase64 = config.rideKey.encodeBase64()
        )
        _rideSession.value = session
        // TODO: start BLE advertising + Wi-Fi Direct group owner role.
        scope.launch { heartbeatAsHost(session) }
        session
    }

    override fun discoverRides(): Flow<List<RideAdvertisement>> {
        if (!isScanning) {
            isScanning = true
            scope.launch {
                rideScanner.scan().collect { advertisement ->
                    _advertisements.update { current ->
                        (current + advertisement).distinctBy { it.rideId }.takeLast(12)
                    }
                }
            }
        }
        return _advertisements
    }

    override suspend fun joinRide(joinRequest: RideJoinRequest): Result<RideSession> = runCatching {
        val session = RideSession(
            rideId = joinRequest.rideId,
            rideName = "Pending",
            hostId = "",
            isHost = false,
            joinCode = joinRequest.rideCode,
            keyBase64 = ""
        )
        _rideSession.value = session
        // TODO: connect to Wi-Fi Direct group and authenticate with host.
        scope.launch { heartbeatAsMember(session, joinRequest.riderName) }
        session
    }

    override suspend fun sendMessage(message: MeshMessage): Result<Unit> = runCatching {
        if (messageStore.shouldProcess(message.id)) {
            _incomingMessages.emit(message)
        }
        // TODO: actually send encrypted payload over Wi-Fi Direct sockets.
    }

    override suspend fun stop() {
        _rideSession.value = null
        // TODO: tear down radios.
    }

    private suspend fun heartbeatAsHost(session: RideSession) {
        val hostPeer = PeerInfo(
            peerId = session.hostId,
            displayName = session.hostId,
            lastSeen = System.currentTimeMillis(),
            role = PeerRole.HOST,
            batteryLevel = null,
            hopCount = 0
        )
        _peerUpdates.emit(listOf(hostPeer))
    }

    private suspend fun heartbeatAsMember(session: RideSession, riderName: String) {
        val member = PeerInfo(
            peerId = riderName,
            displayName = riderName,
            lastSeen = System.currentTimeMillis(),
            role = PeerRole.MEMBER,
            batteryLevel = null,
            hopCount = 1
        )
        _peerUpdates.emit(listOf(member))
    }
}

private fun ByteArray.encodeBase64(): String = android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
