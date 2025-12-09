package com.ridermesh.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridermesh.app.data.MeshMessage
import com.ridermesh.app.data.MessageType
import com.ridermesh.app.data.PeerInfo
import com.ridermesh.app.data.RideSession
import com.ridermesh.app.data.RideStatus
import com.ridermesh.app.encryption.CryptoManager
import com.ridermesh.app.network.MeshManager
import com.ridermesh.app.network.RideHostConfig
import com.ridermesh.app.network.RideJoinRequest
import com.ridermesh.app.network.RideAdvertisement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RideViewModel(
    private val meshManager: MeshManager,
    private val cryptoManager: CryptoManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RideUiState())
    val uiState: StateFlow<RideUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            meshManager.rideSession.collect { session ->
                _uiState.update { it.copy(activeSession = session, status = if (session == null) RideStatus.Idle else RideStatus.Connected) }
            }
        }
        viewModelScope.launch {
            meshManager.peerUpdates.collect { peers ->
                _uiState.update { it.copy(peers = peers) }
            }
        }
        viewModelScope.launch {
            meshManager.incomingMessages.collect { message ->
                _uiState.update { state -> state.copy(messages = (state.messages + message).takeLast(100)) }
            }
        }
        viewModelScope.launch {
            meshManager.discoverRides().collect { ads ->
                _uiState.update { it.copy(discoveredRides = ads) }
            }
        }
    }

    fun createRide(rideName: String, hostName: String, pin: String?) {
        viewModelScope.launch {
            val bundle = cryptoManager.generateRideKey()
            val result = meshManager.startHosting(
                RideHostConfig(
                    rideName = rideName,
                    hostName = hostName,
                    pin = pin,
                    rideKey = bundle.rawKey,
                    rideCode = bundle.joinCode
                )
            )
            result.onFailure { err ->
                _uiState.update { it.copy(status = RideStatus.Error(err.message ?: "Unknown")) }
            }.onSuccess { session ->
                _uiState.update { it.copy(activeSession = session.copy(joinCode = bundle.joinCode), status = RideStatus.Hosting) }
            }
        }
    }

    fun joinRide(rideId: String, rideCode: String, riderName: String, pin: String?) {
        viewModelScope.launch {
            val result = meshManager.joinRide(
                RideJoinRequest(rideId = rideId, rideCode = rideCode, riderName = riderName, pin = pin)
            )
            result.onFailure { err ->
                _uiState.update { it.copy(status = RideStatus.Error(err.message ?: "Unable to join")) }
            }.onSuccess { session ->
                _uiState.update { it.copy(activeSession = session, status = RideStatus.Joining) }
            }
        }
    }

    fun sendTextMessage(body: String) {
        val session = _uiState.value.activeSession ?: return
        val message = MeshMessage(
            rideId = session.rideId,
            senderId = session.hostId,
            type = MessageType.TEXT_MESSAGE,
            payload = mapOf("body" to body)
        )
        viewModelScope.launch {
            val key = cryptoManager.getKey(session.rideId) ?: return@launch
            val encrypted = cryptoManager.encrypt(message, key)
            val securedPayload = mapOf(
                "body" to body,
                "cipher" to encrypted.ciphertext.encodeBase64(),
                "iv" to encrypted.iv.encodeBase64()
            )
            meshManager.sendMessage(message.copy(payload = securedPayload))
        }
    }

    fun resetRide() {
        viewModelScope.launch {
            meshManager.stop()
            _uiState.value = RideUiState()
        }
    }
}

data class RideUiState(
    val status: RideStatus = RideStatus.Idle,
    val activeSession: RideSession? = null,
    val peers: List<PeerInfo> = emptyList(),
    val messages: List<MeshMessage> = emptyList(),
    val discoveredRides: List<RideAdvertisement> = emptyList()
)

private fun ByteArray.encodeBase64(): String = android.util.Base64.encodeToString(this, android.util.Base64.NO_WRAP)
