import SwiftUI

struct SettingsView: View {
    let state: RideUiState
    @EnvironmentObject private var viewModel: RideViewModel
    @State private var displayName = ""
    @State private var autoJoin = false
    @State private var voiceEnabled = false

    var body: some View {
        Form {
            Section("Profile") {
                TextField("Display name", text: $displayName)
                Button("Save") { viewModel.updateDisplayName(displayName) }
            }
            Section("Automation") {
                Toggle("Auto-join host rides", isOn: $autoJoin)
                    .onChange(of: autoJoin) { viewModel.toggleAutoJoin($0) }
                Toggle("Push-to-talk voice", isOn: $voiceEnabled)
                    .onChange(of: voiceEnabled) { viewModel.toggleVoice($0) }
            }
            Section {
                Button("Debug info") { viewModel.navPath.append(.debug) }
            }
        }
        .navigationTitle("Settings")
    }
}
