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

## CLI Build & Test Scripts

Scripts live in `scripts/` to help CI or headless testing:

- `scripts/build_android.sh`: Assembles a debug APK via the Gradle wrapper inside `android-app/`. Run `cd android-app && gradle wrapper` once if `gradlew` is missing, then execute the script from the repo root (or any path). Resulting APK is copied to the repo root as `app-debug.apk`.
- `scripts/build_ios.sh`: Generates the Xcode project with XcodeGen (if needed) and runs `xcodebuild clean test` for the `RiderMeshApp` scheme against the default simulator (`platform=iOS Simulator,name=iPhone 15`). Override the destination by setting the `DESTINATION` env var before running.


