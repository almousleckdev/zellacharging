# Push notifications

## Design
Transactional outbox. A business transaction calls `Notifier.notify(...)`, which inserts a row into `notification_outbox`
inside that same transaction. A separate `NotificationDispatcher` (every 10 s) claims due rows, sends them through FCM
HTTP v1 and records the result. Nothing is sent from inside a financial transaction; a crash between commit and send
loses nothing because the row is already stored.

| Part | Behaviour |
|---|---|
| Dedupe | unique `(customer_id, dedupe_key)`; e.g. `topup:<id>:done` so a retried settlement queues one push |
| Retry | exponential backoff from 30 s, six attempts, then `DEAD` with an `ALERT` log |
| Devices | many per customer in `device_token`; a token is owned by one customer; `UNREGISTERED`/invalid answers delete the token |
| Language | stored per device; texts are rendered at send time (French default, English) |
| Observability | `state`, `attempts`, `delivered_devices`, `sent_at`; finished rows purged after 30 days |
| Payloads | title, short body, and `screen`/`id` for navigation. Never amounts, balances or tokens |
| Inactive mode | without `ZELLA_FCM_CREDENTIALS_JSON` the dispatcher does nothing and rows wait in the outbox |

## Events
Top-up started, succeeded, failed. Withdrawal started, succeeded, failed. Charge started, completed, failed,
interrupted. Password changed. Not wired: account registered (no device exists yet), a separate "charge stopped" push
(a stop ends in "completed"), balance-updated pushes, per-category preferences.

## API
`PUT /v1/devices` registers or moves a token (`token`, `platform`, `language`). `DELETE /v1/devices` removes a token
owned by the caller. Both need a customer token.

## Mobile
`PushListener` (inside the signed-in shell) asks permission, registers the FCM token, re-registers on refresh, shows a
banner for foreground messages, and routes a tapped notification (`wallet`, `charging`, `settings`) whether the app was
in the background or terminated. Sign-out unregisters the token. Android creates the `zella_alerts` channel at startup and
declares `POST_NOTIFICATIONS`; iOS has the push entitlement and the `remote-notification` background mode.
Notifications are `notification` messages, so Android and iOS display them even when the app is closed; no background
isolate is needed.

## What still needs a human
1. Create a Firebase service account with the Cloud Messaging role, put its JSON in `ZELLA_FCM_CREDENTIALS_JSON`.
2. Upload the APNs authentication key (.p8) in Firebase console, Cloud Messaging, for iOS.
3. Test on a physical Android device and a physical iPhone (simulators do not receive APNs).
4. `firebase_messaging` 17 deprecates `getToken` in favour of an installation-id registration. The app still uses
   `getToken`, which works with the FCM v1 `token` field. Revisit when the deprecated calls are removed.

## Tests
Backend: `NotificationIntegrationTest` (delivery in each device language, dedupe, invalid token cleanup, retry then dead
letter, no devices, ownership). Mobile: `push_notifications_test.dart` (routing, permission, refresh, failure, sign-out).
**Real delivery to a phone has not been verified.**

## Notification center and icon badge

- Every event stays in `notification_outbox`, so it doubles as the customer's inbox (`read_at` marks it read).
  The inbox does not depend on push delivery: it lists the event even when no device is registered.
- API (customer token): `GET /v1/notifications` (localized from `Accept-Language`, newest first, with `unread`),
  `GET /v1/notifications/unread-count`, `POST /v1/notifications/{id}/read`, `POST /v1/notifications/read`.
  Only the owner can mark a notification read.
- Each push carries the unread count: `aps.badge` on iOS and `notification_count` on Android.
  iOS uses `apns-push-type: alert` with priority 10 and a sound, so it shows on a locked phone.
  Android uses the high-importance `zella_alerts` channel with `visibility: PUBLIC`, so the content shows on the lock screen.
- The app keeps the icon badge equal to the unread count (`app_badge_plus`), refreshing on foreground pushes, every 15 seconds
  while open and on resume. Signing out resets it to zero.
- Not verified on hardware: lock-screen display and launcher badges need a real iOS and Android device with the Firebase
  service account and the APNs key. Android launchers differ: Pixel and Samsung show counts, some launchers only show a dot.
