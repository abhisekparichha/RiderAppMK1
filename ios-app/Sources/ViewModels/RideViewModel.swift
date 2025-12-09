import Foundation
import SwiftUI
import Combine

final class RideViewModel: ObservableObject {
    @Published private(set) var state = RideUiState()
    @Published var navPath: [RideRoute] = []
    @Published var discoveredRides: [RideAdvertisement] = []

    private let meshManager: MeshManaging
    private let cryptoManager: CryptoManager
    private var cancellables: Set<AnyCancellable> = []

    init(meshManager: MeshManaging = MCRideMeshManager(), cryptoManager: CryptoManager = CryptoManager()) {
        self.meshManager = meshManager
        self.cryptoManager = cryptoManager
        bind()
    }

    private func bind() {
        meshManager.messages
            .receive(on: DispatchQueue.main)
            .sink { [weak self] message in
                self?.mutateState { $0.messages.append(message) }
            }
            .store(in: &cancellables)

        meshManager.peers
            .receive(on: DispatchQueue.main)
            .sink { [weak self] peers in self?.mutateState { $0.peers = peers } }
            .store(in: &cancellables)

        meshManager.sessionState
            .receive(on: DispatchQueue.main)
            .sink { [weak self] session in
                self?.mutateState {
                    $0.activeSession = session
                    $0.status = session == nil ? .idle : .connected
                }
            }
            .store(in: &cancellables)
    }

    func createRide(rideName: String, hostName: String, pin: String?) {
        let bundle = cryptoManager.generateRideKey()
        mutateState { $0.status = .hosting }
        meshManager.startHosting(
            config: RideHostConfig(rideName: rideName, hostName: hostName, pin: pin, rideKey: bundle.key, rideCode: bundle.joinCode)
        )
        mutateState {
            $0.activeSession = RideSession(rideId: bundle.rideId, rideName: rideName, hostId: hostName, isHost: true, joinCode: bundle.joinCode, key: bundle.key)
        }
        navPath.append(.dashboard)
    }

    func joinRide(request: RideJoinRequest) {
        mutateState { $0.status = .joining }
        meshManager.joinRide(request: request)
        navPath.append(.dashboard)
    }

    func sendText(_ body: String) {
        guard let session = state.activeSession else { return }
        let message = MeshMessage(rideId: session.rideId, senderId: session.hostId, type: .text, payload: ["body": body])
        meshManager.send(message: message)
    }

    func resetRide() {
        meshManager.stop()
        state = RideUiState()
        navPath = []
    }

    func updateDisplayName(_ name: String) {
        // TODO: persist preferred rider name + sync to presence payload
    }

    func toggleAutoJoin(_ enabled: Bool) {
        // TODO: remember preference and auto-accept host invites when mesh discovery matches
    }

    func toggleVoice(_ enabled: Bool) {
        // TODO: wire into upcoming Wi-Fi Direct / Multipeer voice pipeline
    }

    private func mutateState(_ mutation: (inout RideUiState) -> Void) {
        var copy = state
        mutation(&copy)
        state = copy
    }
}
