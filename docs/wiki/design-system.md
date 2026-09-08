# Design System & Material 3

Nullify combines Google's **Material 3 (Material You)** specification with a lightweight **Glassmorphism** visual language. This document details the color architecture, typography scale, dark/light theme strategy, and placeholder components.

---

## 1. Design Principles

1. **Security & Transparency**: Frosted glass surfaces over ambient gradients communicate call screening, inspection, and perimeter protection.
2. **Accessible Contrast**: Meets WCAG AA standards (≥4.5:1 for normal text) across both daylight and dark environments via calibrated card alphas and tonal containers.
3. **Pure Material 3**: Strict adherence to official `androidx.compose.material3` APIs, discarding legacy Material 2 dependencies.
4. **Edge-to-Edge Native**: Full-bleed rendering behind system bars with dynamic icon contrast synchronization on Android 15+.

---

## 2. Color System & Roles

The color system is built on an Ecuadorian forest-green primary palette, complimented by slate secondaries, teal/mint tertiaries, and modern Material 3 surface container roles.

### Primary, Secondary & Tertiary Roles

| Role | Light Value | Dark Value | Purpose |
|---|---|---|---|
| **Primary** | `#1B5E20` (Forest Green) | `#A5D6A7` (Mint) | High-emphasis branding, active indicators |
| **OnPrimary** | `#FFFFFF` | `#0D3310` | Text/icons on primary surfaces |
| **PrimaryContainer** | `#A5D6A7` | `#1B5E20` | Tonal buttons, avatar backgrounds |
| **OnPrimaryContainer** | `#0D3310` | `#FFFFFF` | Text on primary containers |
| **Secondary** | `#37474F` (Slate) | `#CFD8DC` | Supporting actions, search icons |
| **SecondaryContainer** | `#CFD8DC` | `#37474F` | Tonal chips, secondary containers |
| **Tertiary** | `#00695C` (Teal) | `#80CBC4` | Contrasting accents, status indicators |
| **TertiaryContainer** | `#80CBC4` | `#00695C` | Tertiary badges and highlights |
| **Error** | `#B71C1C` (Crimson) | `#FFCDD2` | Blocked call badges, delete icons, error states |

### Material 3 Surface Containers (v1.2 & v1.3)

In accordance with Material 3 v1.2+ recommendations, hardcoded elevation layers are replaced by tonal surface roles:

| Role | Light Token | Dark Token | Purpose |
|---|---|---|---|
| **surfaceDim** | `#DADADA` | `#111411` | Dimmed background behind dialogs/scrim |
| **surfaceBright** | `#FBFBFB` | `#373A37` | Highest-luminance surface in theme |
| **surfaceContainerLowest** | `#FFFFFF` | `#0C0F0C` | Deepest surface tier |
| **surfaceContainerLow** | `#F4F6F4` | `#191C19` | Cards floating above ambient gradient |
| **surfaceContainer** | `#EEF1EE` | `#1D201D` | Standard surface container for components |
| **surfaceContainerHigh** | `#E8ECE8` | `#282B28` | Elevated modal sheets and menus |
| **surfaceContainerHighest** | `#E2E7E2` | `#333633` | Input fields, active card states |
| **outlineVariant** | `#C4C7C5` | `#424942` | Subtle dividers and decorative borders |
| **scrim** | `#000000` | `#000000` | Backdrop dimming |

### Glassmorphism Ambient Tokens

| Token | Light | Dark | Application |
|---|---|---|---|
| **GlassGradientStart** | `#E8F5E9` | `#1B2E1B` | App background vertical gradient (top) |
| **GlassGradientMid** | `#E0F2F1` | `#1A2E2D` | App background vertical gradient (center) |
| **GlassGradientEnd** | `#E3F2FD` | `#0D2338` | App background vertical gradient (bottom) |
| **GlassBorder** | `30% #FFFFFF` | `10% #FFFFFF` | 0.5dp translucent card stroke |
| **GlassCard Alpha** | `82%` | `70%` | Card background translucency |

---

## 3. Dark & Light Theme Strategy

### Theme Mode Independence
Theme state is managed through the `ThemeMode` enum:

```kotlin
enum class ThemeMode { System, Light, Dark }
```

- **Cycling**: Tapping the top app bar icon cycles `Auto (System) → Claro (Light) → Oscuro (Dark) → Auto`.
- **`LocalThemeIsDark` CompositionLocal**: Exposes the resolved boolean (`darkTheme`) to the entire composable subtree, ensuring components like `GlassCard` and placeholders update immediately regardless of OS system state.
- **Dynamic Color**: On Android 12+ (API 31+), `NullifyTheme` queries `dynamicLightColorScheme` / `dynamicDarkColorScheme` if enabled.

### Edge-to-Edge System Bar Synchronization

Nullify utilizes `enableEdgeToEdge()` in `MainActivity`. System bar icons are dynamically coordinated with `NullifyTheme` via `PlatformSystemBarsEffect`:

```kotlin
@Composable
actual fun PlatformSystemBarsEffect(darkTheme: Boolean) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            insetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
```

This guarantees status bar and navigation bar icons remain crisp and legible in both Light and Dark modes.

---

## 4. Contact Placeholders & Skeleton System

All contact loading and empty states use specialized components located in `com.nullify.ui.components.ContactPlaceholders.kt`.

### 1. Shimmer Loading (`rememberShimmerBrush`)
- **Animation**: 1300ms infinite sweep (`Brush.linearGradient`) using `FastOutSlowInEasing`.
- **Light Theme**: Low-contrast surface tint (`alpha = 0.04f..0.12f`) over light glass.
- **Dark Theme**: Soft white gradient (`alpha = 0.04f..0.15f`) avoiding high-luminance flashes on OLED displays.

### 2. Contact List Skeleton (`ContactListSkeleton`)
Rendered when `whitelistState is UiState.Loading`:
- Simulates 4–5 contact items inside `GlassCard` containers.
- Circular placeholder (42dp) for avatar.
- Two rounded bars (width 55% / height 16dp and width 38% / height 12dp) for name and number.
- Action icon placeholder (24dp) for the delete action button.

### 3. Contact Avatar (`ContactAvatarPlaceholder`)
Embedded in each `SwipeableContactCard`:
- **Initials Algorithm**: Extracts up to 2 uppercase initials from multi-word names (e.g., `"Banco Pichincha"` → `"BP"`, `"Mamá"` → `"M"`).
- **Fallback**: Displays `Icons.Default.Person` if the name contains no letter characters.
- **Light Theme**: Container in `primaryContainer` (85% alpha) with `onPrimaryContainer` typography.
- **Dark Theme**: Container in `primary` (20% alpha) with luminous `primary` typography and subtle border.

### 4. Empty State (`ContactEmptyStatePlaceholder`)
Rendered when `whitelistState is UiState.Success` with an empty list:
- **Empty Whitelist Mode**: Displays `Icons.Default.Shield`, reassuring the user that contacts are synced automatically and manual entries can be added above.
- **Empty Search Mode**: Displays `Icons.Default.PersonSearch`, the searched term in quotes, and an interactive **"Limpiar búsqueda"** button to reset the filter.

---

## 5. Typography Scale

Configured in `NullifyTypography` (`Type.kt`), implementing the official Material 3 typography scale:

| Token | Size | Line Height | Weight | Usage |
|---|---|---|---|---|
| `headlineLarge` | 28sp | 36sp | Bold | Prominent headings |
| `headlineMedium` | 24sp | 32sp | SemiBold | Section headings |
| `headlineSmall` | 20sp | 28sp | SemiBold | Sub-section headings, dialog titles |
| `titleLarge` | 20sp | 28sp | SemiBold | Screen top bar titles |
| `titleMedium` | 16sp | 24sp | Medium | Card headers, empty state titles |
| `titleSmall` | 14sp | 20sp | Medium | Form field headers, avatar text |
| `bodyLarge` | 16sp | 24sp | Normal | Contact display names, log phone numbers |
| `bodyMedium` | 14sp | 20sp | Normal | Empty state descriptions, contact numbers |
| `bodySmall` | 12sp | 16sp | Normal | Error validation messages |
| `labelLarge` | 14sp | 20sp | Medium | Buttons, tab navigation labels |
| `labelMedium` | 12sp | 16sp | Medium | Chips, auxiliary badges |
| `labelSmall` | 11sp | 16sp | Medium | Call log timestamps and status tags |

---

## 6. WindowInsets & Nested Scaffold Architecture

To prevent double-padding when combining the bottom `NavigationBar` with per-screen `TopAppBar` elements:

- **Root Scaffold ([App.kt](file:///home/iferpaz7/src/personal/nullify/composeApp/src/commonMain/kotlin/com/nullify/App.kt))**: Hosts the bottom navigation bar and passes its insets via `paddingValues`.
- **Child Scaffolds ([WhitelistScreen.kt](file:///home/iferpaz7/src/personal/nullify/composeApp/src/commonMain/kotlin/com/nullify/ui/WhitelistScreen.kt), [CallLogScreen.kt](file:///home/iferpaz7/src/personal/nullify/composeApp/src/commonMain/kotlin/com/nullify/ui/CallLogScreen.kt))**: Set `contentWindowInsets = WindowInsets(0, 0, 0, 0)`. The `TopAppBar` consumes the top status bar insets independently, guaranteeing that content area calculations are pixel-accurate.
