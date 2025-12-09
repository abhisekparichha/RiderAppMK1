import Foundation

struct MeshMessage: Identifiable, Codable, Equatable {
    enum MessageType: String, Codable {
        case text, presence, control, voice
    }

    let id: UUID
    let rideId: String
    let senderId: String
    let type: MessageType
    let timestamp: Date
    let ttl: Int
    let payload: [String: String]

    init(id: UUID = UUID(), rideId: String, senderId: String, type: MessageType, timestamp: Date = .init(), ttl: Int = 4, payload: [String: String]) {
        self.id = id
        self.rideId = rideId
        self.senderId = senderId
        self.type = type
        self.timestamp = timestamp
        self.ttl = ttl
        self.payload = payload
    }
}

final class MessageStore {
    private var ids: [UUID] = []
    private let capacity: Int

    init(capacity: Int = 256) {
        self.capacity = capacity
    }

    func shouldProcess(_ id: UUID) -> Bool {
        if ids.contains(id) { return false }
        ids.append(id)
        if ids.count > capacity { ids.removeFirst(ids.count - capacity) }
        return true
    }
}
