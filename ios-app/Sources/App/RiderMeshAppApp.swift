import SwiftUI

@main
struct RiderMeshAppApp: App {
    @StateObject private var viewModel = RideViewModel()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(viewModel)
        }
    }
}
