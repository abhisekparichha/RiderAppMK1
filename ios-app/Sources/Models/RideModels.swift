import Foundation

struct RideSession: Identifiable, Equatable {
    let id: UUID
    let rideId: String
    let rideName: String
    let hostId: String
    let isHost: Bool
    let joinCode: String
    let key: Data

    init(rideId: String, rideName: String, hostId: String, isHost: Bool, joinCode: String, key: Data) {
        self.id = UUID()
        self.rideId = rideId
        self.rideName = rideName
        self.hostId = hostId
        self.isHost = isHost
        self.joinCode = joinCode
        self.key = key
    }
}

struct PeerInfo: Identifiable, Equatable {
    enum Role: String { case host, member }

    let id: UUID
    let peerId: String
    let displayName: String
    let lastSeen: Date
    let role: Role
    let batteryLevel: Int?
    let hopCount: Int

    init(peerId: String, displayName: String, lastSeen: Date = .init(), role: Role, batteryLevel: Int? = nil, hopCount: Int = 0) {
        self.id = UUID()
        self.peerId = peerId
        self.displayName = displayName
        self.lastSeen = lastSeen
        self.role = role
        self.batteryLevel = batteryLevel
        self.hopCount = hopCount
    }
}

struct RideUiState {
    enum Status { case idle, hosting, joining, connected, error(String) }

    var status: Status = .idle
    var activeSession: RideSession? = nil
    var peers: [PeerInfo] = []
    var messages: [MeshMessage] = []
}

struct RideAdvertisement: Identifiable {
    let id = UUID()
    let rideId: String
    let rideName: String
    let hostName: String
    let signalStrength: Int
    let connectedPeers: Int
}

struct RideJoinRequest {
    let rideId: String
    let rideCode: String
    let riderName: String
    let pin: String?
}
