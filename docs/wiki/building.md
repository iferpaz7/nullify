# Building & deployment

## Android

```bash
# Debug
./gradlew :androidApp:assembleDebug

# Release (requires configured keystore)
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

## Signing a release

Configure `androidApp/signing.properties` or use environment variables:

```properties
storeFile=/path/to/keystore.jks
storePassword=...
keyAlias=...
keyPassword=...
```
