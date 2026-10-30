# Payment architecture

Collections (top-ups) and payouts (withdrawals) go through PVit, which fronts Airtel Money and Moov Money in Gabon.
Mali is not integrated.

## Principle
The backend never trusts a success claim from the mobile app or from an incoming request. A wallet is credited only
after the backend itself asks PVit for the status of that exact reference and receives a confirmation.

## Top-up flow
1. App calls `POST /v1/wallet/topups` with an `Idempotency-Key`, operator, number and amount (bounds from `zella.wallet`).
2. Backend stores the top-up `PENDING` with a reference that encodes the country (`TopupReference`), then asks PVit to
   collect. A second top-up while one is unsettled is refused (`TOPUP_PENDING`).
3. PVit calls `POST /v1/payments/callback`. This endpoint is public (the provider cannot send a token) and rate limited.
   **It carries no authority**: it only names a merchant reference. `TopupSettlement.settleByReference` finds the top-up,
   asks PVit for its status, and settles from the answer. A forged callback for an unpaid reference changes nothing
   (test: `aCallbackAloneNeverCreditsWithoutTheProviderConfirming`).
4. `TopupReconciliationJob` re-checks every unsettled top-up on a schedule, so a lost callback is harmless.
5. On `SUCCEEDED` the wallet is credited in one transaction with a ledger entry and an audit record.

## Validations on the settlement path
- Amount: the credit is `min(confirmed, requested)`; a smaller confirmation credits only what was confirmed and logs an error.
- Currency: single currency per country from settings; the wallet row stores currency and decimals.
- Idempotency: unique `(customer_id, idempotency_key)`; state transitions are compare-and-set updates
  (`PENDING -> SUCCEEDED`), so duplicate callbacks and concurrent settlements credit once.
- Replay: a replayed callback finds a settled top-up and returns immediately.
- Unknown or foreign-country references are ignored without error (no probing signal).
- Timeouts: an unknown provider answer keeps the top-up `PENDING`; after 30 minutes it becomes `EXPIRED` but is still
  re-checked for six hours, and a late confirmation still credits.

## Provider signature
PVit's callback specification does not provide a signature that this integration can verify, which is why the design
pulls status instead of trusting the callback body. If PVit documents a signature or a fixed source IP range, add it as an
extra filter in front of the endpoint; it would not replace the status pull.

## States
Top-up: `PENDING`, `SUCCEEDED`, `FAILED`, `EXPIRED`. Withdrawal: see [wallet-withdrawals.md](wallet-withdrawals.md).

## Tests
`TopupSettlementIntegrationTest` (12 cases: confirmed once, forged callback, unknown reference, other country, timeout,
duplicate key, partial amount, declined, expiry then late credit, foreign read, one pending at a time),
`WalletFlowIntegrationTest`, `PvitGatewayTest`, `PvitPayoutGatewayTest`.

## Open items
Fee display before confirmation (PVit fees API not used), callback source allow-list, KYC enforcement.
