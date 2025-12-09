import Foundation
import CryptoKit

final class CryptoManager {
    func generateRideKey() -> RideKeyBundle {
        let keyData = Data((0..<32).map { _ in UInt8.random(in: 0...255) })
        let rideCode = keyData.prefix(5).map { String(format: "%02X", $0) }.joined()
        return RideKeyBundle(rideId: rideCode, joinCode: rideCode, key: keyData)
    }

    func encrypt(message: MeshMessage, key: Data) throws -> EncryptedPayload {
        let symmetricKey = SymmetricKey(data: key)
        let plaintext = try JSONEncoder().encode(message.payload)
        let sealedBox = try AES.GCM.seal(plaintext, using: symmetricKey)
        return EncryptedPayload(nonce: sealedBox.nonce.data, ciphertext: sealedBox.ciphertext, tag: sealedBox.tag)
    }

    func decrypt(payload: EncryptedPayload, key: Data) throws -> [String: String] {
        let symmetricKey = SymmetricKey(data: key)
        let nonce = try AES.GCM.Nonce(data: payload.nonce)
        let sealedBox = try AES.GCM.SealedBox(nonce: nonce, ciphertext: payload.ciphertext, tag: payload.tag)
        let plaintext = try AES.GCM.open(sealedBox, using: symmetricKey)
        return try JSONDecoder().decode([String: String].self, from: plaintext)
    }
}

struct RideKeyBundle {
    let rideId: String
    let joinCode: String
    let key: Data
}

struct EncryptedPayload: Codable {
    let nonce: Data
    let ciphertext: Data
    let tag: Data
}
