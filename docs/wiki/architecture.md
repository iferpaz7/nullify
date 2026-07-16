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

## Layer architecture

Following [Google's official app architecture recommendations](https://developer.android.com/topic/architecture/recommendations),
Nullify uses a **3-layer architecture** with unidirectional data flow (UDF):

```
┌──────────────────────────────────────────────────┐
│  UI Layer (Compose Multiplatform)                │
│  WhitelistScreen · CallLogScreen                 │
│  Collects UiState → renders UI                   │
│  Events flow up to ViewModel                     │
├──────────────────────────────────────────────────┤
│  Presentation Layer (ViewModel)                  │
│  NullifyViewModel                                │
│  Exposes StateFlow<UiState<T>>                   │
│  Injected CoroutineDispatcher                    │
│  Delegates to Repository interfaces              │
├──────────────────────────────────────────────────┤
│  Data Layer (Repository + DAO + Room)            │
│  ContactRepository · CallLogRepository           │
│  ContactDao · CallLogDao · NullifyDatabase       │
│  AllowedContact · CallLogEntry (entities)        │
├──────────────────────────────────────────────────┤
│  Platform Layer (Android-specific)               │
│  NullifyApp · MainActivity · ScreeningService    │
│  ContactSyncWorker · DatabaseFactory             │
└──────────────────────────────────────────────────┘
```

**State flows down; events flow up.** The ViewModel transforms data from
repositories into a sealed `UiState` (`Loading | Success<T> | Error`) consumed
by Compose screens via `collectAsState()`. User actions are dispatched as
method calls back to the ViewModel, never as events to the UI.

### Dependency injection (manual)

All singletons are created at the Application level in `NullifyApp`:

| Singleton                | Created from                  |
|--------------------------|-------------------------------|
| `NullifyDatabase`        | `DatabaseFactory`             |
| `ContactRepository`      | `ContactRepositoryImpl(dao)`  |
| `CallLogRepository`      | `CallLogRepositoryImpl(dao)`  |
| `NullifyViewModel`       | `NullifyViewModelFactory` via `by viewModels` |

The `NullifyViewModel` receives `CoroutineDispatcher` (default `Dispatchers.IO`)
as a constructor parameter, making it testable by substituting test dispatchers
([per official coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#inject-dispatchers)).

### Modules

#### composeApp (Shared KMP)

| Source set    | Contents                                                |
|---------------|---------------------------------------------------------|
| `commonMain`  | `data/` (entities, DAOs, database, repositories),       |
|               | `ui/` (ViewModel, screens, theme, UiState),             |
|               | `utils/`, `App.kt`                                      |
| `androidMain` | `getDynamicColorScheme`, `DatabaseFactory`              |
| `iosMain`     | `getDynamicColorScheme`, `DatabaseFactory`,             |
|               | `MainViewController`                                   |

#### androidApp (Android entry)

- `MainActivity` — entry point, requests CallScreening role + permissions
- `NullifyScreeningService` — call screening service (allowlist mode)
- `ContactSyncWorker` — immediate + periodic contact sync
- `NullifyApp` — Application + WorkManager config + repository wiring
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

## Call log

Every incoming call is logged to the `call_log` table (`CallLogEntry` entity) with:

| Field       | Description                           |
|-------------|---------------------------------------|
| `phoneNumber` | Raw caller ID string                 |
| `result`      | `ALLOWED` or `BLOCKED`              |
| `reason`      | Why the call was allowed/blocked    |
| `timestamp`   | Epoch millis                         |

Logs are visible in the **Historial** tab (bottom navigation). Maximum 200 entries
shown, oldest are evicted by Room's `LIMIT 200` query.

## Sealed UI state

All ViewModel states use the `UiState<T>` sealed interface:

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

Screens handle all three branches via `when`:
- **Loading** — shown briefly while Room emits the initial query result
- **Success** — renders the actual data (or empty-state placeholder)
- **Error** — displays an error message (catches downstream Flow exceptions)

This follows the [guide to app architecture](https://developer.android.com/topic/architecture/ui-layer/stateholders#ui-state) recommendation of explicit state modeling.

## Screening performance

On first call arrival the process may cold-start. To avoid the database lazy-init
penalty, `NullifyApp.prewarmDatabase()` opens the SQLite connection eagerly in a
background thread during `Application.onCreate()`. The `SELECT EXISTS` query in
`isNumberAllowed()` is synchronous (no `runBlocking` overhead), so each
screening decision typically completes in **<10ms** after prewarming.

## Navigation

Two-tab bottom `NavigationBar` (defined in `App.kt`):
- **Lista Blanca** — manage manual exceptions, view synced contacts
- **Historial** — view recent call screening decisions with block/allow status

Tab state is held locally in `NullifyApp` via `remember { mutableStateOf(Tab.Whitelist) }`.
Each tab receives `viewModel` and the current `themeMode` for glass border colors.

| Feature                 | Android | iOS           |
|-------------------------|---------|---------------|
| Call Screening          | ✅      | ❌ (no API)   |
| Contact sync            | ✅      | ❌ (no API)   |
| Compose UI              | ✅      | ✅            |
| Manual allowlist        | ✅      | ✅            |
| Local database          | ✅      | ✅            |
| Call log                | ✅      | ✅            |
| Bottom navigation       | ✅      | ✅            |
