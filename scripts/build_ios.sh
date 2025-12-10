#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
IOS_DIR="$PROJECT_ROOT/ios-app"
SCHEME="RiderMeshApp"
DESTINATION=${DESTINATION:-"platform=iOS Simulator,name=iPhone 15"}

if ! command -v xcodebuild >/dev/null 2>&1; then
  echo "xcodebuild is required (install Xcode command-line tools)." >&2
  exit 1
fi

if ! command -v xcodegen >/dev/null 2>&1; then
  cat <<'EOF' >&2
XcodeGen is required to materialize the Xcode project.
Install via `brew install xcodegen` then rerun this script.
EOF
  exit 1
fi

pushd "$IOS_DIR" >/dev/null

if [[ ! -d RiderMeshApp.xcodeproj ]]; then
  xcodegen generate
fi

xcodebuild \
  -scheme "$SCHEME" \
  -destination "$DESTINATION" \
  -configuration Debug \
  clean test

popd >/dev/null

echo "✅ iOS build + tests completed for scheme $SCHEME at destination $DESTINATION"
