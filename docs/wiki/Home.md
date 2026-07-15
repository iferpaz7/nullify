# Nullify

> Call firewall for Ecuador — block spam, telemarketing, and unwanted calls on Android.

**Repository:** [iferpaz7/nullify](https://github.com/iferpaz7/nullify)

---

## Features

| Feature | Description |
|---|---|
| 📋 **Blocklist** | Numbers you want to block — manual or from call log |
| 📖 **Allowlist** | Numbers that always ring through |
| 📊 **Call log** | In-app history of all screened calls |
| 🤖 **Auto-block prefixes** | Block by prefix (e.g. `1800-` telemarketing) |
| 🔒 **Private numbers** | Block unknown/private/hidden callers |
| 📱 **Contact sync** | Select contacts to never block |

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
| Android 11+ | ✅ Supported (CallScreeningService) |
| iOS | 🚧 In development (Call Directory extension) |
