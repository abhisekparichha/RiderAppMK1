import Foundation
import MultipeerConnectivity
import Combine
import UIKit

protocol MeshManaging {
    var messages: AnyPublisher<MeshMessage, Never> { get }
    var peers: AnyPublisher<[PeerInfo], Never> { get }
    var sessionState: AnyPublisher<RideSession?, Never> { get }

    func startHosting(config: RideHostConfig)
    func discoverRides()
    func joinRide(request: RideJoinRequest)
    func send(message: MeshMessage)
    func stop()
}

struct RideHostConfig {
    let rideName: String
    let hostName: String
    let pin: String?
    let rideKey: Data
    let rideCode: String
}

final class MCRideMeshManager: NSObject, MeshManaging {
    private let serviceType = "ridemesh"
    private lazy var peerID = MCPeerID(displayName: UIDevice.current.name)
    private var advertiser: MCNearbyServiceAdvertiser?
    private var browser: MCNearbyServiceBrowser?
    private var session: MCSession?

    private let messageSubject = PassthroughSubject<MeshMessage, Never>()
    private let peerSubject = CurrentValueSubject<[PeerInfo], Never>([])
    private let sessionSubject = CurrentValueSubject<RideSession?, Never>(nil)

    var messages: AnyPublisher<MeshMessage, Never> { messageSubject.eraseToAnyPublisher() }
    var peers: AnyPublisher<[PeerInfo], Never> { peerSubject.eraseToAnyPublisher() }
    var sessionState: AnyPublisher<RideSession?, Never> { sessionSubject.eraseToAnyPublisher() }

    func startHosting(config: RideHostConfig) {
        let info: [String: String] = ["rideId": config.rideCode, "rideName": config.rideName]
        advertiser = MCNearbyServiceAdvertiser(peer: peerID, discoveryInfo: info, serviceType: serviceType)
        advertiser?.delegate = self
        advertiser?.startAdvertisingPeer()
        startSession()
        sessionSubject.send(RideSession(rideId: config.rideCode, rideName: config.rideName, hostId: config.hostName, isHost: true, joinCode: config.rideCode, key: config.rideKey))
    }

    func discoverRides() {
        browser = MCNearbyServiceBrowser(peer: peerID, serviceType: serviceType)
        browser?.delegate = self
        browser?.startBrowsingForPeers()
    }

    func joinRide(request: RideJoinRequest) {
        discoverRides()
        // Real implementation would filter advertisement list and invite.
        // Placeholder invites immediately once browser finds host.
    }

    func send(message: MeshMessage) {
        guard let session else { return }
        guard let data = try? JSONEncoder().encode(message) else { return }
        try? session.send(data, toPeers: session.connectedPeers, with: .reliable)
        messageSubject.send(message)
    }

    func stop() {
        advertiser?.stopAdvertisingPeer()
        browser?.stopBrowsingForPeers()
        advertiser = nil
        browser = nil
        session?.disconnect()
        session = nil
        sessionSubject.send(nil)
    }

    private func startSession() {
        let session = MCSession(peer: peerID, securityIdentity: nil, encryptionPreference: .required)
        session.delegate = self
        self.session = session
    }
}

extension MCRideMeshManager: MCNearbyServiceAdvertiserDelegate, MCNearbyServiceBrowserDelegate, MCSessionDelegate {
    func advertiser(_ advertiser: MCNearbyServiceAdvertiser, didReceiveInvitationFromPeer peerID: MCPeerID, withContext context: Data?, invitationHandler: @escaping (Bool, MCSession?) -> Void) {
        invitationHandler(true, session)
    }

    func advertiser(_ advertiser: MCNearbyServiceAdvertiser, didNotStartAdvertisingPeer error: Error) {
        print("Advertiser error: \(error)")
    }

    func browser(_ browser: MCNearbyServiceBrowser, foundPeer peerID: MCPeerID, withDiscoveryInfo info: [String : String]?) {
        guard let session else { return }
        browser.invitePeer(peerID, to: session, withContext: nil, timeout: 10)
    }

    func browser(_ browser: MCNearbyServiceBrowser, lostPeer peerID: MCPeerID) {}
    func browser(_ browser: MCNearbyServiceBrowser, didNotStartBrowsingForPeers error: Error) { print("Browse error: \(error)") }

    func session(_ session: MCSession, peer peerID: MCPeerID, didChange state: MCSessionState) {
        let peerInfo = PeerInfo(peerId: peerID.displayName, displayName: peerID.displayName, role: state == .connected ? .member : .member)
        switch state {
        case .connected:
            var peers = peerSubject.value.filter { $0.peerId != peerInfo.peerId }
            peers.append(peerInfo)
            peerSubject.send(peers)
        case .notConnected:
            peerSubject.send(peerSubject.value.filter { $0.peerId != peerInfo.peerId })
        default:
            break
        }
    }

    func session(_ session: MCSession, didReceive data: Data, fromPeer peerID: MCPeerID) {
        guard let message = try? JSONDecoder().decode(MeshMessage.self, from: data) else { return }
        messageSubject.send(message)
    }

    func session(_ session: MCSession, didReceive stream: InputStream, withName streamName: String, fromPeer peerID: MCPeerID) {}
    func session(_ session: MCSession, didStartReceivingResourceWithName resourceName: String, fromPeer peerID: MCPeerID, with progress: Progress) {}
    func session(_ session: MCSession, didFinishReceivingResourceWithName resourceName: String, fromPeer peerID: MCPeerID, at localURL: URL?, withError error: Error?) {}
    func session(_ session: MCSession, didReceive certificate: [Any]?, fromPeer peerID: MCPeerID, certificateHandler: @escaping (Bool) -> Void) {
        certificateHandler(true)
    }
}
