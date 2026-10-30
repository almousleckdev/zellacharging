# Wallet withdrawals

**Status: implemented and tested against a scripted gateway. Not verified against the real PVit payout API. Disabled by
default (`ZELLA_WITHDRAWALS_ENABLED=false`).**

## Rules
- Money only goes to a number that already topped up this wallet with the same operator. The customer types the
  number (or picks one of the known numbers); any other number is refused with `PAYOUT_NUMBER_NOT_ALLOWED` and nothing
  moves. `GET /v1/wallet` lists the eligible numbers (`payoutNumbers`). An older app that sends no number still falls
  back to the last successful top-up (`NO_PAYOUT_NUMBER` if there is none). This keeps the destination tied to a number
  that already paid, which is the main laundering and account-takeover control.
- Every withdrawal waits for an admin to approve it. Nothing reaches the provider before that.
- The customer re-enters their password for every withdrawal; the key `Idempotency-Key` is required.
- Bounds: minimum 500, maximum 200000 per request, 500000 per day per customer (all settings).
- Available balance excludes money reserved for a charge.

## Flow and states
`PENDING_APPROVAL` -> `REQUESTED` -> `PROCESSING` -> `PAID`, or `FAILED` (reversed or declined), or `REVIEW` (outcome unknown).
1. In one transaction: lock wallet, check available, check the daily limit, insert the withdrawal, debit the wallet, write a
   `WITHDRAWAL` ledger entry, record audit, queue a notification.
2. The request stops in `PENDING_APPROVAL` and the customer is told it awaits approval. The funds are already held, so
   the balance cannot be spent twice.
3. Approval (below) moves it to `REQUESTED` and then submits to the provider. Accepted or pending -> `PROCESSING`. Refused -> reversal. An exception (timeout)
   leaves the funds held and `REQUESTED`; it is never treated as a failure.
4. `WithdrawalReconciliationJob` and `GET /v1/wallet/withdrawals/{id}` ask the provider for the status of the reference.
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

## Admin approval
- Permission `WITHDRAWALS_REVIEW` (migration V25 grants it to every `ADMIN` and `SUPER_ADMIN`). Dashboard page
  "Retraits à valider" lists `WITHDRAWAL` payments in `PENDING_APPROVAL`.
- `POST /v1/admin/withdrawals/{country}/{id}/approve` (optional note): compare-and-set `PENDING_APPROVAL` -> `REQUESTED`,
  audit `WITHDRAWAL_APPROVED`, notify the customer, then submit the payout. If no payout gateway is configured the
  request is refused with `WITHDRAWALS_UNAVAILABLE` and the withdrawal stays pending.
- `POST /v1/admin/withdrawals/{country}/{id}/reject` (reason required): `PENDING_APPROVAL` -> `FAILED` (`REJECTED`),
  the wallet is credited back once (`WITHDRAWAL_REVERSAL`), audit `WITHDRAWAL_REJECTED`, the customer is notified.
- A second decision on the same withdrawal returns `WITHDRAWAL_NOT_PENDING`. The reviewer and time are stored on the row.
- The reconciliation job ignores `PENDING_APPROVAL`: there is nothing to ask the provider yet.
