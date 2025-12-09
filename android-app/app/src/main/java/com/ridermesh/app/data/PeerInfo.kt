package com.ridermesh.app.data

data class PeerInfo(
    val peerId: String,
    val displayName: String,
    val lastSeen: Long,
    val role: PeerRole,
    val batteryLevel: Int? = null,
    val hopCount: Int = 0,
    val distanceMeters: Float? = null
)

enum class PeerRole { HOST, MEMBER }
