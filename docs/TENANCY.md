# Tenancy: country isolation

Zella Charging is multi-country. The tenant is the country (`GA` first, `ML` later). A customer
belongs to exactly one country for life.

## Model

Shared database, shared schema. Every customer-owned table has `country_code char(2) not null`
with a composite foreign key `(customer_id, country_code) -> customer (id, country_code)`, so a child
row can never point at a customer of another country. Nothing is filtered in the application only.

Row-Level Security is the enforcement layer. Each tenant table has `ENABLE` + `FORCE ROW LEVEL SECURITY`
and a `tenant_isolation` policy on `current_setting('app.country', true)` for both `USING` and
`WITH CHECK`. The column default is `current_setting('app.country')`, so inserts inherit the tenant and
fail closed when none is bound.

## How the tenant is bound

| Path | Source of the country |
|---|---|
| Authenticated API | `cty` claim of the access token, signed by us. `TenantFilter` rejects tokens with no active country (401). |
| Register / login / verify | Derived server-side from the phone number region. Email-only identifiers use `zella.tenancy.default-country`. |
| Refresh / logout | Prefix of the opaque refresh token (`GA.xxxx`), then validated by the token hash lookup inside that tenant. |
| Background jobs | One run per active country (`SessionSweeper`). |

`TenantDataSource` wraps the pool and runs `set_config('app.country', ?, false)` on every connection
checkout, using an empty value when no tenant is bound. A stale value can never leak between requests.

## Operating requirements

The runtime database role must NOT be a superuser and must not have `BYPASSRLS`; superusers skip RLS.
`TenantIsolationGuard` refuses to start otherwise (`zella.tenancy.require-restricted-role`, disabled in
the `dev` profile only).

```sql
create role zella_runtime login password '<secret>' in role zella_app;
```

The migration creates `zella_app` and grants it DML on every table, except `UPDATE`/`DELETE` on
`ledger_entry`, which makes the ledger append-only for the application. Migrations run through
`DB_MIGRATION_USER` / `DB_MIGRATION_PASSWORD` (the owner); the application uses `DB_USER` / `DB_PASSWORD`.

## Decisions and trade-offs

* Shared schema over database-per-country: one migration path, one pool, simple operations. If a legal
  requirement for physical separation appears, every table already carries `country_code`, so a split
  by country is a data move, not a redesign.
* The same email or phone may exist in two countries (unique per country). Accounts are separate.
* Email-only sign-up resolves to the default country. This is correct while Gabon is the only active
  country. Before Mali is activated, sign-up must carry an explicit, validated country.
* Platform administrators across countries are not implemented. When added they need an explicit role,
  a dedicated database role and an audit record per access.
* Existing rows were backfilled to `GA`. Tokens issued before this change have no `cty` claim and are
  rejected, so existing sessions must sign in again.
