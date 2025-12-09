# Rider Mesh

Experimental offline mesh networking concept for Android and iOS riders. Android uses Kotlin + Jetpack Compose inside `android-app/`, iOS uses SwiftUI + MultipeerConnectivity via XcodeGen config in `ios-app/`. See `ARCHITECTURE.md` for the full product and networking breakdown.

## Quickstart

### Android
1. Open `android-app/` in Android Studio Giraffe+.
2. Sync Gradle (AGP 8.3, Kotlin 1.9.22).
3. Deploy the `app` configuration to two nearby devices, enable Bluetooth + Wi-Fi.

### iOS
1. Install [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`).
2. From `ios-app/`, run `xcodegen generate` and open the generated project in Xcode 15.
3. Enable Bluetooth/Wi-Fi entitlements when prompted and run on two devices.

Unit tests live under `android-app/app/src/test` and `ios-app/Tests`. Use the debug screens to inspect message routing and peer health.
