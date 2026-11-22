# Integration Contracts — Customer Backend ↔ OCPP Platform

Everything in §1–§8 is **[V] verified** by reading `backend/src` (controllers, migrations,
`CommandService`) and the platform's own verified docs (`docs/partner-api.md`,
`docs/13`–`17`). Exact field shapes: the platform's generated OpenAPI,
`GET {base}/v3/api-docs/public-api-v1` — **that, not this file, is the contract**.
§9 lists **[P] proposed** changes that do **not** exist today.

Not verified against a physical charger: the platform states validation against the
supplier's unit is still upcoming. Everything about real charger behaviour is **[A]**.

## 1. Authentication [V]

| Item | Behaviour |
|---|---|
| Credential | `X-API-Key` (SHA-256 hashed server-side). Missing/invalid/deactivated/expired → `401` |
| Provisioning | `POST /api/operator/external-clients` (ADMIN JWT) returns key + HMAC secret **once** |
| Scopes | `READ`, `CHARGING_COMMANDS` (start/stop + command reads), `ID_TAGS_WRITE` |
| Data mode | `PARTNER` (own sessions, full write) vs `GRANTED` (read-only, granted stations). We need `PARTNER` |
| IP allowlist | optional; source IP from `X-Real-IP` only; absent → `403` fail-closed |
| Request signing | optional per client (`require_request_signature`), `docs/16` |
| Rate limit | **60 req/min per client**, in-memory token bucket (`publicapi.rate-limit-per-minute`) — one bucket for *all* our customers |
| Station prerequisite | start requires the charge point to be assigned to our client (`PUT /api/operator/charge-points/{id}/owner`, ADMIN); otherwise `404` |

## 2. Identifiers [V]

* Station = `deviceId` (string, e.g. `CP-SITE-001`; regex `^[A-Za-z0-9][A-Za-z0-9._:-]*$`, 3–64).
  Connector = integer `connectorId` (1-based) **[A]** real firmware numbering unconfirmed (open question #3 in `CLAUDE.md`).
* Session = `transactionId` (int, sequence from 1, **0 is a sentinel, never real**).
* Command = `commandId` (UUID generated per execution).
* `idTag` = our customer's identity on the platform: **global namespace**, ≤20 chars,
  must start with `Z-` on registration, one **ACTIVE session per idTag platform-wide**
  (partial unique index `uk_one_active_session_per_id_tag`, V35).
* `orderId` (optional, ours) is echoed on command results/callbacks but **not persisted
  as a link to `transactionId`** by the platform — we must store the pairing.

## 3. Discovery and availability [V]

| Call | Notes |
|---|---|
| `GET /api/v1/charge-points?lat&lng&radiusKm` | proximity is all-or-nothing; excludes stations at `0.0/0.0`; fields: id, isoCode, vendor, model, online, lastSeenAt, latitude, longitude, connectors |
| `GET /api/v1/charge-points/{id}` | detail |
| `GET /api/v1/charge-points/{id}/connectors` | `deviceId, connectorId, status (OCPP), errorCode, powerType, connectorType, maxPowerKw`; the last three are `null` today (StatusNotification doesn't carry them) |
| `GET /c/{token}` | no auth; any failure = identical bare `404`; success = connector mapping |
| Webhooks `connector.status`, `device.online/offline` | emitted on every `StatusNotification` / connect / disconnect; **broadcast to all destinations** |

Availability = OCPP `status == "Available"` and station `online`. It is a point-in-time
view; it does not account for walk-up RFID users or other apps.

## 4. Charging operations [V]

`POST /api/v1/charge-points/{deviceId}/connectors/{connectorId}/start`
headers `X-API-Key`, `Idempotency-Key`; body `{ "idTag", "orderId?" }`.
Order of checks, all before any OCPP frame: (1) idTag authorization, (2) active tariff
for the station's country, (3) per-idTag in-flight start reservation in Redis (503 if
Redis down), then `RemoteStartTransaction{connectorId, idTag}` — **no charging profile,
no limit parameter, no amount/energy field exists**.

`POST …/stop` body `{ "orderId?" }`: resolves the open session *started by our client*
on that connector; none → `409`, indistinguishable from "not yours".

Result DTO `PublicCommandResultDto`: `status ∈ {PENDING, REJECTED, FAILED}`, `commandId`,
`deviceId`, `connectorId`, `orderId`, `transactionId` (null on start), `failureReason`
(free text, not an enum), `reasonCode`.

* `PENDING`/200 = charger **accepted the instruction**, nothing more.
* `409` + `reasonCode` ∈ `UNKNOWN_IDTAG`, `BLOCKED_IDTAG`, `EXPIRED_IDTAG`,
  `CONCURRENT_IDTAG`, `TARIFF_NOT_CONFIGURED`; `409` with null reasonCode = charger
  refused / offline. `502` comm error, `504` charger reply timeout (**OCPP call timeout
  is 30 s**), `503` Redis unavailable, `404` unknown/not-owned station, `429` +
  `Retry-After` when the per-client rate limit is exceeded.
* `Idempotency-Key` replays the stored response (`Idempotency-Replayed: true`);
  without it a retry sends a second physical command.
* `GET /api/v1/commands/{commandId}`, `GET /api/v1/commands?deviceId&connectorId&page&size`.

Other idTag calls: `POST /api/v1/id-tags` (register), `GET /api/v1/id-tags/{idTag}`
(`Accepted|Blocked|Expired|404`). **There is no endpoint to block, expire or delete an
idTag** from the partner side.

## 5. Sessions, meter values, transactions [V]

* `GET /api/v1/sessions` (filters deviceId, connectorId, idTag, from, to, status; page
  0-based, size ≤100), `GET /api/v1/sessions/{transactionId}`,
  `GET /api/v1/sessions/{transactionId}/metering`. Other clients' sessions → `404`.
* Session fields: transactionId, chargePointId, connectorId, idTag, startedAt,
  stoppedAt, energyKwh, durationSeconds, `costMinor`, `currency`, `currencyDecimals`,
  `pricePerKwhMinor`, `stopReason`, `status ∈ ACTIVE|COMPLETED|ABANDONED|REJECTED`,
  `socStartPercent`, `socEndPercent`.
* **`costMinor` is null while ACTIVE**, and can be null on `COMPLETED` when no tariff
  existed at start (`MISSING_AT_START`). The price is frozen at start, cost computed at
  stop from that snapshot.
* Metering: `totalKwh` (session energy, 3 dp), `currentPowerKw`, `voltage`,
  `currentAmpere`, `stateOfChargePercent`, `durationSeconds`, `sampledAt`. A `null`
  field = never reported, not zero. **Cadence = charger's `MeterValueSampleInterval`
  (typically 30–60 s); at 0 the charger sends nothing.** **[A]** real interval unknown.
* Source of truth for final energy = `StopTransaction` meter reading, not the last
  sample.
* Synthetic closes: `ABANDONED` (+`stopReason` `PowerLoss` | `Other`) with null
  `energyKwh`/`costMinor`; recognize by `status` and `stopReason`, never by zero delta.
* `GET /api/v1/sessions/sync?cursor&updatedSince&limit≤1000` — signed opaque cursor,
  history phase then outbox journal; the canonical **restart-recovery** path.

## 6. Events [V]

Seven types: `device.online`, `device.offline`, `connector.status`, `command.result`,
`session.started`, `session.stopped`, `session.metering`. Envelope
`{eventId, eventType, occurredAt, data}`. Headers `X-Webhook-Signature`
(`HMAC-SHA256(secret, ts + "\n" + rawBody)`), `X-Webhook-Signature-Timestamp`,
`X-Webhook-Event-Id`, `X-Webhook-Event-Type`. At-least-once; **dedupe on `eventId`**;
retries at 1 min, 5 min, 30 min, 2 h, 4 h then `FAILED` (recover via
`GET /api/v1/events`); no ordering guarantee; freshness window ±300 s. Session events
are targeted to the owning client; device-level events are broadcast. Destinations are
registered by an ADMIN, https only.

## 7. Errors and timeouts [V]

RFC 7807 problem bodies on errors; `400` validation, `401`, `404` (never `403` for
foreign resources), `409`, `502/504`, `503`. Reply timeout 30 s per OCPP call; start
reservation TTL = 30 s + margin until accepted, then up to **15 min** (`START_INTENT_TTL`).

## 8. Versioning & compatibility [V]/[P]

Path-versioned `/api/v1`. The platform's rule: adding fields/event types is
backward-compatible; renaming is not. **[P]** Our client must ignore unknown fields,
pin the OpenAPI document hash in CI contract tests, and subscribe to platform changes.

## 9. Gaps and proposed OCPP-platform changes [P] — do NOT exist today

| # | Gap (why it matters) | Proposed change | Blocking? |
|---|---|---|---|
| G1 | **No charging limit exists** (no `chargingProfile`, no `ReserveNow`, no `SetChargingProfile` code) → limits can only be enforced by us sending `stop` when metering says so | Phase 1: enforce in customer backend (see `CHARGING_WORKFLOW.md` §9). Phase 2 (optional): platform adds `SetChargingProfile`/`TxProfile` or a native `maxKwh`/`maxAmount` on start, if the charger supports it **[A]** | No (degraded accuracy) |
| G2 | **No way to cancel an accepted start**: if the driver never plugs in, the idTag stays reserved for up to 15 min (`CONCURRENT_IDTAG` on retry) | Add `POST …/start/{commandId}/cancel` that releases the reservation and intent, or shorten TTL configurable per client | **Yes for UX** |
| G3 | **Tariff not readable by partners** (no tariff endpoint; price visible only after a session starts) → cannot show an estimate | Add `GET /api/v1/charge-points/{id}/tariff` (`pricePerKwhMinor`, `currency`, `currencyDecimals`, `validFrom`) | **Yes** |
| G4 | No reservation concept; walk-up RFID starts bypass our hold | Accept residual risk in v1 (§CHARGING_WORKFLOW §6); optionally `ReserveNow` support later **[A]** firmware support unknown | No |
| G5 | Rate limit 60/min shared by all customers | Raise the limit for our client (today it is a single global property, `publicapi.rate-limit-per-minute`; a per-client override would be a code change); we never proxy per-user polling | **Yes (config)** |
| G6 | No idTag block/delete | `POST /api/v1/id-tags/{idTag}/block`; until then we refuse locally before calling start | No |
| G7 | `orderId` not linked to `transactionId` | Return/persist it on session DTO and events | Nice-to-have (we correlate by idTag, unique ACTIVE per idTag) |
| G8 | Webhook destination & station ownership are manual ADMIN steps | Runbook in `IMPLEMENTATION_PLAN.md`; no code | No |
| G9 | Single-instance OCPP gateway (S6/S27) | Out of our scope; tracked as platform risk | Operational |

## 10. Answers from the OCPP platform team (2026-10-16)

Source: written reply to our 22 questions. Items marked **[supplier]** are not
answered by the platform and need the charger supplier.

| Topic | Answer | Consequence for Zella |
|---|---|---|
| Tariff | One flat per-kWh tariff per country; no time, idle, connection or fixed fees; no per-site or per-station tariff; no dates | Quote = price/kWh x energy only. Tariff read endpoint (G3) was not answered and stays a blocker for pre-start prices |
| Tariff timing | Frozen at `StartTransaction`; later changes never touch a running session; `MISSING_AT_START` leaves `costMinor` null | Debit the wallet from the session snapshot; a null cost must never be guessed |
| Cost authority | `costMinor` is computed by the platform; the charger's "Local Billing" value is ignored **[supplier]**; cost is not proof of payment | Wallet debit uses platform `costMinor` only |
| Plug-in wait | The 15 min is the platform's attribution intent, not a charger timeout **[supplier]** | Keep our own 3 min `START_TIMEOUT` and the auto-stop rule for late starts |
| RemoteStop | No durable queue, no replay after reconnect or restart; a new stop can be issued while the session is ACTIVE | We must retry stop ourselves until the session is confirmed stopped |
| Start timeout | A late OCPP reply is not turned into success; partner ownership is attached after `Accepted`, so a late start may produce a session we do not own | Ask the OCPP team to attribute sessions by `idTag` at `StartTransaction`. Until then, treat a 504 on start as "uncertain" and reconcile |
| Latency | No measured SLA for stop or metering events | Keep polling and sync as the fallback; webhooks are an optimisation |
| MeterValues | Energy, power, voltage, current and SoC supported; real unit sent SoC=0 at begin and first positive SoC 24 to 76 s later; interval change possible via ChangeConfiguration but firmware-dependent and not permitted for our client | Limit accuracy is bounded by 24 to 76 s of unknown consumption |
| Limits | No SetChargingProfile API, no limit at start, no confirmed charger auto-stop **[supplier]** | Limits are enforced by us from metering and are approximate. Wallet balance is the hard cap |
| idTag | Reusable; no delete API; no retention job; `Z-` prefix required | One tag per customer is safe; volume grows slowly |
| Identity | `deviceId` is the permanent id, no rename; connector numbering on the real unit **[supplier]** | Never key data on anything but `deviceId` and `connectorId` |
| Station data | `isoCode` exposed; currency only on sessions; timezone not exposed (possible) | Take currency from the session or tariff, never from the station |
| Sync | No retention job today but a stale cursor returns 410; no guaranteed catch-up window | On 410, do a full resync by `idTag` |
| Presence | `device.offline` only on socket close; silent drops have no fixed detection time | Never trust `online` alone; the start timeout decides |
| Operations | Multi-instance (S6/S27) not scheduled; no maintenance process; no status page; no hosted sandbox, only the simulator | Local simulator for all tests; plan a manual contact for outages |
| Versioning | Breaking changes go to `/v2` with an overlap; no fixed notice; no changelog | Pin the OpenAPI document in our contract tests |
| Credentials | One API key per client, replaced immediately on regeneration; webhook and request-signing secrets are separate | Key rotation needs a coordinated switch window until dual keys exist |

### Follow-ups to send back

1. Tariff read endpoint (G3), still unanswered.
2. Attribute sessions to the client by `idTag` at `StartTransaction`, not after `Accepted`.
3. Dual active API keys for rotation.
4. Expose station currency and timezone.
5. Supplier questions: plug-in timeout, Local Billing, MeterValues interval, SetChargingProfile, auto-stop at kWh, connector numbering.

## 11. Partner API: NDZela reads from this backend [V]

Implemented 2026-11-05. Base path `/v1/partners/ndzela`, HTTPS only, header `X-Api-Key`, JSON. The key is bound to one
country; the tenant is that country. Errors use RFC 7807 with these `code` values: `PARTNER_KEY_INVALID` (401),
`TOO_MANY_REQUESTS` (429, `Retry-After`), `PARTNER_BATCH_EMPTY`, `PARTNER_BATCH_TOO_LARGE`, `PARTNER_CONTACT_INVALID`,
`PARTNER_CURSOR_INVALID`, `PARTNER_LIMIT_INVALID` (400), `NOT_FOUND` (404).

| Method and path | Purpose |
|---|---|
| `POST /id-tags/lookup` | `{drivers:[{ref,email,phoneE164}]}`, 1 to 200. Email lowercase, phone E.164. Returns `{results:[{ref,matches:[{customerId,idTag,matchedOn,quality,firstName,lastName}]}]}`. `quality` is `FULL` (email and phone match one customer; names returned) or `PARTIAL` (one field matches; no names). A customer who signed in with Google or Apple and has no phone yet is also `FULL` on a matching email alone (`matchedOn: ["email"]`), because the provider proved the email. That customer stays `FULL` on email even when a different customer matches the phone; the phone customer is then returned as `PARTIAL` next to it. Only `ACTIVE` customers of the key's country. `idTag` is null until the customer's first charge; lookup never creates one. Read-only, links nothing. |
| `PUT /links/{customerId}` | Confirm a driver. Idempotent, 204. 404 for an unknown, inactive or other-country customer. |
| `DELETE /links/{customerId}` | Remove the link. Idempotent, 204. |
| `GET /sessions?cursor=&limit=&customerId=` | Payment feed of linked customers' sessions that reached the charger. `limit` 1 to 500, default 100. No cursor reads from the beginning. Returns `{items,nextCursor,hasMore}`; `nextCursor` is null only for an empty first call. Changes newer than `ZELLA_PARTNER_FEED_LAG` (default 10 seconds) are not served yet, so a change that is still committing is never skipped. `customerId` restricts the feed to one linked customer (404 if not linked) with its own cursor: read it once after linking a driver, because the global cursor has already moved past that driver's history. |

| `GET /public-sessions?cursor=&limit=` | The same feed for the sessions of customers who are **not** linked to NDZela (2026-11-06). Same paging, key and lag. An item is the feed item **without** `customerId`, `idTag` and `paymentMethod`: what was charged, never who. A customer who gets linked leaves this feed. Empty when everyone is linked. |

Feed item: `transactionId, customerId, idTag, stationId, connectorId, sessionState, startedAt, stoppedAt, energyKwh,
energyCostMinor, vatMinor, serviceFeeMinor, totalChargedMinor, currency, currencyDecimals, paymentMethod ("WALLET"),
paymentState, breakdownEstimated, updatedAt`. `paymentState` is `PENDING`, `CHARGED`, `NOT_CHARGED`, `UNDER_REVIEW` or
`REFUNDED` (reserved: no refund flow exists yet). A change of state re-emits the same `transactionId` with a newer
`updatedAt`; order is `(updatedAt, sessionId)`; `totalChargedMinor` is what the wallet actually captured.
`breakdownEstimated` is true for charges settled before `session_charge` existed. When a charge was capped at the
authorized budget, the breakdown parts are the full uncapped amounts while `totalChargedMinor` is what was captured, so
they do not add up.

Admin key management (super admin, `ADMINS_MANAGE`): `POST /v1/admin/partners/NDZELA/keys` with `{"country":"GA"}`
returns the key once; `GET` lists without key material; `DELETE /{keyId}` revokes at once.

### 11b. Zella calls NDZela: free Sunday eligibility

`GET {ZELLA_NDZELA_BASE_URL}/api/v1/partners/zella/customers/{customerId}/free-charging`, header `X-Api-Key`, answer
`{eligible, reason, agreementId, sundayDate (yyyy-MM-dd), windowEndsAt (ISO instant), window {day, from, until, endOfDay}}`;
the three middle fields are null when not eligible, `window` is present whenever the driver is known. Reasons from NDZela: `ELIGIBLE, NO_AGREEMENT, AGREEMENT_SUSPENDED, AGREEMENT_NOT_STARTED, AGREEMENT_ENDED,
OUTSIDE_WINDOW, UNPAID_DAYS, DRIVER_SUSPENDED, NOT_LINKED`. Local reasons added by Zella: `UNAVAILABLE` (no usable
answer, fail closed) and `SUNDAY_USED` (already claimed by this customer or by another customer on the same
`agreementId`). NDZela limits this read to 30 per minute per customer under a 2000 per minute ceiling. See `NDZELA_INTEGRATION.md` section 11 and NDZela ADR-0048.

### 11c. Customer app: free Sunday offer, and the feed category

`GET /v1/free-sunday` (customer JWT, 401 without) answers `{available, reason, endsAt, window}` (`window` is `{day, from, until, endOfDay}` from NDZela, absent for a customer outside the program; the app builds its texts from it): `endsAt` is an ISO-8601 instant
or null, `reason` is one of the stable codes of 11b (untranslated, the app localizes). It is read-only (it never claims)
and never fails when NDZela is down: it answers 200 with `available=false`, `reason=UNAVAILABLE`.

Only customers linked as NDZela drivers are ever checked with NDZela: an unlinked customer gets `NOT_LINKED` locally,
with no HTTP call and no customer id sent to NDZela (also for the claim at session start). The route is rate limited per
customer (`zella.rate-limit.free-sunday-per-customer-per-minute`, 20), 429 `TOO_MANY_REQUESTS` with `Retry-After`. A
failure reading the claims answers `UNAVAILABLE` instead of an error. NDZela's answer is cached per customer for 30
seconds (5 seconds when `UNAVAILABLE`, never past `windowEndsAt`) for this route only; the claim at start is never
cached and still fails closed to a paid charge when NDZela is unreachable or over its limit.

In the partner feed and the admin session views `freeCategory` is `NDZELA_STAFF` or `OTHER` for complimentary
customers, `FREE_SUNDAY` when the session's hold is waived and its `free_sunday_claim` is `CLAIMED`, and null otherwise
(a released claim never shows `FREE_SUNDAY`). Free sessions keep a zero charge.

### 11d. Admin free-charging routes (Zella dashboard to Zella backend to NDZela)

`GET /v1/admin/free-charging/drivers?q=&size=` (`FREE_CHARGING_MANAGE`, country scope; `q` is cut at 100 characters, `size` bounded 1 to 50, both passed to NDZela URL-encoded) forwards the driver search of NDZela (name, e-mail, phone, fleet number or plate of the car held) and answers `[{driverId, customerId, fullName, phone, email, fleetNumber, plate, status, linked, inProgram}]`; the Gratuité page uses it to add several drivers at once (one `POST /agreements` with the `customerId` per driver), so it no longer needs the Customers permission. Agreement rows also carry `phone`, `email`, `fleetNumber` and `plate`.

The free window (day, first moment, end) is a country setting kept by NDZela (NDZela ADR-0049):
`GET /v1/admin/free-charging/window` (`FREE_CHARGING_READ`) and `PUT /v1/admin/free-charging/window {day, from, until?, endOfDay}`
(`FREE_CHARGING_MANAGE`, country scope like every route here; each change is audited `FREE_WINDOW_CHANGED`). NDZela refuses an invalid
window with `422 FREE_WINDOW_INVALID`, passed through.


`/v1/admin/free-charging`: `GET /agreements` and `GET /usage?page&size` (permission `FREE_CHARGING_READ`), `POST /agreements`,
`PUT /agreements/{id}`, `POST /agreements/{id}/suspend|resume|revoke` (permission `FREE_CHARGING_MANAGE`). `POST /agreements`
takes `{customerId (the Zella customer, uuid), startsOn, endsOn?, conditions?}`; the customer must be linked to NDZela in the default
country, Gabon (else 422 `FREE_AGREEMENT_NOT_LINKED`, NDZela not called; an admin without a grant on that country gets 403 `COUNTRY_FORBIDDEN`) and NDZela resolves the driver from `customerId`. Each is forwarded to
NDZela `/api/v1/partners/zella/free-charging/...` with `X-Api-Key` and `X-Actor`. Agreement
`{id, driverId, driverName, startsOn, endsOn, status, conditions, eligibility, unpaidDays, lastFreeUse}`; usage page
`{items:[{sessionId, driverId, driverName, startedAt, energyKwh, stopReason}], page, size, total}`. Error `code`s:
NDZela's own (`FREE_AGREEMENT_EXISTS`, `FREE_AGREEMENT_NOT_LINKED`, `FREE_AGREEMENT_DATES_INVALID`,
`FREE_AGREEMENT_REVOKED`, `VALIDATION_ERROR`, `RESOURCE_NOT_FOUND`) with NDZela's 4xx status, `FREE_CHARGING_UNAVAILABLE` (503),
`PERMISSION_DENIED` (403), `COUNTRY_FORBIDDEN` (403), `INVALID_PAGING` (400). See `NDZELA_INTEGRATION.md` section 12.
The dashboard picker lists the customers with the existing `GET /v1/admin/customers?country&q&ndzelaDriver=true&size&sort=name,asc`
(`CUSTOMERS_READ`): `ndzelaDriver=true` keeps only the customers linked to NDZela, filtered in the database so the total and the
pages count only them (default `false`, no filter).

## 12. OCPP platform webhooks (incoming)

`POST /v1/ocpp/webhooks` on the customer backend, public route, protected by the signature only.

- Headers: `X-Webhook-Signature` (lowercase hex HMAC-SHA256). With V2 also `X-Webhook-Signature-Timestamp`; the signed text is
  `timestamp + "\n" + rawBody`, and the timestamp must be within `zella.ocpp-webhook.tolerance` (300 s). Without the
  timestamp header the V1 form applies: the HMAC of the raw body. Wrong or missing signature: `401 WEBHOOK_SIGNATURE_INVALID`.
- The secret is `ZELLA_OCPP_WEBHOOK_SECRET`, falling back to `OCPP_WEBHOOK_SECRET`. Empty secret refuses every event.
- Success is `202`; the work is done in the background. A signed body that is not an event is `400 MALFORMED_REQUEST`.
- Handled: `session.metering` (stores energy, power, voltage and battery of the live session found by station, connector and
  idTag), `session.started` and `session.stopped` (refresh that session from the platform). Other types are acknowledged
  and ignored. Repeated `eventId`s are dropped (last 5000 remembered in memory; every handler is idempotent anyway).
- Polling stays the source of truth. When the platform has no readings to poll, the live view and the budget guard use the
  latest webhook reading if it is under 120 s old.
- The OCPP team registers `https://apicharging.up.railway.app/v1/ocpp/webhooks` as the Zella destination.
