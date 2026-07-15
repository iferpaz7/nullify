# Architecture

## Overview

Nullify uses Android's `CallScreeningService` to intercept incoming calls. Each
call is evaluated against a local allowlist (device contacts + system whitelist)
stored in Room. If the number is not authorized, the call is silently rejected.

```
Incoming call
      │
      ▼
NullifyScreeningService
      │
      ├── Private/hidden/no number? ──> Block
      ├── Emergency? ──> Allow
      ├── In allowlist (contacts + system)? ──> Allow
      └── Unknown number ──> Block
```

## Allowlist population

The allowlist has two sources:

- **Device contacts** — synced immediately on permission grant and then
  periodically every 6 hours via WorkManager (`ContactSyncWorker`)
- **System whitelist** — prepopulated with Ecuadorian bank/utility numbers
  (BGR, Banco Pichincha, Produbanco, etc.) on first database creation

## Modules

### composeApp (Shared KMP)

| Source set    | Contents                                                |
|---------------|---------------------------------------------------------|
| `commonMain`  | `EcuadorPhoneUtils`, `AllowedContact`, `ContactDao`,    |
|               | `NullifyDatabase`, `NullifyViewModel`, `WhitelistScreen`,|
|               | `App`, `Color`, `Theme`, `Type`                        |
| `androidMain` | `getDynamicColorScheme`, `DatabaseFactory`              |
| `iosMain`     | `getDynamicColorScheme`, `DatabaseFactory`,             |
|               | `MainViewController`                                   |

### androidApp (Android entry)

- `MainActivity` — entry point, requests CallScreening role + permissions
- `NullifyScreeningService` — call screening service (allowlist mode)
- `ContactSyncWorker` — immediate + periodic contact sync
- `NullifyApp` — Application + WorkManager config
- `MockConnectionService` — testing utility

## Platform support

| Feature                 | Android | iOS           |
|-------------------------|---------|---------------|
| Call Screening          | ✅      | ❌ (no API)   |
| Contact sync            | ✅      | ❌ (no API)   |
| Compose UI              | ✅      | ✅            |
| Manual allowlist        | ✅      | ✅            |
| Local database          | ✅      | ✅            |
