# Usage

## Allowlist mode (default)

By default, Nullify blocks **all calls from unknown numbers**. Only the
following ring through:

1. Numbers in your device contacts (synced automatically)
2. Ecuadorian bank/utility numbers (pre-populated)
3. Emergency numbers (911, 101, 131, etc.)

## Permissions

On first launch the app requests:

1. **Call Screening role** — required to intercept and block calls
2. **Contacts permission** (`READ_CONTACTS`) — to sync your address book
3. **Notifications permission** (`POST_NOTIFICATIONS`) — Android 13+

Once granted, contacts are synced immediately and the service activates.

## Main screen

### Add manual exception

For numbers not in your contacts (e.g. a delivery service, doctor's office):

1. Enter a name or identifier (e.g. "Dentist")
2. Enter the local phone number (e.g. `0998887777` or `023965006`)
3. Tap "Permitir Llamadas" (Allow Calls)

### Whitelist

Displays all authorized numbers — both synced contacts and manual entries.
Swipe or tap the delete icon to remove an entry.

## Automatic sync

Device contacts are synced immediately after granting `READ_CONTACTS`, then
periodically every 6 hours via WorkManager. Numbers are normalized to the
Ecuadorian local format (strips `593` prefix and leading `0`).

## System whitelist

The following numbers are always allowed out of the box:

| Number       | Name                        |
|--------------|-----------------------------|
| 1700600600   | BGR Contact Center          |
| 023965006    | BGR Canales de Atención     |
| 022509929    | BGR Matriz Quito            |
| 022999999    | Banco Pichincha Canales     |
| 043730100    | Banco Guayaquil Atención    |
| 1700123123   | Produbanco Call Center      |

These are inserted on first database creation and preserved across contact
syncs.
