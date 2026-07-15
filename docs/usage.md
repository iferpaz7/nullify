# Usage

## Permissions

On first launch, the app requests:

1. **Call Screening role** — required to intercept calls
2. **Contacts permission** — to sync the address book

Once granted, the app runs in the background without user intervention.

## Main screen

### Add manual exception

1. Enter a name or identifier (e.g. "BGR", "Banco Pichincha")
2. Enter the local phone number (e.g. `0998887777` or `023965006`)
3. Tap "Permitir Llamadas" (Allow Calls)

### Whitelist

Displays all authorized numbers. Swipe or tap the delete icon to remove an entry.

## Automatic sync

Device contacts are synced automatically every 6 hours via WorkManager. Numbers
are normalized to the Ecuadorian local format (strips `593` prefix and leading
`0`).
