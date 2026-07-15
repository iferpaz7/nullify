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

## UI design

Nullify uses **Glassmorphism** — a modern design language where surfaces appear
as frosted glass floating over a soft gradient background. This style was chosen
because its transparency metaphor communicates the app's core function (call
screening/firewall) and aligns with security-focused design patterns.

### Implementation

| Element          | Treatment                                                |
|------------------|----------------------------------------------------------|
| Background       | `Brush.verticalGradient` (green → teal → blue)           |
| Cards            | `surface.copy(alpha = 0.82)` light / `0.70` dark         |
| Card borders     | `GlassBorderLight` (30% white) / `GlassBorderDark` (10%) |
| Card shape       | `RoundedCornerShape(16.dp)`                              |
| TopAppBar        | Transparent background, `HorizontalDivider` border       |
| Text contrast    | WCAG AA 4.5:1 via ≥82% (light) / 70% (dark) card opacity |
| Theme toggle     | TopAppBar icon cycles: System → Light → Dark → System    |

The `ThemeMode` enum (`System | Light | Dark`) controls the theme independently
of the device setting. The `next()` extension function cycles through modes.
The gradient background and glass-surface alpha adapt per mode.

### Why Glassmorphism over alternatives

| Style          | Verdict | Reason                                                     |
|----------------|---------|------------------------------------------------------------|
| **Glassmorphism** | ✅ Applied | Transparent, secure feel; works cross-platform in Compose |
| Liquid Glass   | ❌ Rejected | Requires real-time shaders (Metal/Skia); no KMP API       |
| Neumorphism    | ❌ Rejected | Low contrast → fails WCAG AA; outdated trend              |

## Platform support

| Feature                 | Android | iOS           |
|-------------------------|---------|---------------|
| Call Screening          | ✅      | ❌ (no API)   |
| Contact sync            | ✅      | ❌ (no API)   |
| Compose UI              | ✅      | ✅            |
| Manual allowlist        | ✅      | ✅            |
| Local database          | ✅      | ✅            |
