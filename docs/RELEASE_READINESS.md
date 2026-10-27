# Release readiness (first release, Gabon)

Status of each acceptance criterion, with what is verified and what is not. "Verified" means covered by an
automated test that passed in this repository; nothing here has been run against real PVit or a real charger.

| Criterion | State |
|---|---|
| Airtel Money and Moov Money through a verified integration | **Partly verified.** Against the live PVit API, with the real credentials: authentication (`renew-secret`) returns the expected `secret` and `expires_in`; the status endpoint answers a well-formed unknown reference with 404 `TRANSACTION_NOT_FOUND`, and rejects references over 30 characters (this was a real bug, fixed). **Not yet done:** a real collection and its confirmation, the callback URL registered with PVit, XAF behaviour. |
| Top up, spend | Verified (unit, integration, concurrency of reservation by row lock). |
| Withdraw | **Not available.** State machine and safeguards are implemented and tested with a scripted provider; no payout API is verified, so it is disabled. |
| Charging cannot start before payment confirmed | Verified: reservation requires credited balance; callbacks alone credit nothing. |
| Correct connector identified | Backend refuses a start unless the station is in the customer's country, online, and the connector is `Available` or `Preparing` (cable plugged in) and not in use. The app watches the connector status every 5 s, selects a connector that becomes `Preparing` on its own, and asks the customer when several do. Verified with unit and widget tests; **not verified against a real charger's status reports**. |
| Auto-stop at budget | Implemented with prediction and tested with unit and integration tests. **Not verified on a real charger**; accuracy depends on the MeterValues interval. |
| Final cost reconciles | Verified for capture/release, overshoot, no-cost sessions; ledger recomputed by a job. |
| No double charging from duplicates, delays, failures | Verified for top-up callbacks, retries, expiry and late confirmation, start retries, withdrawal retries. |
| Errors central, FR/EN | Verified on the backend (codes, localized detail, correlation id) and mobile (code-keyed texts, uncertain vs failed). |
| Country isolation | Verified at database level (row-level security, composite keys) and API level. Requires a restricted database role in production. |
| Automated tests and recovery docs | Backend 290, mobile 516 tests; see `OPERATIONS_RUNBOOK.md`. |

## Known gaps before production

1. One real PVit collection (a small amount, a number you control), callback registration; PVit payout API if withdrawals are wanted.
2. OCPP quota: about 20 concurrent sessions fit in 60 req/min after batching and throttling. More needs a larger quota or webhooks / the sync cursor.
3. Real-charger validation of connector numbering, MeterValues interval, stop latency.
4. Access tokens are HS256 with one shared secret, no key rotation, and are not revoked: a blocked customer keeps
   access up to 15 minutes. Move to asymmetric keys with `kid` when a rotation process exists.
5. Rate limits are shared through Redis when `ZELLA_RATE_LIMIT_STORE=redis` and `REDIS_URL` are set (add the Redis plugin first). Without them they stay in memory per instance. If Redis fails the service falls back to the in-memory limiter for ten seconds at a time.
6. Email-only sign-up resolves to the default country. Mali needs an explicit country at sign-up.
7. Cross-country platform administration and its audit are not built.
8. The database role must be created and the app pointed at it before the next deploy (`TENANCY.md`).
9. `application.yml` environment variables were renamed from `NDZELA_*` to `ZELLA_*`; update the deployed service.
10. Mobile: the withdrawal screen is not built (withdrawals are disabled on the server). The charge and top-up screens were redesigned, use the backend quote, show real payment progress, and treat an unconfirmed payment as "being verified" rather than failed.
11. **Verification is by email only.** Registration requires an email and the 6-digit code is sent to it (localized French or English from the request language). The phone number is optional, stored normalized and never verified; `verify` and `resend` refuse the `PHONE` channel (`CHANNEL_NOT_SUPPORTED`). Consequences: withdrawals need a verified phone (`PHONE_NOT_VERIFIED`) and stay unavailable until phone verification exists, and a phone number is not proof of ownership. Phone-only accounts created before this change cannot be verified; they exist only in dev and staging.
12. Changing a password signs the customer out of every device (all refresh tokens are revoked) and the app returns to the login screen with an explanatory message.
