# Project Overview — Ndzela (customer charging platform)

Status: **DRAFT for architecture review — no implementation code exists yet.**

Evidence tags used in every document of this set:

| Tag | Meaning |
|---|---|
| **[V]** | Verified existing behavior — read in the OCPP repository code, migrations or its own verified docs |
| **[P]** | Proposed architecture — our design, not yet built |
| **[A]** | Assumption requiring validation (hardware, payment provider, product) |
| **[U]** | Unresolved business decision — needs an owner |

## 1. Business objectives

1. Let a driver find a charger, scan its QR code, choose how much to charge, pay,
   and watch the session — from a Flutter app (iOS/Android). **[P]**
2. Never double-book a connector, never charge a customer without a matching
   payment, never take a payment without a recorded outcome. **[P]**
3. Reuse the existing OCPP platform as the only authority over chargers; do not
   re-implement OCPP. **[V]** the platform already exposes a partner REST API
   (`/api/v1/**`) and signed webhooks for exactly this role.

## 2. Product scope

In scope (v1): customer accounts, station discovery on a map, QR scan, connector
selection, charging limit (energy or amount), price estimate, connector hold,
payment, remote start/stop, live monitoring, automatic stop at limit, receipt,
history. **[P]**

Hardware reality **[V]** (`CLAUDE.md`): DC, 60 kW, two guns per unit, **GB/T 20234.3
only**; the app must say so (CCS2/CHAdeMO/NACS vehicles cannot plug in). Hardware
has not yet been validated end-to-end against the platform ("not yet in commercial
service", `docs/12-platform-overview.md`).

## 3. System boundaries

```
Flutter app ──HTTPS──▶ Customer Backend (NEW) ──HTTPS+API key (+HMAC)──▶ OCPP Platform (EXISTING) ──OCPP-J/WSS──▶ Chargers
                                ▲                                              │
                                └────────── signed webhooks (HMAC) ◀───────────┘
```

* Flutter never speaks OCPP and never calls the OCPP platform. **[P]**
* The customer backend is a **separate application, separate database**, and an
  ordinary *partner client* of the OCPP platform (`external_client`). **[P]**
* The OCPP platform stays authority for: charger connectivity, connector status,
  transactions, meter values, and the tariff-priced `costMinor`. **[V]**
* The customer backend is authority for: customer identity, holds, payments,
  limits, receipts, customer-facing history. **[P]**

## 4. Main user journeys

1. Sign up / log in. 2. Browse map → station → connector availability.
3. Scan QR → connector resolved. 4. Pick limit (kWh or amount) → see estimate.
5. Pay (authorize) → connector held → start → "plug in" prompt → charging.
6. Watch live kWh / kW / SoC / cost. 7. Auto-stop at limit or manual stop.
8. Receipt, refund of unused amount, history.

Full sequences and failures: `CHARGING_WORKFLOW.md`.

## 5. Functional requirements

| ID | Requirement | Basis |
|---|---|---|
| F1 | Email/phone + password or social login; JWT access + refresh | **[P]** |
| F2 | Station list with proximity filter, connector status | **[V]** `GET /api/v1/charge-points?lat&lng&radiusKm` |
| F3 | QR resolve to `deviceId`+`connectorId` | **[V]** `GET /c/{token}` (unauthenticated, bare 404 on any failure) |
| F4 | Limit by energy (kWh) or by maximum amount | **[P]**, enforced by the customer backend — see Risk R1 |
| F5 | Price estimate before payment | **[P]**; needs the per-kWh price, **not exposed to partners today** (gap G3) |
| F6 | Hold connector for a short window | **[P]**; the OCPP platform has **no reservation concept** [V] |
| F7 | Pay with provider-authorized funds, capture actual cost, refund remainder | **[A]** provider capability unverified; **[U]** provider |
| F8 | Remote start / stop | **[V]** `POST .../connectors/{n}/start|stop` |
| F9 | Live monitoring pushed to the app | **[P]** SSE; source **[V]** `session.metering` webhook |
| F10 | Receipt (PDF/JSON) and history | **[P]** |
| F11 | Push notifications (session started/ended/failed) | **[P]** FCM/APNs |

## 6. Non-functional requirements

* Availability of the **stop path** is higher priority than any other feature (an
  unstoppable session is a safety and money problem). **[P]**
* Financial arithmetic in **integer minor units** + currency + decimals (XOF = 0
  decimals, TND = 3) — never floating point. **[V]** rule n°7 of the OCPP repo; we
  adopt it. Currency of the first deployment: XOF. **[V]** seeded tariff.
* Multi-country: country/currency come from the station, never hard-coded. **[V]**
* Customer data isolated per customer; PII minimized. **[P]**
* Idempotent writes on every money/command path. **[P]**
* Observability: structured logs with `correlationId`, metrics, reconciliation
  reports. **[P]**
* Platform limits we must live inside: see `INTEGRATION_CONTRACTS.md` §8
  (60 req/min/client rate limit, single OCPP instance, 30–60 s metering cadence).

## 7. Explicitly excluded (v1)

Roaming/OCPI, fleet billing, subscriptions/loyalty, OCPP smart charging profiles,
AC chargers, non-GB/T vehicles, invoicing with VAT (the OCPP platform "does not
calculate or apply VAT", `docs/partner-api.md` §9 [V] — a customer-side concern,
**[U]** whether needed), web app, multi-instance OCPP routing.

## 8. Confirmed vs unresolved

**Confirmed [V]:** partner API, webhooks, idempotency header, per-session tariff
snapshot, one open session per connector, one ACTIVE session per idTag.

**Unresolved [U]:**

1. Payment provider and whether it supports pre-authorization + partial capture.
2. Whether a connector hold is a real product requirement or "start now" is enough.
3. Limit semantics offered to users (kWh only, amount only, both; "approximately").
4. Refund policy for charge-never-started / charger-faulted sessions.
5. VAT/invoice legal requirements per country.
6. Who owns the production OCPP-side changes listed in `INTEGRATION_CONTRACTS.md` §9.
7. Customer-backend language level: Java 21 to match the OCPP repo **[P]** (the
   Boot 4 / Java 25 skill defaults would diverge from the sibling codebase).

## 9. Decision log (approved 2026-10-15)

| # | Decision |
|---|---|
| D1 | Stack, architecture and document set approved as written (Java 21 Modulith, Flutter/Riverpod, SSE, PostgreSQL, no Redis in v1). |
| D2 | **The OCPP platform belongs to another developer and is never modified by this project.** Gaps G1–G9 (`INTEGRATION_CONTRACTS.md` §9) and the technical risks are reported to its owner; this project designs around them and treats any not-yet-delivered gap as a hard dependency or a documented degradation. |
| D3 | **Payment: PVit aggregator, with Airtel Money Gabon and Moov Money Gabon.** Mobile money is an immediate debit (customer approves a USSD/app prompt); **pre-authorization and partial capture are assumed NOT available [A — confirm with PVit]**. Consequence: payment design is the **prepaid wallet** (`STATE_MACHINES.md` §4.1, now the primary design). Currency XAF, 0 decimals. |
| D4 | Overshoot beyond a customer's limit is billed `min(actual, limit)`; the operator absorbs the excess. |
| D5 | Connector hold is a real requirement. |
| D6 | Refund policy, VAT/receipt rules and retention: proposals in the documents accepted as the working baseline (legal review still advised for holding customer funds in a wallet [U]). |
| D7 | Java 21 (aligned with the OCPP repository). |
| D8 | Product/app name: **Ndzela**. Mobile UI design will be supplied by the product owner when the mobile phase begins; no UI is built before it. |

## 10. Registration and login (Ndzela) — confirmed requirements

* **Email/password registration:** first name, last name, **email or phone number
  (at least one)**, password.
* **Google or Apple sign-in:** a **phone number is then mandatory** (collected and
  verified right after the social sign-in if the provider did not supply one; the
  account stays `PENDING_PHONE` until verified and cannot hold or start sessions).
* Phone number is the primary identity for mobile-money payments (Airtel/Moov Gabon),
  so it is normalized to E.164 (`+241…`) and unique when present; email unique when present.
* **[U]** SMS/OTP provider for Gabon and transactional email provider (needed for
  verification and password reset); phone verification by OTP assumed.
