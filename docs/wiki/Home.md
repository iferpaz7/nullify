# Nullify

> Call firewall for Ecuador — block spam, telemarketing, and unwanted calls on Android.

**Repository:** [iferpaz7/nullify](https://github.com/iferpaz7/nullify)

---

## Features

| Feature | Description |
|---|---|
| **Allowlist mode** | Only contacts and emergency numbers ring through |
| **Private number blocking** | Unknown/private/hidden callers are blocked |
| **Contact sync** | Device contacts synced automatically to allowlist |
| **System whitelist** | Ecuadorian bank/utility numbers always allowed |
| **Manual exceptions** | Add numbers not in your contacts to the allowlist |

---

## Documentation

| Guide | Description |
|---|---|
| [Architecture](architecture) | Project structure, tech stack, module dependency map |
| [Setup](setup) | Local development environment setup |
| [Usage](usage) | Daily use, permissions, call screening |
| [Building](building) | Build & deploy for Android and iOS |
| [Roadmap](roadmap) | Upcoming features and milestones |

---

## Quick start

```bash
git clone git@github.com:iferpaz7/nullify.git
cd nullify
./gradlew :androidApp:assembleDebug
```

Requires Android SDK 36+ and JDK 21.

---

## Platform support

| Platform | Status |
|---|---|
| Android 10+ | ✅ Supported (CallScreeningService) |
| iOS | 🚧 In development (Call Directory extension) |
