# Payments, wallet and charging budget

## Model

Prepaid wallet. Charging is paid from the wallet; the wallet is funded by mobile-money top-ups. A charge
cannot start unless the funds are already credited and reserved, which is how "no confirmed payment, no
charging" is enforced: the reservation needs a credited balance, and a balance is only credited after the
provider confirms.

Money is `long` minor units with a currency. XAF and XOF have 0 decimals. No floating point anywhere.

## Provider boundary

| Port | Purpose | Implementations |
|---|---|---|
| `PaymentGateway` | `supports(country, operator)`, `collect`, `status` | `PvitGateway` (Airtel Money, Moov Money, Gabon), `SandboxPaymentGateway` (non-production only) |
| `PayoutGateway` | `supports`, `payout`, `status` | none yet |

`PaymentGateways` / `PayoutGateways` select the gateway by `(tenant country, operator)`. Adding Mali means a new
adapter and new `MobileOperator` values; no change to wallet, ledger or charging code.

The sandbox gateway credits money without collecting it. `PaymentProviderGuard` refuses to start unless
`zella.payment.allow-sandbox=true`, which only the dev profile and the staging compose file set.

## Top-up lifecycle

`PENDING -> SUCCEEDED | FAILED | EXPIRED`. An `EXPIRED` top-up is still re-checked for `late-window` (6 h) and is
credited if the provider confirms late.

1. The client sends `Idempotency-Key` (required). A transaction row exists before the provider is called.
2. `collect` is sent with our own reference: the country plus 18 random base32 characters, 20 in all (withdrawals add a `W` and use 17 random characters). PVit's payment endpoint rejects references over 20 characters (its status endpoint allows 30); both limits were found with live calls, and `TopupReferenceTest` pins the length. A refusal (HTTP 400/422) fails the top-up. A
   timeout or 5xx is an unknown outcome: the top-up stays `PENDING`, never `FAILED`.
3. Confirmation comes only from `status(reference)`, queried by our server. The callback
   (`POST /v1/payments/callback`, unauthenticated, rate limited) carries no trusted data: it only names a
   reference and triggers that status query. A forged callback cannot credit anything.
4. Credit rules: the provider reference must equal ours, the status must be success, the confirmed amount must be
   positive. The wallet is credited `min(confirmed, requested)`; a smaller confirmed amount is logged as an error.
5. A single guarded transition `PENDING|EXPIRED -> SUCCEEDED` plus a ledger entry keyed `topup:<id>` makes repeated
   callbacks, retries and the reconciliation job idempotent.
6. While one top-up is `PENDING`, a second is refused (`TOPUP_PENDING`) so an impatient retry cannot double pay.
7. `TopupReconciliationJob` (every 60 s, per country) re-checks unsettled top-ups older than 3 minutes.

## Withdrawal lifecycle (disabled)

`REQUESTED -> PROCESSING -> PAID`, or `-> FAILED` (funds returned) or `-> REVIEW`.

* Disabled by `zella.wallet.withdrawal.enabled=false`. No payout provider has been verified, so none is wired.
* Destination is the customer's verified phone only; the password is required at request time.
* Funds leave the balance at request time, in the same transaction as the withdrawal row and the ledger entry.
* A reversal happens only on a provider-confirmed failure, exactly once. An unknown outcome is never reversed
  automatically: after `review-after` (2 h) it moves to `REVIEW` and raises an alert, because the money may have left.
* Limits: minimum, maximum and rolling 24 h total. Idempotency key required.

## Ledger

`ledger_entry` is append-only for the application role (`UPDATE`/`DELETE` revoked). Balance and reservation are
cached on `wallet` and derived from the ledger:

* balance = TOPUP + WITHDRAWAL_REVERSAL - CAPTURE - WITHDRAWAL
* reserved = RESERVE - RELEASE - CAPTURE

`LedgerReconciliationJob` recomputes both every 10 minutes per country and logs an `ALERT` and increments
`zella.wallet.ledger.discrepancies` for any wallet that disagrees.

## Charging budget

* `POST /v1/sessions/quote` returns limits, available balance, shortfall, fee percentages and an energy estimate.
  Nothing is reserved.
* `POST /v1/sessions` validates the connector (station exists in the customer's country, online, connector
  `Available` or `Preparing`, not in use), the amount range, then reserves atomically. `Idempotency-Key`
  makes a retry return the same session.
* The backend stops the charge. `FundsGuard` runs every 15 s: estimated cost plus the cost of the next
  `stop-lead-time` (45 s since 2026-11-06, covering the sweep, the metering staleness and the stop latency) of charging at the current power, against 100% of the reservation, so the chosen
  amount is consumed. Any energy delivered after the stop command is capped at the reservation, and an unused
  remainder (battery full first) is released back to the wallet. A stop that is not
  confirmed is re-issued every 30 s while the session is `STOPPING`. A session is never marked stopped because a
  command was sent; only the OCPP platform's report moves it.
* A session charging with no known tariff is stopped (`zella.charging.stop.without_tariff`), since the budget
  cannot be enforced.
* Settlement captures `min(cost, reserved)` and releases the rest. A cost above the authorization is recorded in
  `zella.charging.overshoot.minor`; the customer is never charged more than authorized. A finished session with no
  cost charges nothing and is flagged for review.

### Limits of the budget guarantee

The OCPP platform has no charging limit, no auto-stop and no profile API. Metering arrives at the charger's
interval (30-60 s typical, unconfirmed on the real unit) and a stop has latency. The budget is therefore enforced
by prediction and is approximate; the hold is the hard ceiling on what the customer can be charged, and any
overshoot is borne by the operator and measured. Do not describe it as an exact cap.

## Rate-limit budget toward OCPP

The platform allows 60 requests/minute for all customers together. What a sweep (every 15 s) costs now:

* one `GET /api/v1/sessions?status=ACTIVE` for every live session of every customer (`RemoteSessionLookup`),
  shared by the sweeper and by user reads for 5 s;
* one metering call per ACTIVE session, at most every 10 s (`metering-interval`); the battery level shown can never
  be fresher than the charger's own `MeterValueSampleInterval`; the stop prediction (`stop-lead-time`, 45 s) covers a little of the extra staleness;
* one lookup by transaction when a session leaves the active list, once;
* a lookup by idTag only for a session still waiting for its transaction, at most every 20 s (always when it is
  about to time out).

That is about 4 + 2 x sessions requests per minute, so roughly 20 concurrent sessions fit in the 45 requests/minute
that background polling may use. `OcppRequestBudget` enforces this on our side: start/stop commands are never
refused, interactive reads (catalog, QR, station lookup before a start) may use the full 55, background session
polling stops at 45, and an HTTP 429 from the platform pauses background polling for its `Retry-After`.
A throttled or failed lookup leaves the stored session untouched (it never marks a waiting session as timed out),
and `zella.ocpp.polls.throttled` counts the skipped polls.

Beyond ~20 concurrent sessions the platform needs a higher quota or push (webhooks / sync cursor). Webhooks are not
used: their payload shapes are only in the platform's OpenAPI document, which could not be reached while building this.
