# Zella Charging <-> NDZela: who charged, which car, what they paid

Status: both sides are built (Zella 2026-11-05, NDZela since); the contract is in `docs/INTEGRATION_CONTRACTS.md` section 11
and the key procedure in `docs/OPERATIONS_RUNBOOK.md`. Sections 3 to 5 below are the original proposal; section 8b records
what was agreed and the built contract wins on any difference.
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

## 11. Zella asks NDZela whether a driver may charge free on Sunday (2026-11-22)

NDZela is the only judge of the free Sunday charge (agreement, the free window of the country, by default the whole
Sunday 00:00 to 23:59 Gabon time and editable in the dashboards, zero unpaid days computed live at that moment); the
decisions are NDZela ADR-0048 and ADR-0049. Zella has no window rule and no Sunday rule: it uses NDZela's answer
as given and holds the one-per-Sunday claim. How the claim, the free start and the 22:00 stop work is in
`CHARGING_WORKFLOW.md` (Free Sunday charge).

- Zella calls `GET {ZELLA_NDZELA_BASE_URL}/api/v1/partners/zella/customers/{customerId}/free-charging` with
  `X-Api-Key: {ZELLA_NDZELA_API_KEY}`. It reuses the settings, timeouts (3 s connect, 5 s read) and client of section 10.
- NDZela answers `{eligible, reason, agreementId, sundayDate, windowEndsAt, window}`. The three middle fields are null when not eligible; `window` is `{day, from, until, endOfDay}` whenever the driver is known (`until` is `23:59` when the window runs to the end of the day) and Zella passes it on to the app so no client hard-codes the hours.
  `reason` is `ELIGIBLE`, `NO_AGREEMENT`, `AGREEMENT_SUSPENDED`, `AGREEMENT_NOT_STARTED`, `AGREEMENT_ENDED`,
  `OUTSIDE_WINDOW`, `UNPAID_DAYS`, `DRIVER_SUSPENDED` or `NOT_LINKED`.
- Zella adds two local reasons: `UNAVAILABLE` (timeout, network error, non-2xx status, malformed JSON, missing settings,
  or an eligible answer without agreement, date or end time) and `SUNDAY_USED` (NDZela says eligible but this customer
  or another customer holds a claim on the same `agreementId`, for that `sundayDate`). Failure is closed: no free
  benefit, the paid path continues. NDZela's own rate limit (30 reads per minute per customer under a ceiling of 2000
  per minute for all customers, answered 422 `TOO_MANY_REQUESTS`) and a wrong key (401) also end as `UNAVAILABLE`; at
  start time that means an ordinary paid charge. Failures are logged with the exception type and HTTP status only, never
  the URL (it holds the customer id) or NDZela's answer body.
- A customer without an NDZela partner link is answered `NOT_LINKED` inside Zella: no request and no customer id reach NDZela.
- Inside Zella, `FreeSundayAccess.offer` is read-only and used for display, with a 30-second per-customer cache (5 seconds
  for `UNAVAILABLE`). `FreeSundayAccess.claimFor` never uses the cache: it calls NDZela, then
  takes the unique Sunday claim with NDZela's `agreementId`, `sundayDate` and `windowEndsAt`.

## 12. Zella administrators manage free-charging agreements through NDZela (2026-11-22)

NDZela owns the agreements. The Zella dashboard never stores one: each call is forwarded and NDZela answers.

- Zella calls `{ZELLA_NDZELA_BASE_URL}/api/v1/partners/zella/free-charging/...` with `X-Api-Key` (same key and client as
  sections 10 and 11, `Redirect.NEVER`, 3 s connect, 5 s read) and `X-Actor`, the acting administrator's e-mail with
  control and non-ASCII characters removed, capped at 100 characters (their id when nothing remains). NDZela records it as
  `zella:<actor>`. Routes: `GET|POST /agreements`, `PUT /agreements/{id}`, `POST /agreements/{id}/suspend|resume|revoke`,
  `GET /usage?page&size`.
- Zella admin routes, all under `/v1/admin/free-charging`. The country is the default country (Gabon): an admin without a
  grant on it gets 403 `COUNTRY_FORBIDDEN` on every route before NDZela is called.

| Route | Permission | Answer |
|---|---|---|
| `GET /agreements` | `FREE_CHARGING_READ` | array of agreements |
| `POST /agreements` `{customerId, startsOn, endsOn?, conditions?}` | `FREE_CHARGING_MANAGE` | 201, the agreement |
| `PUT /agreements/{id}` `{startsOn, endsOn?, conditions?}` | `FREE_CHARGING_MANAGE` | the agreement |
| `POST /agreements/{id}/suspend`, `/resume`, `/revoke` | `FREE_CHARGING_MANAGE` | the agreement |
| `GET /usage?page=0&size=20` (size 1 to 100) | `FREE_CHARGING_READ` | `{items, page, size, total}` |

- An agreement is `{id, driverId, driverName, startsOn, endsOn, status, conditions, eligibility, unpaidDays, lastFreeUse}`.
  A usage row is `{sessionId, driverId, driverName, startedAt, energyKwh, stopReason}`.
- Errors: NDZela answers with 400, 404, 409 or 422 and a stable `code` (`FREE_AGREEMENT_EXISTS`,
  `FREE_AGREEMENT_NOT_LINKED`, `FREE_AGREEMENT_DATES_INVALID`, `FREE_AGREEMENT_REVOKED`, `VALIDATION_ERROR`,
  `RESOURCE_NOT_FOUND`): Zella passes the status and code through, with its own neutral message. Anything else (timeout,
  5xx, 401, 403, 429, redirect, unreadable answer, missing settings) becomes 503 `FREE_CHARGING_UNAVAILABLE`. Zella's own:
  `PERMISSION_DENIED` (403), `INVALID_PAGING` (400), 400 for a malformed id. The key and NDZela's wording never reach the
  response or the logs.
- Audit: one entry per successful mutation, only after NDZela accepted: `FREE_AGREEMENT_CREATED`, `UPDATED`, `SUSPENDED`,
  `RESUMED`, `REVOKED`, subject the agreement id (the create entry holds "customer <uuid>"). No entry on refusal or outage.
- Permissions (migration V44): `FREE_CHARGING_READ` for `ADMIN`, `SUPPORT`, `FINANCE`, `OPERATIONS`;
  `FREE_CHARGING_MANAGE` for `ADMIN`; `SUPER_ADMIN` has both.

- Dashboard page "Gratuité" (`/free-charging`, nav entry shown with `FREE_CHARGING_READ`, buttons with `FREE_CHARGING_MANAGE`):
  tabs Drivers and History, code in `dashboard/src/features/free-charging`, calls these routes through the `/api/admin`
  BFF proxy. The add form picks a Zella customer linked to NDZela (the admin API takes `customerId`; the backend checks the Zella link in the default country, then NDZela resolves the driver through a `LINKED` id tag only and answers `FREE_AGREEMENT_NOT_LINKED` if it has none). The page holds
  no eligibility rule: it shows the reason code the backend returns.
