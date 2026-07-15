# Building & deployment

## Android

```bash
# Debug
./gradlew :androidApp:assembleDebug

# Release (requires signing config)
./gradlew :androidApp:assembleRelease

# APK output:
#   androidApp/build/outputs/apk/debug/androidApp-debug.apk
#   androidApp/build/outputs/apk/release/androidApp-release.apk
```

## iOS (macOS)

```bash
# Generate debug framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64

# Release framework
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```

The framework is generated at `composeApp/build/bin/iosSimulatorArm64/debugFramework/`.

## Version overrides

Version name and code can be set via environment variables:

```bash
VERSION_CODE=42 VERSION_NAME=2.0.0 ./gradlew :androidApp:assembleRelease
```

In CI, these are derived automatically from git tags and run numbers.

## Signing a release

Configure via environment variables:

| Variable | Description |
|---|---|
| `KEYSTORE_PATH` | Path to the keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Signing key alias |
| `KEY_PASSWORD` | Signing key password |

On CI (tagged releases) the keystore is decoded from `KEYSTORE_BASE64` secret
and the APK is automatically uploaded as a GitHub Release artifact.

## CI pipeline

The GitHub Actions workflow (`.github/workflows/build.yml`) runs on every push
and pull request to `main`. On version tags (`v*`) it builds a signed release
APK and creates a GitHub Release. Documentation changes (`docs/**`) are
excluded from triggering builds.
