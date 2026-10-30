# Wallet and ledger

## Model
- `wallet(customer_id, currency, decimals, balance_minor, reserved_minor)` with `check (balance >= 0 and reserved >= 0 and
  reserved <= balance)`. Available money is `balance - reserved`. Amounts are `bigint` minor units; XAF has 0 decimals.
- `ledger_entry` is the append-only journal: `TOPUP`, `RESERVE`, `RELEASE`, `CAPTURE`, `WITHDRAWAL`,
  `WITHDRAWAL_REVERSAL`, each with a unique `idempotency_key` such as `topup:<id>` or `withdrawal-reversal:<id>`.
  The runtime role has no `UPDATE` or `DELETE` on this table (migration V9); the code never issues either.
- `funding_hold` reserves money for a charge (`OPEN`, `SETTLED`, `RELEASED`, `REVIEW`). A charge captures the real cost plus
  the service fee and releases the rest.
- Clients cannot change balances: no endpoint accepts a balance; every movement is created by the backend from a settled
  top-up, a charge settlement or a withdrawal.

## Concurrency
Every balance change locks the wallet row (`select ... for update`) inside one transaction that also writes the ledger
entry and the audit record. Tests: parallel charge starts cannot spend a balance twice
(`WalletConcurrencyIntegrationTest`), parallel withdrawals cannot exceed the balance or the daily maximum
(`WithdrawalBalanceRaceIntegrationTest`, `WithdrawalDailyLimitIntegrationTest`).

## Integrity check
`LedgerReconciliationJob` recomputes each wallet from its ledger entries on a schedule and logs
`ALERT wallet ... does not match its ledger` on any difference (test: `theLedgerCheckFlagsAWalletThatNoLongerMatchesItsEntries`).
Never fix a balance by editing it: add a compensating entry (see `OPERATIONS_RUNBOOK.md`).

## Double entry: not implemented
The ledger is a typed single-entry journal per customer wallet. Platform revenue (commissions, electricity) is derived from
settled holds by queries, not stored as the opposite side of each movement. A full double-entry ledger (customer
liability account against provider cash, revenue and fee accounts) would let the books balance by construction and is
the recommended next financial-engineering step, as its own migration and backfill project. It was not attempted in
this pass because it changes the meaning of every historical row of a live system.

## Audit
`audit_record` holds actor, action, subject and correlation id; the runtime role cannot update or delete it.
