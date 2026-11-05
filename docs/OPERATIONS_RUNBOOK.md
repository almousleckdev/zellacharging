# Operations runbook

Alerts are `ERROR` log lines starting with `ALERT`, plus these counters.

| Signal | Meaning | Action |
|---|---|---|
| `ALERT stop not accepted ...` / `zella.charging.stop.refused` | The charger or platform refused or did not answer a stop | Check the charger. The session stays `STOPPING` and the stop is retried every 30 s. If it persists, stop the session at the OCPP platform. The customer cannot be charged beyond the hold. |
| `ALERT ... charging with no known tariff` | A session started without a price the backend can see | Configure the tariff at the OCPP platform or `zella.charging.price-per-kwh-minor`. |
| `ALERT session ... finished without a cost` | Platform reported no cost (e.g. `MISSING_AT_START`, abandoned) | Hold state is `REVIEW`: nothing was charged. Decide with the OCPP team, then adjust by an explicit ledger entry. |
| `zella.ocpp.polls.throttled` rising | The platform request budget is used up; sessions are shown from stored state and refresh slower | Check concurrent sessions against the ~20 limit; ask the OCPP team for a higher quota. Stop commands are not affected. |
| `ALERT wallet ... does not match its ledger` | Wallet and ledger disagree | Do not edit balances. Find the entries since the last good check; fix with a compensating ledger entry. |
| `ALERT payout ... unknown outcome` | A withdrawal could not be confirmed | Look it up with the provider by reference. Mark `PAID` or reverse by hand. |
| `Payment ... confirmed X for a requested Y` | Provider confirmed less than requested | The confirmed amount was credited. Check with the provider. |

## Recovery after a backend restart

All state is in PostgreSQL. On boot the schedulers resume: `SessionSweeper` walks every open hold and reconciles
against the OCPP platform; `TopupReconciliationJob` re-checks unsettled top-ups; `WithdrawalReconciliationJob`
re-checks unsettled payouts. Nothing is held in memory that cannot be rebuilt.

## Payment provider down

New top-ups return an unknown outcome and stay `PENDING`; the app tells the customer the payment is being
verified. Charging from existing balance is unaffected. After recovery the reconciliation job settles them. A
payment that never confirms expires after 30 minutes and is still re-checked for 6 hours.

## OCPP platform down

Catalog and start return `CATALOG_UNAVAILABLE` / `CHARGING_UNAVAILABLE` and nothing is reserved. Running
sessions continue on the charger. Settlement waits for the platform; holds stay open until it answers.

## Database role

The runtime role must not be a superuser, `BYPASSRLS` or the table owner, or the application refuses to start.
See `TENANCY.md`.

## Required production settings

`ZELLA_JWT_SECRET`, `ZELLA_CODE_PEPPER`, `ZELLA_OCPP_BASE_URL` (https), `ZELLA_OCPP_API_KEY`,
`ZELLA_OCPP_SIGNING_SECRET`, `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_MIGRATION_USER`, `DB_MIGRATION_PASSWORD`,
the `PVIT_*` settings, and `ZELLA_PAYMENT_PROVIDER=pvit`. Do not set `ZELLA_PAYMENT_ALLOW_SANDBOX` or
`ZELLA_OCPP_REQUIRE_SECURE_CHANNEL=false` in production.

## First deploy of this version on Railway

The deployed service keeps its existing `NDZELA_*` variables: `application.yml` reads `ZELLA_*` first and falls back
to `NDZELA_*`, for every variable.

Flyway runs migrations `V9` to `V12` on start (tenancy, country backfill to `GA`, audit, withdrawals). They cannot be
undone: take a database backup first.

Three start-up guards can stop the service on purpose. Each has an explicit override, to be used only knowingly:

| Guard | Stops the service when | Proper fix | Override (weakens security) |
|---|---|---|---|
| Restricted database role | the database user is a superuser, has `BYPASSRLS`, or owns the tables | create a login in role `zella_app` and use it as `DB_USER` (see `TENANCY.md`) | `NDZELA_TENANCY_REQUIRE_RESTRICTED_ROLE=false` |
| Secure charging-platform link | the OCPP URL is not https or no signing secret is set | set `NDZELA_OCPP_SIGNING_SECRET` (registered on the OCPP side) | `NDZELA_OCPP_REQUIRE_SECURE_CHANNEL=false` |
| Payment provider | `sandbox` is selected without permission, or `pvit` is selected with missing settings | set the `PVIT_*` variables and `NDZELA_PAYMENT_PROVIDER=pvit` | `NDZELA_PAYMENT_ALLOW_SANDBOX=true` (credits money without collecting it) |

## Partner API keys (NDZela)

1. Sign in to the admin console as super admin and call `POST /v1/admin/partners/NDZELA/keys` with `{"country":"GA"}`.
   The response contains the key once. Share it through a password manager, never email or chat.
2. NDZela stores it as `ZELLA_API_KEY` in its own service and sends it as `X-Api-Key`.
3. Rotate: issue a second key, let NDZela switch, then `DELETE /v1/admin/partners/NDZELA/keys/{id}` for the old one.
4. Revoke immediately if a key leaks: the same `DELETE`. `GET .../keys` shows `lastUsedAt` to confirm who still uses what.
5. `ZELLA_PARTNER_RATE_PER_MINUTE` (default 120) changes the per-key limit and `ZELLA_PARTNER_FEED_LAG` (default 10s)
   the delay before a change appears in the feed; both need a restart.
