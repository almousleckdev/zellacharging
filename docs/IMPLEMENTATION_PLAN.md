# Implementation Plan

**Status: not started. Nothing below begins until this documentation set is approved.**
Each phase ends with a demo, green CI, and a docs update. No phase is "done" without
the acceptance evidence listed (verify, then claim).

## 0. Prerequisites outside our code (start immediately — long lead)

| # | Item | Owner | Blocks |
|---|---|---|---|
| X1 | ~~Choose payment provider~~ **Decided: PVit (Airtel/Moov Gabon)**. Remaining: PVit sandbox + written answers on top-up API, webhooks/signature, payouts, fees; get written answers: pre-auth, partial capture, void, refund, webhooks, sandbox, XOF support, mobile-money flow | Business | Phase 4 |
| X2 | OCPP platform changes **G3** (tariff read) and **G2** (cancel start) specified & scheduled; **G5** quota raised for our client | OCPP team | Phase 3/5 |
| X3 | Provision on a **test** OCPP deployment: `external_client customer-backend` (PARTNER, scopes, expiry, IP allowlist, signing on), station owner assignment, webhook destination | OCPP ADMIN | Phase 3 |
| X4 | Field test on the real GB/T unit: confirm `connectorId` numbering, `MeterValueSampleInterval`, power/SoC in MeterValues, remote-start-without-plug timeout, stop latency | Hardware + OCPP team | Phase 5 limit accuracy; commercial launch |
| X5 | Product decisions: hold TTL, overshoot billing policy, refund policy, receipt/VAT rules, retention | Business/Legal | Phases 2,4,6 |

## Phase 0 — Foundations (1 week)

Scope: `users/backend` skeleton (Boot 4.1/Java 21/Modulith, Flyway, Testcontainers,
no H2), `ModuleStructureTest`, `shared` (`Money`, errors RFC 7807, clock), CI job,
Docker, OpenAPI export. Flutter: project structure, Riverpod, router, Dio, theme, l10n,
lint rules; replace counter template.
Acceptance: `./mvnw verify` green in CI incl. Modulith verify; app boots to splash on
iOS+Android; `Money` property tests (rounding HALF_UP, 0- and 3-decimal currencies).
Depends on: nothing.

## Phase 1 — Accounts (1–1.5 weeks)

Backend `account`: register/login/refresh/logout/delete, idTag allocation + registration
against OCPP (behind `ocppclient` port, stubbed with WireMock), ownership-filter test
harness. Flutter: onboarding, auth screens, secure storage, auth interceptor with
single-flight refresh.
Acceptance: two-customer isolation test; refresh-reuse revokes family; idTag registration
survives timeout+retry (WireMock fault injection); widget + provider tests.

## Phase 2 — Catalog, QR, quote (1.5 weeks) — depends on X2(G3) or a stub

Backend `catalog` (cached read-through), `pricing` (quote, cap math), QR resolve proxy.
Flutter: map (MapLibre), station detail, QR scanner, limit picker, estimate.
Acceptance: **contract tests against pinned OCPP OpenAPI** (fail on drift);
cap/estimate unit tests incl. XOF (0 dp) and TND (3 dp); QR bare-404 handled; offline
station never offerable.

## Phase 3 — Integration core (2 weeks) — depends on X3

`ocppclient`: signed REST client (key+HMAC+idempotency), webhook receiver + inbox,
sync poller, `ChargerGateway` port. `session_command` machine with outbox worker.
Acceptance (against test OCPP + `tools/simulator.py`): start→`session.started`→metering→
stop→`session.stopped` end-to-end; duplicate/out-of-order webhook replay tests; kill CB
mid-session and recover from sync cursor; `Idempotency-Replayed` behaviour test proving
new-attempt-new-key logic; 429 handling.

## Phase 4 — Booking & wallet payments (2.5 weeks) — depends on X1 (PVit sandbox), X5

Wallet ledger + PVit top-up replace card-style authorize/capture (D3).

`booking` (holds + constraints), `payment` (port + adapter, authorize/capture/void/refund
machines, webhook verification, ledger fallback if X1 says no pre-auth).
Acceptance: **concurrency test** — N parallel hold/session attempts on one connector ⇒
exactly one winner (Testcontainers Postgres); expiry race tests; duplicate PSP webhooks;
capture-failure retry schedule; payment-ok/charge-never-starts ⇒ voided; property tests
on settlement amounts.

## Phase 5 — Session orchestration & limits (2.5 weeks) — depends on Phases 3,4; X4 for tuning

`session` orchestrator, limit watcher (projection algorithm), degraded monitoring,
late-start rule, SSE endpoint with `Last-Event-ID`, FCM notifications.
Acceptance: simulator scenarios — limit hit with 30/60 s intervals (record overshoot
distribution); no `currentPowerKw`; metering stops; charger offline; late start after
timeout; user-stop vs limit race; CB restart at each state. Overshoot KPI measured and
published to Business. **Real-charger run (X4) is an exit gate for "limit accuracy"
claims.**

## Phase 6 — Receipts, history, reconciliation, ops (1.5–2 weeks)

`receipt`, history API, reconciliation jobs & report, dashboards/alerts, runbooks (key
rotation, webhook re-registration, stuck session). Flutter: live session screen, history,
receipts, error/empty/loading states, dark/light, tablets.

## Phase 7 — Hardening & launch readiness (2 weeks)

Security review (use the sibling's `revue-securite` approach), dependency scan, pen-test
checklist, load test (rate limit budget: peak concurrent sessions × events), backup/
restore drill, chaos tests (kill OCPP, PSP, CB), store-submission checklist, staged rollout.
Acceptance: all invariants in `STATE_MACHINES.md` §7 monitored; zero unreconciled money in
a 7-day soak on test; restore drill < RTO; go/no-go checklist signed.

## Testing requirements (all phases)

* Unit: state machines (table-driven over every allowed/forbidden transition),
  money math, signature verification.
* Integration: Testcontainers Postgres, real Flyway; **no H2** (sibling rule).
* Contract: consumer tests vs pinned `public-api-v1` OpenAPI; PSP sandbox contract tests.
* E2E: simulator-driven scenarios (`tools/simulator.py` of the OCPP repo) in CI.
* Concurrency & fault injection (WireMock delays/drops, kill -9 mid-flow).
* Flutter: unit (use-cases), provider tests, widget/golden, integration_test for the
  happy path against a mock server.

## Risk register

| ID | Risk | Impact | Likelihood | Mitigation |
|---|---|---|---|---|
| R1 | No native charging limit; metering interval unknown → overshoot | Customer/operator money | High | Projection stop, margin in authorization, billing cap policy [U], refuse limits on unverified stations, X4 |
| R2 | No start-cancel (G2) → idTag locked ≤15 min, late start after payment void | UX, small revenue loss | High | Rule L auto-stop; G2 in OCPP backlog |
| R3 | Payment provider lacks pre-auth/partial capture | Redesign of §4/5 | Medium | Prepaid-wallet ledger fallback; decide at X1 |
| R4 | Tariff unreadable (G3) | Cannot quote | Certain until fixed | Hard dependency X2 |
| R5 | Single shared API key + 60 req/min quota | Throttling = unstoppable sessions | Medium | Webhooks not polling, stop priority, raise quota, per-attempt budgeting |
| R6 | Hardware unvalidated; firmware quirks (connector numbering, power units) | Wrong behaviour in field | Medium | X4 gate, per-model capability records |
| R7 | OCPP gateway single instance (S6/S27) | Availability | Medium | Out of scope; track; stop-path monitoring |
| R8 | Walk-up user takes the "held" connector | Rejected start, voided payment | Medium | Pre-start status check, clear UX, optional `ReserveNow` later |
| R9 | Webhook loss | Stale state | Medium | Sync poller + reconciliation |
| R10 | Money drift between systems | Financial | Low–Med | Reconciliation + REVIEW queue, no auto-correction |

## Recommended sequence

X1–X5 in parallel → Phase 0 → 1 → 2 → 3 → 4 → 5 → 6 → 7. Phases 1 (Flutter) and 3
(backend) can overlap with two engineers; Phase 5 must follow 3 and 4.
