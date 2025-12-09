import SwiftUI

struct DebugView: View {
    let messages: [MeshMessage]
    let peers: [PeerInfo]

    var body: some View {
        List {
            Section("Peers") {
                ForEach(peers) { peer in
                    VStack(alignment: .leading) {
                        Text(peer.displayName)
                        Text("Hop \(peer.hopCount) · last \(peer.lastSeen.timeIntervalSince1970)")
                            .font(.caption)
                    }
                }
            }
            Section("Raw Messages") {
                ForEach(messages) { message in
                    VStack(alignment: .leading) {
                        Text("\(message.type.rawValue) from \(message.senderId)")
                        Text("TTL: \(message.ttl)")
                            .font(.caption)
                    }
                }
            }
        }
        .navigationTitle("Debug")
    }
}
