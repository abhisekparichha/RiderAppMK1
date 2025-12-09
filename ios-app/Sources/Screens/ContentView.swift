import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var viewModel: RideViewModel

    var body: some View {
        NavigationStack(path: $viewModel.navPath) {
            HomeScreen(rideState: viewModel.state)
                .navigationDestination(for: RideRoute.self) { route in
                    switch route {
                    case .create:
                        CreateRideScreen { rideName, hostName, pin in
                            viewModel.createRide(rideName: rideName, hostName: hostName, pin: pin)
                        }
                    case .join:
                        JoinRideScreen(advertisements: viewModel.discoveredRides) { join in
                            viewModel.joinRide(request: join)
                        }
                    case .dashboard:
                        if let session = viewModel.state.activeSession {
                            RideDashboardView(state: viewModel.state, session: session)
                        }
                    case .settings:
                        SettingsView(state: viewModel.state)
                    case .debug:
                        DebugView(messages: viewModel.state.messages, peers: viewModel.state.peers)
                    }
                }
                .toolbar {
                    ToolbarItem(placement: .topBarTrailing) {
                        Button("Settings") { viewModel.navPath.append(.settings) }
                    }
                }
        }
    }
}

enum RideRoute: Hashable {
    case create, join, dashboard, settings, debug
}
