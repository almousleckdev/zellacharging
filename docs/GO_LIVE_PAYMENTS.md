# First real payments (staging on Railway)

Goal: move from the sandbox to real Airtel Money / Moov Money collections, safely. Do the steps in order and
do not skip a gate. Nothing here needs new code.

Backend URL used below: `https://apicharging.up.railway.app`.

## Step 1. Run the application as a restricted database role

Today the service connects as the Postgres owner (superuser), so row-level security does not isolate countries.
The log shows `The database role bypasses row-level security`.

1. The migrations already created the group role `zella_app`. Connect to the Railway Postgres as the owner
   (`railway connect Postgres`, or `psql` with the owner URL) and run:

   ```sql
   create role zella_runtime login password '<long random password>' in role zella_app;
   ```

2. On the `backend` service set:

   | Variable | Value |
   |---|---|
   | `DB_USER` | `zella_runtime` |
   | `DB_PASSWORD` | the password above |
   | `DB_MIGRATION_USER` | the owner user (the one used today) |
   | `DB_MIGRATION_PASSWORD` | the owner password |

   Keep `DB_URL` as it is.

3. Delete the override `ZELLA_TENANCY_REQUIRE_RESTRICTED_ROLE` (or `NDZELA_TENANCY_REQUIRE_RESTRICTED_ROLE`) if it
   is set to `false`, then redeploy.

Gate: the service starts, the `bypasses row-level security` warning is gone, and register, login and a
charge-history call still work.
Rollback: set `*_TENANCY_REQUIRE_RESTRICTED_ROLE=false` and put the owner back in `DB_USER`/`DB_PASSWORD`.

## Step 2. Configure PVit

Set on the `backend` service (values come from PVit, never from chat or git):

`PVIT_BASE_URL`, `PVIT_SECRET_PASSWORD`, `PVIT_CODE_URL_SECRET`, `PVIT_CODE_URL_PAYMENT`,
`PVIT_CODE_URL_STATUS`, `PVIT_CODE_URL_KYC`, `PVIT_CALLBACK_URL_CODE`, `PVIT_AGENT`, `PVIT_PRODUCT`,
`PVIT_OWNER_CHARGE`, and `ZELLA_PAYMENT_PROVIDER=pvit`.

Each operator has its own PVit operation account: set `PVIT_COMPTE_OPERATION_AIRTEL` and/or
`PVIT_COMPTE_OPERATION_MOOV`. An operator without an account is reported as unavailable in the app. `PVIT_CODE_URL_PAYOUT`
is only needed once withdrawals are enabled. The KYC lookup never blocks a payment; its answer is logged.

Do not set `ZELLA_PAYMENT_ALLOW_SANDBOX` (or its `NDZELA_` twin). With `pvit` selected and a setting missing, the
service refuses to start, which is the intended safety net.

Register this callback URL with PVit and use the code they return as `PVIT_CALLBACK_URL_CODE`:

```
https://apicharging.up.railway.app/v1/payments/callback
```

The callback carries no trusted data. It only names a reference and triggers a status query, so a forged call
credits nothing.

Gate: the service starts with `pvit` selected.

## Step 3. First real collection (tiny amounts, a number you control)

Minimum top-up is 500 XAF. Use that.

| # | Test | Expected |
|---|---|---|
| 1 | Top up 500 and confirm on your phone | Top-up becomes `SUCCEEDED`, wallet +500 exactly once |
| 2 | Top up 500 and refuse or cancel on your phone | Top-up `FAILED`, wallet unchanged |
| 3 | Top up 500 and do nothing | Stays `PENDING`, then expires; wallet unchanged |
| 4 | Replay the callback for test 1's reference (same POST twice) | No second credit |
| 5 | Send a callback with an unknown reference | Ignored, logged as a warning, nothing credited |
| 6 | Confirm on your phone after the top-up expired (late confirmation) | Credited once, ledger consistent |

Watch the backend logs for `PVit payment <reference> status <STATE>` and, for test 5,
`Payment notification for an unknown reference`.

Check the database after each test:

```sql
select state, amount_minor, reference from topup order by created_at desc limit 5;
select type, amount_minor, idempotency_key from ledger_entry where customer_id = '<id>' order by created_at;
select balance_minor, reserved_minor from wallet where customer_id = '<id>';
```

Gate: exactly one `TOPUP` ledger entry per successful payment, balance equals the sum of entries, and no
`ALERT wallet ... does not match its ledger` after the next ledger check.

## Step 4. Limit the exposure for the first users

The caps are `zella.wallet.min-topup-minor` and `zella.wallet.max-topup-minor` (defaults 500 and 500000 XAF). For
the closed beta lower the maximum with the environment variable `ZELLA_WALLET_MAX_TOPUP_MINOR`, for example `50000`,
and raise it later. Watch the logs daily for lines starting with `ALERT`.

## Not part of this phase

* Withdrawals stay disabled: the PVit payout API is not verified and phone numbers cannot be verified (email-only
  verification).
* Real-charger validation (OCPP key, station ownership, MeterValues interval, auto-stop) is the next phase and is
  independent of payments.
