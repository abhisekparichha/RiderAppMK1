# Offline Rider Mesh – Architecture Overview

## Platform Strategy
We ship dedicated native apps for Android (`android-app/`) and iOS (`ios-app/`). Each app follows a shared conceptual model (sessions, peers, messages, crypto) but implements networking with the platform-native radio stacks:

- **Android**: Bluetooth Low Energy (BLE) for ride discovery/control and Wi-Fi Direct for high-bandwidth data (text, optional voice). Networking is encapsulated in `network/` with `MeshManager` coordinating BLE advertising/scan (`BleRideAdvertiser`, `BleRideScanner`) and Wi-Fi Direct sessions (`WifiDirectCoordinator`).
- **iOS**: [`MultipeerConnectivity`](https://developer.apple.com/documentation/multipeerconnectivity) abstracts Bluetooth + Wi-Fi. `MCRideMeshManager` handles hosting, discovery, and relaying. BLE-specific control packets remain optional but the abstraction keeps parity with Android.

Shared business rules (ride/session state, serialization, encryption helpers) are mirrored on both platforms to keep behavior aligned.

## Key Concepts

| Concept | Description |
| --- | --- |
| `RideSession` | Local view of the current ride: metadata, encryption key, host/member role, peer table, connection state. |
| `PeerInfo` | Rider identity + live diagnostics (lastSeen timestamp, battery, hop count). |
| `MeshMessage` | Envelope for all payloads with dedup + TTL metadata. Types include `TEXT_MESSAGE`, `PRESENCE`, `CONTROL`, and future `VOICE_FRAME`. |
| `MeshManager` | Platform-specific service responsible for discovery, join/host lifecycle, transport binding, routing, and delivery of incoming messages into reactive streams. |
| `CryptoManager` | Generates ride keys, builds QR/join codes, performs AES-GCM encryption/decryption, and signs metadata (AAD) to prevent tampering. |

## Offline Mesh Design

### Discovery & Join Flow
1. **Host** chooses “Create Ride”, enters name/PIN. A 256-bit ride key + short ride code are generated.
2. Host starts advertising:
   - Android: BLE manufacturer payload carries rideId, hostId, flags; Wi-Fi Direct group owner is created simultaneously.
   - iOS: `MCNearbyServiceAdvertiser` advertises the same payload.
3. **Joiner** selects ride from BLE/Multipeer scan or scans the QR code. Join requests include rider name + optional PIN hashed with the rideId.
4. Host authorizes and shares Wi-Fi Direct credentials (Android) or accepts the Multipeer invitation (iOS). All peers derive the symmetric key from the join payload and immediately start encrypted messaging.

### Message Routing & Deduplication
- Messages enter the `MeshRouter` with `messageId = sha256(senderId + timestamp + nonce)`.
- Each node keeps a bounded LRU cache of recently seen IDs. If a message is unseen it is emitted to the UI, persisted in the local log, and optionally forwarded to neighbors (controlled by role + TTL).
- MVP uses a **host-centric star**: all members send to host; host re-broadcasts. The router abstraction already exposes `broadcast()` and `forward(to peers)` hooks so multi-hop metrics (RSSI, hop count) can be added later.

### Presence & Diagnostics
- Every device publishes a `PRESENCE_UPDATE` every 5 seconds containing `lastSeen`, `battery` (if available), and transport health (BLE/Wi-Fi Direct link status). The UI reflects this data in the rider list and debug graphs.

## Security Model
1. **Ride Key Generation**: `CryptoManager.generateRideKey()` creates 32 random bytes + `joinCode` (Base32 w/ checksum). The key is embedded in a QR payload `{rideId, rideKey, pinSaltedHash}`.
2. **Encryption**: All payloads are serialized JSON/CBOR, encrypted with AES-256-GCM. Associated data includes `messageType`, `rideId`, and `senderId` to prevent cross-ride replay.
3. **Key Distribution**: QR or manual join code exchange is mandatory. BLE advertisements never reveal the key. Optional PIN further hardens unauthorized joins.
4. **Future Hardening**: Doc outlines extension points for ECDH and per-peer session keys once multi-hop arrives.

## Voice / PTT Extension Points
- Android `VoiceChannel` and iOS `VoicePipeline` stubs sit beside the text transport. They share the ride key for encryption but keep their own pacing/jitter buffers. MVP ships with TODO hooks so we can plug in Opus streaming later without reworking the rest of the stack.

## Data Flow Summaries

### Create Ride
1. UI → `RideViewModel.createRide()`
2. ViewModel requests `MeshManager.startHosting(config)` + `CryptoManager.generateRideCredentials()`.
3. MeshManager starts BLE/Wi-Fi Direct or Multipeer advertising, exposes ride info as Flow/Combine publisher.
4. UI displays QR/join code; presence loop begins sending host status.

### Join Ride
1. UI selects discovered ride or scans QR.
2. `RideViewModel.joinRide()` calls `MeshManager.joinRide()`.
3. On success, CryptoManager stores the key in secure storage (Android EncryptedSharedPreferences, iOS Keychain).
4. Message + presence streams become active for the dashboard.

### Messaging
1. Text input builds `MeshMessage(type=TEXT, payload=EncryptedPayload)`.
2. `MeshManager.sendMessage()` emits over Wi-Fi Direct/Multipeer.
3. Receivers decrypt via CryptoManager, drop duplicates via `MessageStore`, update chat log, and forward if necessary.

## Testing Strategy
- Unit tests verify message serialization + TTL handling plus AES-GCM round trips (`android-app/app/src/test/...`, `ios-app/Tests/...`).
- A preview-only “Debug Mesh” screen visualizes peers and last raw frames to help manual testing with 3–5 phones.

## Repository Layout
```
ARCHITECTURE.md
README.md
android-app/
  settings.gradle.kts
  build.gradle.kts
  app/
    build.gradle.kts
    src/main/java/com/ridermesh/app/... (see README)
ios-app/
  project.yml (XcodeGen)
  Sources/
    RiderMeshAppApp.swift, ...
  Tests/
```

## Build Instructions (High Level)
- **Android**: Open `android-app/` in Android Studio Giraffe+, sync Gradle, run `app` configuration. App targets SDK 34, min SDK 28.
- **iOS**: Install [XcodeGen](https://github.com/yonaskolb/XcodeGen), run `xcodegen generate` inside `ios-app/`, open the generated `.xcodeproj`, select an iPhone target, and run. The project uses Swift 5.9 and SwiftUI. Multipeer entitlements must be accepted when prompted.

The remaining sections in this repo provide concrete code scaffolding for these components; see inline TODOs for next steps (multi-hop routing, Opus voice streaming, sensor fusion for distance, etc.).
