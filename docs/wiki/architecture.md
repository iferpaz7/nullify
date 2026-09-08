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

| Source set    | Contents                                                                  |
|---------------|---------------------------------------------------------------------------|
| `commonMain`  | `data/` (entities, DAOs, database, repositories),                         |
|               | `ui/` (ViewModel, screens, theme, UiState, `components/ContactPlaceholders`), |
|               | `utils/`, `App.kt`                                                        |
| `androidMain` | `getDynamicColorScheme`, `PlatformSystemBarsEffect`, `DatabaseFactory`    |
| `iosMain`     | `getDynamicColorScheme`, `PlatformSystemBarsEffect`, `DatabaseFactory`,   |
|               | `MainViewController`                                                     |

#### androidApp (Android entry)

- `MainActivity` — entry point with `enableEdgeToEdge()` full-bleed UI, role requests
- `NullifyScreeningService` — call screening service (allowlist mode)
- `ContactSyncWorker` — immediate + periodic contact sync
- `NullifyApp` — Application + WorkManager config + repository wiring
- `MockConnectionService` — testing utility

## UI design

Nullify uses **Glassmorphism** combined with **Material 3 (Material You)**. For full design system, tokens, and color role specifications, see [Design System & Material 3](design-system).

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
| Edge-to-Edge     | `enableEdgeToEdge()` + dynamic `PlatformSystemBarsEffect` |
| Placeholders     | `ContactListSkeleton` shimmer & `ContactAvatarPlaceholder`|

The `ThemeMode` enum (`System | Light | Dark`) controls the theme independently
of the device setting, with `LocalThemeIsDark` synchronizing all composables
and system bars contrast.

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
- **Loading** — displays animated glassmorphism skeleton loaders (`ContactListSkeleton`) with calibrated linear shimmer (`rememberShimmerBrush`)
- **Success** — renders contact cards with avatar initials (`ContactAvatarPlaceholder`) or rich contextual empty states (`ContactEmptyStatePlaceholder`) for empty whitelist or search misses
- **Error** — displays an error message with error container colors (catches downstream Flow exceptions)

This follows the [guide to app architecture](https://developer.android.com/topic/architecture/ui-layer/stateholders#ui-state) and [architecture recommendations](https://developer.android.com/topic/architecture/recommendations):
- **Lifecycle-safe flow collection**: Screens collect `UiState` via `collectAsStateWithLifecycle()` (from `androidx.lifecycle.compose`), halting collection when the UI is stopped/hidden.
- **Configuration change survival**: Ephemeral form state (`nameInput`, `numberInput`, `showError`) uses `rememberSaveable` to preserve user input across activity recreation.

## Screening performance

On first call arrival the process may cold-start. To avoid the database lazy-init
penalty, `NullifyApp.prewarmDatabase()` opens the SQLite connection eagerly in a
background thread during `Application.onCreate()`. The `SELECT EXISTS` query in
`isNumberAllowed()` is synchronous (no `runBlocking` overhead), so each
screening decision typically completes in **<10ms** after prewarming.

## Navigation & Inset Handling

Two-tab bottom `NavigationBar` (defined in `App.kt`):
- **Lista Blanca** — manage manual exceptions, view synced contacts
- **Historial** — view recent call screening decisions with block/allow status

Tab state is held locally in `NullifyApp` via `remember { mutableStateOf(Tab.Whitelist) }`.
The root `Scaffold` manages bottom bar insets, while child screens specify
`contentWindowInsets = WindowInsets(0, 0, 0, 0)` so that `TopAppBar` elements
consume status bar insets independently without double padding.

| Feature                 | Android | iOS           |
|-------------------------|---------|---------------|
| Call Screening          | ✅      | ❌ (no API)   |
| Contact sync            | ✅      | ❌ (no API)   |
| Compose UI              | ✅      | ✅            |
| Manual allowlist        | ✅      | ✅            |
| Local database          | ✅      | ✅            |
| Call log                | ✅      | ✅            |
| Bottom navigation       | ✅      | ✅            |
