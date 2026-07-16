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
and pull request to `main`. On every push to `main`, it builds the APK and
automatically creates a tag + GitHub Release (debug-signed). On version tags
(`v*`) it builds a **signed** release APK using the configured keystore.
Documentation changes (`docs/**`) are excluded from triggering builds.

### Database schema changes

When schema changes occur (new entities, new columns), Room rebuilds the
database via `fallbackToDestructiveMigration()`. This means app data **is
lost** on upgrades during development. For production, write an explicit
`Migration` object instead.

### Current toolchain

| Tool    | Version |
|---------|---------|
| AGP     | 9.1.0   |
| Kotlin  | 2.4.10  |
| Gradle  | 9.3.1   |
| R8      | 9.1.x   |
