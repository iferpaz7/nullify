# Nullify

Call firewall for Ecuador. Blocks unwanted incoming calls and allows only authorized numbers via `CallScreeningService`.

## Stack

| Layer       | Technology                                                               |
|-------------|--------------------------------------------------------------------------|
| UI          | Compose Multiplatform 1.11.1 (Material 3, Dynamic Color)                 |
| Language    | Kotlin 2.4.10 (multiplatform)                                            |
| Platforms   | Android 10+ (API 29), iOS 15+ (shared framework only)                    |
| Persistence | Room 2.8.4 (KMP) + SQLite Bundled                                        |
| Background  | WorkManager 2.10.0 (contact sync every 6 h)                              |
| Build       | Gradle 9.1.0 + AGP 9.0.1                                                 |

## Project structure

```
nullify/
├── composeApp/              # Shared KMP module
│   └── src/
│       ├── commonMain/      # Business logic, data, UI (shared)
│       ├── androidMain/     # Android-specific implementations
│       └── iosMain/         # iOS-specific implementations
├── androidApp/              # Android entry point (AGP 9)
│   └── src/main/
│       ├── kotlin/          # Activity, services, workers
│       └── res/             # Android resources
├── docs/                    # Documentation
└── .github/workflows/       # CI / Wiki sync
```

## Requirements

- JDK 21
- Android SDK 36
- Xcode 16+ (iOS build only)

## Build

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS (macOS)
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
```

## License

Apache 2.0
