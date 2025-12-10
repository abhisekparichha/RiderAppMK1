#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ANDROID_DIR="$PROJECT_ROOT/android-app"
GRADLE_WRAPPER="$ANDROID_DIR/gradlew"

if [[ ! -x "$GRADLE_WRAPPER" ]]; then
  cat <<'EOF' >&2
Gradle wrapper (android-app/gradlew) is missing.
Generate it once by running `cd android-app && gradle wrapper` or install Android Studio
and let it create the wrapper, then rerun this script.
EOF
  exit 1
fi

pushd "$ANDROID_DIR" >/dev/null

./gradlew --stacktrace assembleDebug

APK_PATH="$ANDROID_DIR/app/build/outputs/apk/debug/app-debug.apk"

if [[ ! -f "$APK_PATH" ]]; then
  echo "APK not found at $APK_PATH" >&2
  exit 1
fi

cp "$APK_PATH" "$PROJECT_ROOT/app-debug.apk"
echo "✅ APK built: $APK_PATH"
echo "📦 Copied to: $PROJECT_ROOT/app-debug.apk"

popd >/dev/null
