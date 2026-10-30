# Wallet withdrawals

**Status: implemented and tested against a scripted gateway. Not verified against the real PVit payout API. Disabled by
default (`ZELLA_WITHDRAWALS_ENABLED=false`).**

## Rules
- Money only goes back to the number of the customer's last successful top-up with the same operator
  (`NO_PAYOUT_NUMBER` otherwise). This ties withdrawals to a number that already paid and removes the free-form
  destination, which is the main laundering and account-takeover risk.
- The customer re-enters their password for every withdrawal; the key `Idempotency-Key` is required.
- Bounds: minimum 1000, maximum 200000 per request, 500000 per day per customer (all settings).
- Available balance excludes money reserved for a charge.

## Flow and states
`REQUESTED` -> `PROCESSING` -> `PAID`, or `FAILED` (reversed), or `REVIEW` (outcome unknown).
1. In one transaction: lock wallet, check available, check the daily limit, insert the withdrawal, debit the wallet, write a
   `WITHDRAWAL` ledger entry, record audit, queue a notification.
2. After commit: submit to the provider. Accepted or pending -> `PROCESSING`. Refused -> reversal. An exception (timeout)
   leaves the funds held and `REQUESTED`; it is never treated as a failure.
3. `WithdrawalReconciliationJob` and `GET /v1/wallet/withdrawals/{id}` ask the provider for the status of the reference.
   `PAID` ends it. `FAILED` triggers the reversal exactly once (compare-and-set on the state, a unique
   `withdrawal-reversal:<id>` ledger key). Still unknown after `review-after` (2 h) -> `REVIEW`, an `ALERT` log, manual decision.
4. The customer is notified when the withdrawal starts, succeeds or fails. The dashboard lists payments and withdrawals.

`CANCELLED` and `REVERSED` from the brief are not separate states: a failed payout is `FAILED` with a reversal ledger
entry, and the customer cannot cancel once submitted.

## Provider support
`PvitPayoutGateway` uses PVit's `GIVE_CHANGE` operation. Whether it is enabled for the Airtel and the Moov merchant
accounts, and whether it needs its own code (`PVIT_CODE_URL_PAYOUT`), must be confirmed with PVit. Until then: Airtel
Money payout **not verified**, Moov Money payout **not verified**. Test with 1000 XAF to your own number before enabling.

## Tests
`WithdrawalIntegrationTest` (11 cases including refused payout, late failure reversed once, unknown outcome to review,
same key pays once, limits, foreign read), `WithdrawalDailyLimitIntegrationTest`, `WithdrawalBalanceRaceIntegrationTest`,
`PvitPayoutGatewayTest`.
