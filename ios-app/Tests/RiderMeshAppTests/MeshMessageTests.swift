import XCTest
@testable import RiderMeshApp

final class MeshMessageTests: XCTestCase {
    func testMessageSerialization() throws {
        let message = MeshMessage(rideId: "ride", senderId: "alice", type: .text, payload: ["body": "hi"])
        let data = try JSONEncoder().encode(message)
        let decoded = try JSONDecoder().decode(MeshMessage.self, from: data)
        XCTAssertEqual(decoded.payload["body"], "hi")
    }

    func testStoreDropsDuplicates() {
        let store = MessageStore(capacity: 2)
        XCTAssertTrue(store.shouldProcess(UUID()))
        let duplicate = UUID()
        XCTAssertTrue(store.shouldProcess(duplicate))
        XCTAssertFalse(store.shouldProcess(duplicate))
    }
}
