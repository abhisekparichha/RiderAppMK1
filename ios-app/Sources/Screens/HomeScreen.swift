import SwiftUI

struct HomeScreen: View {
    let rideState: RideUiState
    @EnvironmentObject private var viewModel: RideViewModel

    var body: some View {
        VStack(spacing: 16) {
            Text("Rider Mesh")
                .font(.largeTitle)
            GroupBox {
                VStack(alignment: .leading, spacing: 8) {
                    Text("Current ride")
                        .font(.headline)
                    if let session = rideState.activeSession {
                        Text("\(session.rideName) · code \(session.joinCode)")
                    } else {
                        Text("Not connected")
                            .foregroundStyle(.secondary)
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            Button("Create Ride") { viewModel.navPath.append(.create) }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)
            Button("Join Ride") { viewModel.navPath.append(.join) }
                .frame(maxWidth: .infinity)
            Button("Open Dashboard") { viewModel.navPath.append(.dashboard) }
                .frame(maxWidth: .infinity)
            Spacer()
        }
        .padding()
        .background(Color.black.opacity(0.95))
        .foregroundStyle(.white)
    }
}
