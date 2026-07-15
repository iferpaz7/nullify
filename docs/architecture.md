# Architecture

## Overview

Nullify uses Android's `CallScreeningService` to intercept incoming calls. Each
call is evaluated against a local whitelist stored in Room. If the number is not
authorized, the call is silently rejected.

```
Incoming call
      │
      ▼
NullifyScreeningService
      │
      ├── Emergency? ──> Allow
      ├── In whitelist? ──> Allow
      └── Not authorized ──> Block
```

## Modules

### composeApp (Shared KMP)

| Source set    | Contents                                                |
|---------------|---------------------------------------------------------|
| `commonMain`  | `EcuadorPhoneUtils`, `AllowedContact`, `ContactDao`,    |
|               | `NullifyDatabase`, `NullifyViewModel`, `WhitelistScreen`|
| `androidMain` | `DynamicColorScheme`, `DatabaseFactory`                 |
| `iosMain`     | `DynamicColorScheme`, `DatabaseFactory`,                |
|               | `MainViewController`                                    |

### androidApp (Android entry)

- `MainActivity` — entry point, requests CallScreening role
- `NullifyScreeningService` — call screening service
- `ContactSyncWorker` — periodic contact sync
- `NullifyApp` — Application + WorkManager config
- `MockConnectionService` — testing utility

## Platform support

| Feature                 | Android | iOS           |
|-------------------------|---------|---------------|
| Call Screening          | ✅      | ❌ (no API)   |
| Contact sync            | ✅      | ❌ (no API)   |
| Compose UI              | ✅      | ✅            |
| Manual whitelist        | ✅      | ✅            |
| Local database          | ✅      | ✅            |
