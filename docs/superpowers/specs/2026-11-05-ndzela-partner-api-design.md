# NDZela partner API: design (Zella side)

Status: draft for approval. Nothing is built. Agreement with the NDZela developer: `docs/NDZELA_INTEGRATION.md`, section 8b.

## 1. Intent

NDZela owns 150+ electric cars and 100% of the stations on Zella Charging. Its drivers charge with the Zella app and
pay from their own Zella wallet. The NDZela dashboard (Charging, Sessions du réseau) already imports every OCPP session,
but a session only carries an `idTag`. Zella is the only system that knows which person an `idTag` belongs to and what
that person paid. This work gives NDZela exactly two things, and nothing more:

1. a way to **find which Zella customers are its drivers** and confirm the link;
2. a **feed of what each linked driver paid** per charge, including refunds.

## 2. Goals, non-goals, anti-goals

Goals: server-to-server, read-mostly API for one partner (NDZela) bound to one country (Gabon); incremental pull feed
like the OCPP Fleet API; every call authenticated, rate limited and audited; the feed is the source of truth.

Non-goals (v1): a webhook hint (the NDZela developer allows it as optional); other partners; a partner UI; changing
the OCPP platform or the NDZela code; a refund flow for customers (none exists today, see section 6).

Anti-goals: exposing any customer who is not a confirmed NDZela driver; names of unmatched people; wallet balances,
top-ups, mobile-money numbers or passwords; any partner access to the customer mobile API.

## 3. Constraints (from CLAUDE.md and existing code)

- Spring Modulith: new module `partner` with `domain`, `application` (+ `port/`), `infrastructure`, `web`. It reaches
  `account`, `charging` and `wallet` only through small public interfaces at their module root (as `WalletFunding`
  and `StartableConnectors` do today). It never reads their tables directly.
- Money is `long` minor units plus `currency` plus `currencyDecimals` (XAF: 0). Migrations are new `V30+`, never edited.
- Stable error `code` on every error (RFC 7807). No comments, files under ~150 lines, no duplicated code.
- Tenancy: the API key is bound to one country; every query runs with that tenant bound so Postgres RLS applies.
- Fees are percentages (`FeeCalculator`) applied to the OCPP electric cost when a session settles.

## 4. Authentication, tenancy, rate limit, audit

- A second `SecurityFilterChain` (higher order) for `/v1/partners/**` only. It does not use JWT. `PartnerKeyFilter`
  reads `X-Api-Key`, hashes it (SHA-256), looks it up in `partner_api_key`, rejects revoked or unknown keys with 401
  and a constant-time compare, binds the key's country as the tenant, and sets a `PartnerPrincipal(partner, keyId)`.
- Table `partner_api_key`: `id`, `partner` (`NDZELA`), `country_code`, `key_hash` unique, `created_at`, `expires_at`,
  `revoked_at`, `last_used_at`. The plain key is shown once at creation. Two keys may be active so NDZela can rotate;
  revoking one never affects the other. No IP allow-list (NDZela has no fixed outgoing IPs).
- Keys are created by a platform admin through a new admin endpoint `POST /v1/admin/partners/{partner}/keys` (returns
  the key once) and revoked with `DELETE .../keys/{id}`. The key is handed over through a password manager share.
- Rate limit: reuse `RateLimiter`, keyed by key id, 120 requests per minute (NDZela needs far less). 429 with `Retry-After`.
- Audit: `AuditTrail.record` on lookup, link, unlink, key created and key revoked, with the key id as actor and counts
  (never emails or phones) in the detail. Logs never contain emails, phones, names or the key.

## 5. Endpoints

All under `/v1/partners/ndzela`, JSON, HTTPS only. Errors use the existing problem format with codes like
`PARTNER_KEY_INVALID`, `PARTNER_BATCH_TOO_LARGE`, `PARTNER_CURSOR_INVALID`.

### 5.1 Lookup: `POST /id-tags/lookup`

Request: `{ "drivers": [ { "ref": "ndzela-driver-id", "email": "a@b.com", "phoneE164": "+24177000000" } ] }`, 1 to 200.
`ref` is echoed back so NDZela correlates results; email must be lowercase and phone E.164, otherwise 400.

Response: `{ "results": [ { "ref", "matches": [ { "customerId", "idTag", "matchedOn": ["email","phone"], "quality": "FULL" | "PARTIAL", "firstName", "lastName" } ] } ] }`.

- `FULL`: one customer matches on email and phone. Names are returned.
- `PARTIAL`: a customer matches on exactly one field (including two different customers, one per field). `matchedOn`
  says which. **Names are not returned on partial matches** (my call, section 9). `idTag` and `customerId` are returned.
- No match: `matches` is empty and nothing else about the person is revealed.
- `idTag` is `null` for a customer who never started a charge: lookup never creates an idTag (that registers it on
  the OCPP platform). NDZela links by `customerId`; the feed carries `customerId`, so a later idTag still joins.
- Lookup is read-only and does not link anything.

### 5.2 Confirm and remove a link: `PUT` and `DELETE /links/{customerId}`

Only linked customers appear in the feed. NDZela calls `PUT` for a `FULL` match (automatically) or a staff-confirmed
`PARTIAL` match, and `DELETE` when staff unlink. Both are idempotent. `PUT` on an unknown customer or one of another
country returns 404. `DELETE` stops the feed for that customer; past rows already read stay on NDZela's side.
Table `partner_link`: `partner`, `customer_id`, `country_code`, `linked_at`, composite FK to `customer`.
**This endpoint pair is new and must be agreed with the NDZela developer** (section 9).

### 5.3 Payment feed: `GET /sessions`

Query: `cursor` (opaque), `limit` (1 to 500, default 100). No cursor means "from the beginning" (backfill).
Response: `{ "items": [...], "nextCursor": "...", "hasMore": true }`, same shape as the Fleet API.

Item: `transactionId`, `customerId`, `idTag`, `stationId`, `connectorId`, `sessionState`, `startedAt`, `stoppedAt`,
`energyKwh`, `energyCostMinor`, `vatMinor`, `serviceFeeMinor`, `totalChargedMinor`, `currency`, `currencyDecimals`,
`paymentMethod` (`WALLET`), `paymentState`, `breakdownEstimated` (boolean), `updatedAt`.

- Only sessions of **linked** customers that reached the charger (have a `transactionId`) are returned.
- `paymentState`: `PENDING` (funds held, charge running), `CHARGED` (captured), `NOT_CHARGED` (hold released, nothing
  charged), `UNDER_REVIEW` (finished without a cost, flagged for review), `REFUNDED` (reserved: Zella has no refund
  flow yet). A change of state is the same `transactionId` with a newer `updatedAt`; the feed never emits a second
  record for one charge.
- Order is by `(updatedAt, sessionId)` ascending; the cursor encodes the last pair, so equal timestamps neither
  repeat nor skip. A tampered cursor returns `PARTNER_CURSOR_INVALID`.

## 6. Data changes (migrations)

- **V30**: `partner_api_key` and `partner_link` (both RLS by `country_code`, granted to `zella_app`).
- **V31 `session_charge`**: one row per settled session with `session_id`, `customer_id`, `country_code`,
  `energy_cost_minor`, `vat_minor`, `service_fee_minor`, `total_minor`, `currency`, `decimals`, `estimated`,
  `settled_at`. **Why:** today only the total is captured (`funding_hold.captured_minor`) and the breakdown is
  recomputed from the current fee percentages, so it would change if the rates change. `SessionSettlement` writes this
  row at settlement from the same `FeeCalculator` result. Sessions settled before V31 are backfilled with
  `estimated = true` (total from the hold, parts from the current rates).
- No refund exists in the ledger (types are `TOPUP`, `RESERVE`, `RELEASE`, `CAPTURE`). `REFUNDED` stays reserved
  until a refund flow is built; adding it later needs no change to the contract.

## 7. Tests (Testcontainers, no H2)

Key: missing, unknown, revoked, expired, wrong country; rotation with two active keys. Isolation: a Gabon key never
sees a Mali customer, and an unlinked customer never appears in the feed or in lookup names. Lookup: full, partial on
email, partial on phone, two customers split, no match leaks nothing, batch of 201 refused, malformed email or phone
refused, `idTag` null without a prior charge, no idTag created. Links: idempotent, other-country 404, unlink stops the
feed. Feed: backfill without cursor, incremental by cursor, equal timestamps, tampered cursor, state change on the same
`transactionId`, `NOT_CHARGED` after a failed start, `UNDER_REVIEW`, estimated flag on backfilled rows, minimum
columns. Rate limit 429. Audit rows written and free of personal data. Response and log content contain no secrets.

## 8. Docs to update with the change

`docs/INTEGRATION_CONTRACTS.md` (the contract), `docs/DATA_MODEL.md`, `docs/SECURITY_AND_RELIABILITY.md` (partner
keys, personal-data sharing), `docs/OPERATIONS_RUNBOOK.md` (create, rotate and revoke a key).

## 9. Open points

Mine, to confirm: (a) **no names on partial matches**, because a phone number can have changed hands and the name of a
stranger should not leave Zella; the NDZela developer asked for names only on matches, which this respects.
(b) The `PUT/DELETE /links` pair, so the feed only ever covers confirmed drivers. (c) `ref` echoed in lookup.
(d) 120 requests per minute and a 500-item page cap.

Theirs: written confirmation that Zella's terms allow sharing name, email and phone with NDZela for this purpose
(no terms or privacy file exists in this repo); whether NDZela accepts `REFUNDED` being reserved for now.

## 10. Left to the builder

Class names inside `partner`, the cursor encoding, the SQL for lookup and the feed, how the admin key endpoint is
guarded (reuse the existing admin audience), and the exact error messages.
