# Setup & configuration

## Requirements

- **JDK**: 21 (OpenJDK 21.0.11)
- **Android SDK**: API 36 (`platforms;android-35` + `platforms;android-36`)
- **Gradle**: 9.1.0 (wrapper included)
- **Xcode**: 16+ (iOS targets only)

## Environment

Ensure `ANDROID_HOME` points to your Android SDK:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
```

## Getting started

```bash
git clone <repo-url>
cd nullify
./gradlew :androidApp:assembleDebug
```

## IDE

Open the project root in Android Studio Ladybug (2025.3+) or IntelliJ IDEA
2025.3+ with the Kotlin Multiplatform plugin installed.
