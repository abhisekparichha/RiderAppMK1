import SwiftUI

struct CreateRideScreen: View {
    @State private var rideName = ""
    @State private var hostName = ""
    @State private var pin = ""
    let onCreate: (String, String, String?) -> Void

    var body: some View {
        Form {
            Section("Ride Details") {
                TextField("Ride name", text: $rideName)
                TextField("Your name", text: $hostName)
                TextField("PIN (optional)", text: $pin)
            }
            Button("Start Hosting") {
                onCreate(rideName, hostName, pin.isEmpty ? nil : pin)
            }
            .disabled(rideName.isEmpty || hostName.isEmpty)
        }
        .navigationTitle("Create Ride")
    }
}
