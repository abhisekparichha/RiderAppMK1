package com.ridermesh.app.data

import java.util.UUID
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MeshMessage(
    @SerialName("id") val id: String = UUID.randomUUID().toString(),
    @SerialName("rideId") val rideId: String,
    @SerialName("senderId") val senderId: String,
    @SerialName("type") val type: MessageType,
    @SerialName("timestamp") val timestamp: Long = System.currentTimeMillis(),
    @SerialName("ttl") val ttl: Int = DEFAULT_TTL,
    @SerialName("payload") val payload: Map<String, String>
) {
    fun shouldForward(currentHop: Int): Boolean = currentHop < ttl

    companion object {
        const val DEFAULT_TTL = 4
    }
}
