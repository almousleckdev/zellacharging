# Database

PostgreSQL 16, schema managed by Flyway (`backend/src/main/resources/db/migration`, V1 to V23). A migration is never
edited after it has been applied anywhere; fixes are new migrations.

## Roles and tenancy
- The migration user owns the schema. The application connects as a member of `zella_app`, which has
  `select/insert/update/delete` on tables but no `update/delete` on `ledger_entry` and `audit_record`, and is subject to
  row-level security.
- Each tenant table has `country_code` and a `tenant_isolation` policy comparing it with `app.country`, set per
  connection by the tenancy layer. Foreign keys are `(id, country_code)` pairs, so a row cannot point at another
  country's customer. Details: `TENANCY.md`.

## Integrity
- Check constraints on money (`amount_minor > 0`, `balance_minor >= 0`, `reserved_minor <= balance_minor`) and on state values.
- Unique constraints for idempotency: `(customer_id, idempotency_key)` on top-ups and withdrawals, `idempotency_key` on
  ledger entries, `reference` on payments and payouts, `(customer_id, dedupe_key)` on notifications.
- Partial indexes for the work queues: unsettled top-ups, unsettled withdrawals, open holds, pending notifications.

## Sensitive data
- Passwords: bcrypt. Verification and reset codes: hashed with a pepper. Refresh tokens: SHA-256. Admin TOTP secrets:
  AES-GCM. No provider secret and no card or mobile-money credential is stored. Phone numbers and emails are stored in
  clear because the product needs them.
- Gap: no column-level encryption for phone numbers, no pgaudit.

## Performance notes
Connection pool 10 (`DB_POOL_MAX`) with virtual threads. Lists are paginated (admin default 10, `limit` on customer
histories). Query plans were not profiled with production-sized data in this pass; measure with `EXPLAIN (ANALYZE, BUFFERS)`
on a restored copy before large growth. No Redis cache beyond rate limits.

## Migrations in production
Run at application start. Before a migration that rewrites a large table, take a backup (`disaster-recovery.md`) and
deploy in a quiet period. Prefer additive changes (new column, then backfill, then constraint) so the previous version
keeps working during a rollback.
