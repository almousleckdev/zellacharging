# Charging Workflow

Tags: **[V]** verified in the OCPP platform · **[P]** proposed · **[A]** assumption · **[U]** unresolved.
State names in `CODE_FONT` are defined in `STATE_MACHINES.md`. Platform calls and
their limits: `INTEGRATION_CONTRACTS.md` (IC).

## 0. Ground rules

1. The **customer backend (CB)** is authoritative for hold, payment, limit; the
   **OCPP platform** is authoritative for what the charger actually did. The app is
   authoritative for nothing. **[P]**
2. `PENDING` from `/start` or `/stop` means "charger accepted the instruction" only
   (IC §4). Charging started ⇔ `session.started` (or `GET /sessions` shows `ACTIVE`).
   Charging stopped ⇔ `session.stopped` (or session `COMPLETED|ABANDONED`). **[V]**
3. Chargers are not assumed identical. Behaviour that varies is a **per-model
   capability record** in CB config (`meteringIntervalSeconds`, `reportsSoC`,
   `plugInWaitSeconds`, `limitEnforcementVerified`), default-conservative, filled in
   only after a field test per model. The only model seen so far: GB/T 60 kW,
   `JC.DW V1.1`, whose OCPP layer is probably a translation over a YKC 1.5 controller
   (`CLAUDE.md`) — **no OCPP field is trusted until seen on the wire**. **[A]**

## 1. Registration & authentication [P]

Sign-up → verify phone/email → JWT + refresh. On first need (or at sign-up) CB
allocates `ocppIdTag` = `Z-` + 12 random base32 chars (14 chars, under the 20-char [V] limit),
registers via `POST /api/v1/id-tags`; on timeout, `GET /api/v1/id-tags/{idTag}` before
retrying; `409` for a tag we generated ⇒ regenerate. The idTag is stable per customer,
which makes the platform's "one ACTIVE session per idTag" rule [V] equal to "one live
session per customer" — a desired product rule, enforced twice (CB and platform).

## 2. Discovery & availability

App → CB `GET /v1/stations?lat&lng&radiusKm` → CB `catalog` (cached ≤30 s) calls
`GET /api/v1/charge-points` [V]. A connector is *offerable* iff station `online`,
connector status `Available`, active tariff known, station assigned to our client,
GB/T type, not under a live hold in CB. Walk-up users are invisible to the hold
system (§6).

## 3. QR scan & validation

App sends the scanned URL to CB `POST /v1/qr/resolve`; CB extracts the token and calls
`GET /c/{token}` [V]. Bare `404` ⇒ "invalid or expired QR" (reasons are deliberately
indistinguishable [V]). Success ⇒ `(deviceId, connectorId)`; then §2 offerability
checks. Flutter never contacts the OCPP platform.

## 4. Limit selection & pricing

Limit types **[P]**: `ENERGY_KWH` (target energy), `MAX_AMOUNT` (minor units),
`FULL` (until vehicle stops/driver stops; authorized up to a per-country maximum
`maxUnlimitedAmountMinor`). Price source: per-kWh price from the tariff read (gap G3,
**must exist**), `currency`, `currencyDecimals`.

```
capKwh      = ENERGY_KWH ? target : floor3(amountMinor / pricePerKwhMinor)
estimateMinor = round_HALF_UP(capKwh × pricePerKwhMinor)          # BigDecimal, same rule as the platform [V: HALF_UP, scale 0]
```
The quote (`tariff_quote`: price, currency, decimals, capKwh, expiresAt = +5 min) is
persisted and referenced by the hold. If the price frozen by the platform at
`StartTransaction` differs from the quote, CB recomputes `capKwh = authorizedMinor /
actualPrice` — the customer never pays more than the authorized amount **[P]**.

## 5. Sequence (nominal)

```mermaid
sequenceDiagram
  participant A as Flutter
  participant C as Customer Backend
  participant PSP as Payment Provider
  participant O as OCPP Platform
  participant K as Charger
  A->>C: POST /v1/holds {deviceId, connectorId, limit, quoteId} (Idempotency-Key)
  C->>C: INSERT reservation HELD (unique active per connector & per customer)
  C-->>A: 201 hold, expiresAt
  A->>C: POST /v1/sessions {holdId} (Idempotency-Key)
  C->>PSP: authorize(amount = cap + margin)
  PSP-->>C: AUTHORIZED (sync or webhook)
  C->>C: TX: session REQUESTED, hold CONSUMED, outbox(start)
  C->>O: POST …/start {idTag, orderId=sessionId} Idempotency-Key=start:{sessionId}:1
  O->>K: RemoteStartTransaction{connectorId,idTag}
  K-->>O: Accepted
  O-->>C: 200 PENDING {commandId}
  C->>C: session START_ACCEPTED (awaiting plug-in)
  K->>O: StartTransaction (driver plugs in)
  O-->>C: webhook session.started {transactionId, startedAt, meter…}
  C->>C: session CHARGING; freeze price/transactionId
  loop each MeterValues
    K->>O: MeterValues
    O-->>C: webhook session.metering
    C-->>A: SSE session.update
    C->>C: limit watcher
  end
  C->>O: POST …/stop (at limit or user)  Idempotency-Key=stop:{sessionId}:1
  O->>K: RemoteStopTransaction
  K->>O: StopTransaction
  O-->>C: webhook session.stopped {energyKwh, costMinor, stopReason}
  C->>PSP: capture(min(costMinor, authorized)) ; release remainder
  C->>C: receipt, history; SSE final
```

### Step detail

| # | Step | Rule |
|---|---|---|
| 5.1 | **Hold** | One DB transaction inserts `reservation(HELD)`. Partial unique indexes: one live hold per `(deviceId, connectorId)` and per `customerId` (`DATA_MODEL.md`). Holds before payment so we never authorize money for a contested connector. TTL 5 min (config). Re-check connector status from OCPP **after** insert; not `Available` ⇒ release, 409. |
| 5.2 | **Payment authorization** | Amount = `estimateMinor` + overshoot margin (§9). Provider capabilities **[A]**: if no pre-auth/partial capture, fallback is *prepaid wallet / charge-then-refund* **[U]**. |
| 5.3 | **Session creation** | Single local transaction: session `REQUESTED`, hold → `CONSUMED`, `session_command(START)` + outbox row. No network call inside the transaction. |
| 5.4 | **Remote start** | Outbox worker calls OCPP. **Idempotency-Key per attempt** `start:{sessionId}:{n}`. Verified catch **[V]**: the platform stores *every returned result* (including `FAILED` 504 and `REJECTED`) and replays it for the same key without resending; it releases a key only if the call threw. So a *new attempt* needs a *new key* — and a new attempt is allowed only after §5.4a. |
| 5.4a | Ambiguous start (timeout/5xx/network) | Do **not** blindly resend. Query `GET /api/v1/commands?deviceId&connectorId` and `GET /api/v1/sessions?idTag=…&status=ACTIVE`. Charger may have accepted after our 30 s timeout. If a session exists ⇒ adopt it; if command `PENDING` ⇒ adopt; if nothing and a grace period elapsed ⇒ next attempt (max 2). Safety net **[V]**: a second start on the same idTag is refused `CONCURRENT_IDTAG`, same connector by `uk_one_open_session_per_connector`. |
| 5.5 | **Start confirmed** | Only by `session.started` / session `ACTIVE`. Copy `transactionId`, `startedAt`, `pricePerKwhMinor`, `currency`, `currencyDecimals`. No event within `plugInWaitSeconds` + margin (default 120 s) ⇒ `START_TIMEOUT` (§8.2). |
| 5.6 | **Live monitoring** | Metering events stored (§10); pushed over SSE. Cost shown = `round(totalKwh × pricePerKwhMinor)`, labelled "estimate"; final `costMinor` only from `session.stopped`. |
| 5.7 | **Limit stop** | §9. |
| 5.8 | **Stop confirmed** | Only by `session.stopped` / session `COMPLETED|ABANDONED`. No confirmation within 60 s of an accepted stop ⇒ keep `STOPPING`, escalate (§8.5). |
| 5.9 | **Final readings** | From the stopped session (`energyKwh`, `costMinor`, `socEndPercent`, `stopReason`); the `StopTransaction` register is the truth, not the last sample [V]. |
| 5.10 | **Settlement** | Capture `min(costMinor, authorized)`; release/refund remainder. Rules in `STATE_MACHINES.md` §5–6. |
| 5.11 | **Receipt / history** | Receipt rendered from frozen session + payment rows; immutable once issued. |

## 6. Connector exclusivity — what is and is not guaranteed

* **Between app customers**: guaranteed by DB constraints in CB (hold + session unique
  indexes) **[P]**, backed by the platform's `uk_one_open_session_per_connector` and
  per-idTag in-flight start reservation **[V]** (cannot be relied on alone: it keys by
  idTag, i.e. different customers do not collide there).
* **Against walk-up/RFID or other partners**: the platform has no reservation concept
  and no `ReserveNow` (G4). Mitigation: connector status is re-read right before start
  and the OCPP-side connector must be `Available`/`Preparing` for our start to be
  accepted by the charger; if someone else starts first, our `RemoteStart` is rejected
  or `session.started` carries a different `idTag`. Result: hold `FAILED_CONFLICT`,
  payment voided. This residual race is **documented, not eliminated**. **[A]**

## 7. Pricing/limit vocabulary shown to the customer

"Up to X kWh" / "up to Y XOF" — never "exactly". See §9 for why.

## 8. Alternative sequences and failures

| # | Scenario | Behaviour |
|---|---|---|
| 8.1 | Idempotent client retry of hold/session creation | Same `Idempotency-Key` returns the stored response; different body ⇒ 422 |
| 8.2 | **Payment OK, charging never starts** (driver doesn't plug in, charger rejects, offline) | `START_TIMEOUT`/`START_REJECTED` ⇒ void authorization (full), release hold, notify. **Danger [V]**: with no cancel (G2) the platform keeps the idTag start-intent up to 15 min. If the driver plugs in later, `session.started` arrives for a voided session ⇒ CB immediately issues `stop` and marks `LATE_START_STOPPED`; energy delivered in the gap is operator-absorbed (tiny, bounded by stop latency). Until G2 ships, the customer is told "retry in a few minutes" |
| 8.3 | Charging starts, **connection lost** (charger offline) | Charger continues locally; platform emits `device.offline`; metering stops. CB marks `MONITORING_DEGRADED`, **does not stop or refund**, keeps retrying `stop` on reconnect only if limit already exceeded. Staleness: `sampledAt` older than 2.5 × interval |
| 8.4 | Connection lost between CB and OCPP / webhooks missing | Sync poller (`/sessions/sync`, `/events`) closes the gap; 60 s staleness ⇒ explicit `GET /sessions/{id}` |
| 8.5 | Stop command accepted but no `StopTransaction` | Keep `STOPPING`; at +60 s `GET session`; at +5 min raise operator alert (possible stuck contactor). Customer is billed only by the final session record |
| 8.6 | **Charging ends, capture fails** | Payment `CAPTURE_FAILED`; retry with backoff (idempotent capture id); session stays `COMPLETED` and receipt `PENDING_PAYMENT`; customer debt flag blocks new sessions until resolved **[U]** |
| 8.7 | Charger/vehicle stops early (EV full, error, emergency, local stop) | `session.stopped` with `stopReason` (`EVDisconnected`, `EmergencyStop`, `PowerLoss`, `Local`, …) ⇒ normal settlement on real energy |
| 8.8 | `costMinor == null` on `COMPLETED` (`MISSING_AT_START`) | The start pre-check (`TARIFF_NOT_CONFIGURED`) makes it unlikely but cannot rule it out (tariff deactivated between pre-check and `StartTransaction`; station-initiated starts) [V: `CLAUDE.md`]; when it happens ⇒ bill from CB-frozen quote price × `energyKwh`, flag for review **[U]** |
| 8.9 | `ABANDONED` session (power loss/24 h silence) | `energyKwh`/`costMinor` null [V]. Settle from last metering we hold, capped at authorization; operator review **[U]** |
| 8.10 | CB restarts mid-session | State is in Postgres; on boot the recovery job (§11) rebuilds from `/sessions/sync` + `GET /sessions?idTag&status=ACTIVE`; limit watcher resumes from last stored reading |
| 8.11 | Duplicate / out-of-order / replayed webhooks | Dedupe on `eventId` (inbox unique); apply by state-machine guard, not arrival order; late metering after `stopped` ignored |
| 8.12 | User presses stop and limit fires simultaneously | Single `STOPPING` transition guarded by row lock; second caller is a no-op |
| 8.13 | OCPP platform down / 5xx | Start refused up front (health gate) with auth voided; stop retried with backoff; alert |
| 8.14 | Rate limit `429` | Treated as retryable with `Retry-After`; stop commands have priority queue; G5 raises quota |

## 9. Charging-limit enforcement (design with stated limits)

**What the platform can do [V]:** start and stop only. No `chargingProfile`, no
`SetChargingProfile`, no energy/amount parameter on start. **Therefore limits are
enforced by CB sending `stop` based on metering.**

Mechanism **[P]**:

```
on session.metering (or poll):
   projected = totalKwh + currentPowerKw × safetyWindowSeconds / 3600
   if projected ≥ capKwh  and state == CHARGING:  → request stop (once)
safetyWindowSeconds = meteringIntervalSeconds + webhookLatency(≈2) + stopLatency(≈10) + 5
```

Limits of this mechanism — must be shown in product copy and accepted **[U]**:

* Meter cadence is charger-side (30–60 s typical, **[A]** unconfirmed; may be 0). At
  60 kW (1 kWh/min) and a 60 s interval, overshoot can be ≈ 1–2 kWh even with the
  projection. Stop latency adds up to the 30 s OCPP timeout in bad cases.
* If `currentPowerKw` is absent (null ≠ 0 [V]), derive power from two consecutive
  `totalKwh` samples; if neither is possible ⇒ limit is unenforceable for that
  station: **refuse limit-based sessions** (`limitEnforcementVerified=false`) and only
  offer `FULL` with a conservative authorization cap.
* EV-side taper/pause is irrelevant to the algorithm (we read real energy).
* **Billing rule [P/U]:** customer is billed `min(actual, limit amount)`; overshoot is
  an operator cost. Authorization = `estimate + margin` where
  `margin = maxPowerKw × safetyWindow /3600 × price` (uses 60 kW default).
* Amount limits are converted to kWh with the **frozen** price (§4), so a mid-session
  tariff change cannot move the stop point [V: snapshot at start].
* Exact limits require charger-side support (e.g. `SetChargingProfile` /
  firmware energy cap): **not available, not promised**. Phase-2 option, G1.

## 10. Energy & cost tracking

`meter_reading` rows (sessionId, `sampledAt`, totalKwh, powerKw, soc, receivedAt,
sourceEventId unique). Retention per `DATA_MODEL.md`. Monetary display uses
`Money(minor, currency, decimals)`; no division by 100 anywhere [V: XOF has 0
decimals].

## 11. Recovery (summary; details `SECURITY_AND_RELIABILITY.md`)

`@Scheduled` with `FOR UPDATE SKIP LOCKED` over sessions in transitional states
(`REQUESTED`, `START_ACCEPTED`, `STOPPING`, `COMPLETED_UNSETTLED`) and over
sync cursor; every transition re-reads authority (OCPP `GET /sessions/{id}`) before
acting.
