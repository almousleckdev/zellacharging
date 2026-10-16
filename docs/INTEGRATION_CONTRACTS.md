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

| Topic | Answer | Consequence for Ndzela |
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
