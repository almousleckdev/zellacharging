# Zella Charging <-> NDZela: who charged, which car, what they paid

Status: the Zella side is built (2026-11-05); the contract is in `docs/INTEGRATION_CONTRACTS.md` section 11 and the key
procedure in `docs/OPERATIONS_RUNBOOK.md`. The NDZela side is not started. Sections 3 to 5 below are the original proposal;
section 8b records what was agreed and the built contract wins on any difference.
Written 2026-11-05 after reading both codebases (read-only on NDZela).

## 1. The goal

NDZela owns 150+ electric cars and hires drivers (23,000 FCFA rent per day). Drivers charge at the company's stations
using the **Zella Charging app**. Today the NDZela dashboard (`/charging`, tab **Sessions du réseau**) already lists the
charging sessions it pulls from the OCPP platform, but a session only shows a technical `idTag`.

We want each session in **Sessions du réseau** to show:

- the **driver** (first name, last name, email, phone) and their **NDZela contract**
- the **car** (plate, model) assigned to that driver at that time
- the session itself (station, connector, start and end, energy, duration, battery before and after)
- what the driver **actually paid** (see section 5)

## 2. What exists today

**OCPP platform** (`zella-ocpp.com`): produces sessions. A session has `transactionId`, `deviceId`, `connectorId`, `idTag`,
`status`, `startedAt`, `stoppedAt`, `energyKwh`, `durationSeconds`, `stopReason`, `costMinor` (energy x tariff, before VAT),
`currency`, `pricePerKwhMinor`. It knows nothing about vehicles, drivers or payments.

**NDZela backend** (`ocpp` package, ADR-0017): pulls sessions with the Fleet API incremental sync
(`GET /api/v1/sessions/sync`, cursor) and stores them in `charging_sessions`. Webhooks only trigger a sync.
Roadmap Phase 2 (`id_tags`: idTag -> vehicle -> driver) and Phase 3 (what was paid) are **blocked on the charging
application**, which is Zella.

**Zella Charging backend** (customer app): when a customer starts a charge, Zella gives the customer a **stable idTag**
(`Z-` + 12 characters, one per customer, stored in `customer_id_tag`) and registers it on the OCPP platform. Every session
that customer starts uses that same idTag. Zella knows, per customer: `id`, `first_name`, `last_name`, `email`,
`phone_e164`. Zella also stores its own `charging_session` row per charge, keyed by the same `transaction_id`.

So the **missing link is `idTag -> person`**, and only Zella can provide it.

## 3. How a Zella customer is recognised as an NDZela driver

Agreed with the business: an NDZela driver signs up in Zella with the **same email, first name, last name and phone
number** as in their NDZela record.

Proposed matching (NDZela side decides, Zella only answers lookups):

1. NDZela sends the drivers it wants resolved: normalised email and phone (E.164).
2. Zella returns the `idTag` of every customer whose **email and phone both match**.
3. NDZela stores `idTag -> driver` and derives the car from the driver's contract at the session's `startedAt`.

Names are returned so NDZela can show a mismatch warning, not to decide the match.

## 4. Proposed Zella endpoint (server to server)

Authenticated with a dedicated API key header (`X-Api-Key`), HTTPS only, rate limited, every call audited.

**`POST /v1/partners/ndzela/id-tags/lookup`**

Request: `{ "drivers": [ { "email": "a@b.com", "phoneE164": "+24177000000" } ] }` (up to 200 per call)

Response: `{ "matches": [ { "email": "a@b.com", "phoneE164": "+24177000000", "idTag": "Z-8K2M...", "customerId": "uuid", "firstName": "...", "lastName": "..." } ], "unmatched": [ { "email": "..." } ] }`

Why a lookup and not "give me all customers": Zella also has **public customers** who are not NDZela drivers. Their data
must not leave Zella. NDZela only learns about the people it names.

NDZela re-runs the lookup when a driver is created or when an unmatched driver may have registered since. An idTag
never changes for a customer, so results can be cached.

## 5. What the driver paid

Facts about Zella payments, so the NDZela team does not assume the wrong model:

- A driver **tops up a Zella wallet** by mobile money (Airtel Money or Moov Money, via PVit). A charge **debits the
  wallet**. There is no mobile-money transaction per charge; the mobile-money reference belongs to the top-up.
- The amount debited for a charge is the OCPP energy cost plus Zella's service fee and VAT. The OCPP `costMinor`
  alone is **not** what the driver paid.

Proposed second endpoint for Phase 3:

**`GET /v1/partners/ndzela/sessions?updatedSince=<cursor>&limit=...`**, same incremental style as the Fleet API:
`{ items, nextCursor, hasMore }`, each item: `transactionId`, `idTag`, `state`, `energyKwh`, `energyCostMinor`,
`serviceFeeMinor`, `vatMinor`, `totalChargedMinor`, `currency`, `currencyDecimals`, `paymentMethod: "WALLET"`,
`updatedAt`. Only sessions of idTags that NDZela resolved in section 4 are returned.

NDZela joins on `transactionId` (the same value as in its own `charging_sessions`).

## 6. Rules to keep

- Money is integer minor units plus currency plus decimals. XAF has 0 decimals.
- Country scope: Gabon first. Both sides already isolate data per country; the API key is bound to one country.
- Charging cost never enters NDZela rent or arrears (NDZela rule, `ocpp-integration.md`).
- Zella does not change the OCPP platform or the NDZela code; each side builds its own part.
- Personal data shared: name, email, phone, idTag, only for lookups NDZela initiates.

## 7. Open questions for the NDZela developer

1. Is matching on **email and phone together** right, or should either one be enough?
2. Is a pull model (NDZela calls Zella, like it does for OCPP) acceptable, or do you prefer Zella to push a webhook
   when a driver's session finishes?
3. Which fields do you want on the Sessions du réseau row for the payment: only the total, or energy, fee and VAT?
4. What if a driver changes phone or email in Zella later: should the idTag still map to the same NDZela driver?
5. How do you want the API key exchanged, and which IPs will call us?
6. The OCPP credential only sees one charge point (`DY202609110007`) and sessions from 2026-10-03. Will the production
   key cover all NDZela stations?
7. Do you need history, meaning drivers' sessions before this link exists?

## 8. Answered on the Zella side (2026-11-05)

1. All stations in Zella Charging are owned by NDZela. There are no other operators, so no per-station visibility rule is needed.
2. Drivers always pay from their own Zella wallet. NDZela never pays a driver's charge.

Still open: expected volume (150 cars, charges per day) to size rate limits.

## 8b. Decisions agreed with the NDZela developer (2026-11-05)

1. Automatic link only when **email and phone both match**. The lookup also returns `matchedOn` (for example
   `["email","phone"]`). A one-field match is returned flagged `partial`; NDZela never links it automatically and staff
   confirm by hand. NDZela normalises before sending: lowercase email, E.164 phone.
2. **Pull**, incremental (`updatedSince` cursor, `{items, nextCursor, hasMore}`). An optional webhook is only a hint
   ("come and read"), signed with HMAC-SHA256 over the body. The feed is the source of truth.
3. The payment feed carries all fields (energy cost, service fee, VAT, total, currency, decimals, payment method, state).
   A refund or reversal is a **state change on the same `transactionId`**, never a separate record.
4. NDZela stores `idTag` and `customerId` against the driver; the link follows `customerId`, so a later email or phone
   change on either side does not break it. Only NDZela staff unlink. Lookups only find new links.
5. API key sent as `X-Api-Key`, bound to Gabon, exchanged through a password manager share (not email or chat). No IP
   allow-list; rate limiting and key rotation instead.
6. NDZela needs a production OCPP key covering all its stations; arranged with the OCPP platform team.
7. History: the payment feed must be readable from the beginning (first call without a cursor).
8. Volume: about 450 charges a week (about 65 a day); NDZela polls every 10 to 60 seconds with small pages and sends
   lookups in batches up to 200, so 60 requests a minute is enough.
9. Names are returned for matches only, never for unmatched people. Zella must confirm in writing that its terms allow
   NDZela to receive these customers' name, email and phone for this purpose.

## 9. Suggested order of work

1. NDZela confirms sections 3 to 5 (this review).
2. Zella builds the lookup endpoint and an NDZela API key, with tests and audit.
3. NDZela builds Phase 2: store `idTag -> driver`, show driver and car on Sessions du réseau.
4. Zella builds the sessions payment feed; NDZela builds Phase 3 on top.

## 10. Zella reads the contract and the car of a linked driver (2026-11-16)

The customer page of the Zella dashboard shows, in **NDZela driver**, the driver's contract number, contract period and
daily rent, and the car (fleet number such as `V001`, plate, model, colour). The data stays in NDZela: Zella asks for it
when an admin opens the page of a **linked** customer, and keeps no copy.

- Zella calls `GET {ZELLA_NDZELA_BASE_URL}/api/v1/partners/zella/customers/{customerId}/driver` with
  `X-Api-Key: {ZELLA_NDZELA_API_KEY}`. `customerId` is the Zella customer id NDZela already stores on the link.
- NDZela answers `{driver, contract, car}` (`contract` and `car` are null when the driver has no active contract). It
  never returns identity documents, addresses, birth data, VIN or prices. Not linked, or unknown: 404.
- Endpoint: `GET /v1/admin/customers/{country}/{id}/ndzela-driver` (permission `CUSTOMERS_READ`). Errors:
  `NDZELA_NOT_LINKED` (404), `NDZELA_DRIVER_NOT_FOUND` (404), `NDZELA_UNAVAILABLE` (503, not configured or unreachable).
- Keys: NDZela holds `ZELLA_PARTNER_KEY`, Zella holds the same value as `ZELLA_NDZELA_API_KEY`. Generate a long random
  value and share it through a password manager, never email or chat. Rotate by changing both.
