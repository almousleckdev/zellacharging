# Security and Reliability

Tags: **[V]** verified in the OCPP platform · **[P]** proposed · **[A]** assumption · **[U]** unresolved.

## 1. Authentication & authorization (app ↔ CB) [P]

* Passwords: argon2id; or passwordless OTP **[U]**. Rate-limited login (per account
  and per IP), generic error messages.
* Access JWT 15 min (asymmetric signing, `kid` rotation), refresh 30 d, rotating with
  reuse detection (reuse ⇒ revoke whole family). Refresh token in OS secure storage.
* **Authorization = ownership filter on every query** (`WHERE customer_id = :jwt.sub`).
  Foreign resources return `404`, never `403` (same anti-enumeration rule the OCPP
  platform applies [V]). No customer id in request bodies/paths for "my" resources.
* Admin/operator access to CB data is a separate role set; not in v1 scope.

## 2. Secure CB ↔ OCPP communication

| Control | Setting |
|---|---|
| Transport | TLS only; CB pins the OCPP platform host in config; no plaintext fallback |
| Credential | `X-API-Key` for client `customer-backend`, **expiry set** [V: V31], scopes `READ, CHARGING_COMMANDS, ID_TAGS_WRITE` only, mode `PARTNER` |
| Request signing | `require_request_signature=true` (`docs/16`) [V] so a leaked key alone is useless |
| Network | IP allowlist on the client [V; source IP read from `X-Real-IP` set by the proxy chain, not spoofable by `X-Forwarded-For`] |
| Inbound webhooks | verify `X-Webhook-Signature = HMAC-SHA256(secret, ts + "\n" + rawBody)` on **raw bytes**, constant-time compare, reject outside ±300 s, dedupe on `eventId`, then respond `2xx` fast and process from the inbox |
| Secrets | env / secret store, never in git (sibling rule 1 [V]); rotation runbook: `regenerate-api-key`, `regenerate-signing-secret` endpoints exist [V] |
| Blast radius | one shared key for all customers ⇒ compromise = ability to start/stop **our own** sessions only (stop is limited to sessions our client started [V]); sessions of other partners unreachable [V] |

## 3. Customer data isolation [P]

Ownership filters (above) + automated tests asserting two-customer isolation on every
endpoint; DB role without DDL; `audit_record` append-only; logs carry `customerId`
pseudonym, never email/phone/token; payment card data never touches CB (provider-hosted
fields/SDK, SAQ-A scope).

## 4. Payment webhook verification [P]

Provider-specific signature scheme verified over raw body; timestamp tolerance;
`payment_webhook_inbox` unique on provider event id; the handler is "verify → insert →
200", processing is asynchronous and guarded by the payment state machine. Never trust
the payload amount: re-fetch the object from the provider API before changing state
when the event is money-moving.

## 5. Idempotency and replay protection

| Surface | Mechanism |
|---|---|
| App → CB mutating endpoints | `Idempotency-Key` header **required** on hold/session/stop/refund-request; stored with request fingerprint; same key + different body ⇒ 422; replayed response flagged |
| CB → OCPP start/stop | key per attempt `{kind}:{sessionId}:{n}` [V behaviour: platform replays stored result, releases only on thrown error] |
| CB → PSP | `auth:{sessionId}`, `cap:{sessionId}`, `ref:…` keys |
| Webhooks in (OCPP, PSP) | inbox UNIQUE(event id); timestamp window; HMAC |
| DB | unique indexes of `DATA_MODEL.md` §3 are the last line of defense |

## 6. Concurrency — reservation and start

1. DB partial unique indexes (hold & session, per connector and per customer).
2. Lazy expiry inside the insert transaction.
3. Platform-side backstops [V]: `uk_one_open_session_per_connector`, per-idTag
   in-flight start reservation, `uk_one_active_session_per_id_tag`.
4. Residual race with non-CB users (RFID/other partner): documented, bounded to a
   rejected start + voided payment, never a double charge (`CHARGING_WORKFLOW.md` §6).
5. Frontend validation is never trusted for any of this.

## 7. Reconciliation

Scheduled jobs (idempotent, `SKIP LOCKED`, results stored in `reconciliation_run`):

| Job | Cadence | Compares | Action |
|---|---|---|---|
| Session sync | continuous poll 30 s | OCPP `/sessions/sync` cursor vs local sessions | apply missed `started/stopped/metering` via guards |
| Live-session audit | 60 s | OCPP `GET /sessions?status=ACTIVE` vs local live sessions | unknown ACTIVE with our idTag ⇒ adopt/stop; local live with no platform ACTIVE for > grace ⇒ investigate |
| Event catch-up | 5 min | `/api/v1/events` `FAILED` entries | replay into inbox |
| Payment ↔ session | 5 min | invariants list, `STATE_MACHINES.md` §7 | void orphan authorizations, retry captures, open `REVIEW` |
| Provider ↔ ledger | daily | PSP settlement report vs `payment_transaction` | report differences; no auto-correction of money without approval |
| Cost check | on every `COMPLETED` | our `round_HALF_UP(kWh×price)` vs platform `costMinor` | > 1 minor unit ⇒ `REVIEW` |

## 8. Session recovery

Persisted deadlines (not in-memory timers); on boot: (1) resume sweepers, (2) walk
the OCPP sync cursor, (3) reconcile each live session by `GET /sessions/{id}`
or by `(idTag, status=ACTIVE)` when `transactionId` unknown, (4) re-arm the limit
watcher from the last stored reading. If metering is stale at boot, do **not** stop
sessions solely on staleness; only when a limit is provably exceeded or the user/ops
requests stop. A down OCPP platform ⇒ new sessions refused fast; running sessions
continue on the charger.

## 9. Observability and audit

* Structured JSON logs; `correlationId` propagated app → CB → OCPP (`X-Correlation-Id`;
  also `orderId=sessionId` echoed in OCPP command results [V]).
* Metrics: webhook inbox lag, command success/latency by kind, time-to-start,
  time-to-stop after limit (overshoot kWh histogram — the key product KPI), sessions
  per state, `CAPTURE_FAILED` count, reconciliation mismatches, 429 rate toward OCPP.
* Alerts: stop not effective > 60 s (page), `STOPPING` > 5 min (page), capture failing,
  inbox lag > 60 s, OCPP platform unreachable, quota near 60 req/min.
* Audit: every state transition and every money movement is an `audit_record`.

## 10. Failure handling matrix

| Failure | Detection | Response |
|---|---|---|
| OCPP platform down | health probe, 5xx | refuse new holds/sessions; retry stop; alert |
| Webhooks stop arriving | inbox lag + sync poller | poller applies state; page if > 5 min |
| Charger offline mid-session | `device.offline`, stale `sampledAt` | `monitoring_degraded`, notify user; no stop/refund |
| PSP down | timeouts | no new sessions; captures retried; hold voids queued |
| CB crash | restart | §8 |
| Duplicate/malformed webhook | inbox unique / HMAC | drop, log |
| Rate limit hit | `429` | honour `Retry-After`, prioritize stop; CB never forwards per-user polling |
| Clock skew | webhook timestamp reject | NTP; alerts |

## 11. Platform risks inherited [V]

Single OCPP instance (S6/S27); in-memory rate limiter; webhooks manual registration;
hardware not yet validated in the field; metering cadence unknown. Tracked in
`IMPLEMENTATION_PLAN.md` risks.
