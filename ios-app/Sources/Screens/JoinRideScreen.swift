import SwiftUI

struct JoinRideScreen: View {
    let advertisements: [RideAdvertisement]
    let onJoin: (RideJoinRequest) -> Void
    @State private var rideId = ""
    @State private var rideCode = ""
    @State private var riderName = ""
    @State private var pin = ""

    var body: some View {
        List {
            Section("Nearby Rides") {
                ForEach(advertisements) { ad in
                    VStack(alignment: .leading) {
                        Text(ad.rideName)
                            .font(.headline)
                        Text("Host: \(ad.hostName) · RSSI \(ad.signalStrength)")
                            .font(.caption)
                    }
                    .onTapGesture {
                        rideId = ad.rideId
                    }
                }
            }
            Section("Manual Join") {
                TextField("Ride ID", text: $rideId)
                TextField("Join code", text: $rideCode)
                TextField("Your name", text: $riderName)
                TextField("PIN", text: $pin)
                Button("Join") {
                    onJoin(RideJoinRequest(rideId: rideId, rideCode: rideCode, riderName: riderName, pin: pin.isEmpty ? nil : pin))
                }
                .disabled(rideId.isEmpty || rideCode.isEmpty || riderName.isEmpty)
            }
        }
        .navigationTitle("Join Ride")
    }
}
