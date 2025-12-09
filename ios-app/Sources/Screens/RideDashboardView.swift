import SwiftUI

struct RideDashboardView: View {
    let state: RideUiState
    let session: RideSession
    @EnvironmentObject private var viewModel: RideViewModel
    @State private var message = ""

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(session.rideName)
                .font(.title2)
            Text("Code: \(session.joinCode)")
                .font(.caption)
            PeerListView(peers: state.peers)
            MessageListView(messages: state.messages)
                .frame(maxHeight: 320)
            TextField("Broadcast message", text: $message)
                .textFieldStyle(.roundedBorder)
            HStack {
                Button("Send") {
                    viewModel.sendText(message)
                    message.removeAll()
                }
                .disabled(message.isEmpty)
                Spacer()
                Button("Debug") { viewModel.navPath.append(.debug) }
            }
            Button("Leave Ride", role: .destructive) {
                viewModel.resetRide()
            }
            Spacer()
        }
        .padding()
        .navigationTitle("Ride Dashboard")
    }
}

private struct PeerListView: View {
    let peers: [PeerInfo]

    var body: some View {
        GroupBox("Peers") {
            ForEach(peers) { peer in
                HStack {
                    Text(peer.displayName)
                    Spacer()
                    Text(peer.role.rawValue)
                        .font(.caption)
                }
            }
        }
    }
}

private struct MessageListView: View {
    let messages: [MeshMessage]

    var body: some View {
        List(messages) { message in
            VStack(alignment: .leading) {
                Text(message.senderId)
                    .font(.caption)
                Text(message.payload["body"] ?? "<encrypted>")
            }
        }
        .listStyle(.plain)
    }
}
