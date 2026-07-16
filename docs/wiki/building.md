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

Local debug builds default to version `1.1.0` / code `1`. In CI, these are
derived automatically from git tags and run numbers.

### Versioning strategy (CI)

The CI pipeline derives versions as follows:

| Trigger | Version name | Version code |
|---------|-------------|--------------|
| Tag push `v*` | The tag itself, e.g. `1.2.3` | `github.run_number` |
| Push to `main` | MAJOR.MINOR from latest tag + `run_number` as PATCH, e.g. `1.1.42` | `github.run_number` |

This guarantees that after a tagged release (e.g. `v1.1.0`), all subsequent
auto-releases on `main` carry the same MAJOR.MINOR (`1.1.x`) and only the
PATCH (run number) increments. The `versionCode` always increases because it
uses the globally monotonic `github.run_number`.

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

The version name on `main` pushes dynamically derives its MAJOR.MINOR from the
[latest git tag](https://git-scm.com/docs/git-describe), so auto-releases
always match the current release train (e.g. after `v1.1.0`, auto-releases are
`1.1.<run_number>`).

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
