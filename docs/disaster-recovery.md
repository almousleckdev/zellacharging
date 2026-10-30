# Disaster recovery

What exists: the database is the only state; the schedulers rebuild everything else on boot (see `OPERATIONS_RUNBOOK.md`).

## Backups
- Take a logical backup before any risky change:
  `pg_dump --format=custom --no-owner "$DATABASE_PUBLIC_URL" > zella-$(date +%F).dump`
  (the public URL of the Railway Postgres service, used from a trusted machine).
- Restore into a **new** database first and inspect it: `createdb zella_restore && pg_restore --no-owner -d zella_restore file.dump`.
- Keep dumps off the laptop after use; they hold customer data.
- **Not verified:** Railway's own scheduled backups and point-in-time recovery depend on the plan. Confirm in the Railway
  dashboard and write the retention here. Until confirmed, assume there is no automatic backup.

## Scenarios
| Event | Action |
|---|---|
| Bad deploy | Redeploy the previous image in Railway (migrations are additive, so the old version runs on the new schema) |
| Wrong data change | Stop writes if needed, restore a dump into a side database, copy the rows back with explicit SQL; never edit balances, add compensating ledger entries |
| Database lost | Restore the latest dump into a new Postgres, run the app (Flyway verifies the schema), point `DB_URL` at it, then run the ledger check and the top-up and withdrawal reconciliation jobs |
| Provider outage | Nothing to restore; reconciliation settles after recovery |
| Leaked secret | Rotate it in Railway and the provider, redeploy; rotate `ZELLA_JWT_SECRET` to sign everyone out |

## Targets (proposed, not yet rehearsed)
RPO 24 hours with daily dumps (lower with provider PITR), RTO 2 hours. A restore drill has **not** been performed.
