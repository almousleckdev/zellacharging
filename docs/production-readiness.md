# Production readiness report

Branch `chore/production-readiness`. Status words: **VERIFIED** (implemented and checked by a test or command shown
here), **UNVERIFIED** (implemented, not exercised for real), **BLOCKED** (needs something outside the code),
**NOT DONE**.

## Verdict

**READY FOR STAGING. NOT READY FOR PRODUCTION AT SCALE.**
The financial core was already sound and is now better covered. Before wider production: real push delivery check,
PVit payout verification, release signing, a backup you have restored once, a staging environment, and branch
protection with the CI checks. The platform can continue serving the current controlled beta.

## Starting point (audit)
The repository was more advanced than the brief assumed: refresh-token rotation with reuse detection, bcrypt, lockout,
rate limits, per-country row-level security, an idempotent wallet with locking, a provider-confirmed top-up flow,
withdrawals with reversal and reconciliation, an admin console with TOTP, 340+ tests. The audit therefore
concentrated on real gaps.

| Severity | Finding | Result |
|---|---|---|
| HIGH | Android release manifest had no `INTERNET` permission: a release build cannot reach the API | Fixed |
| HIGH | No push notification system (Firebase only initialized) | Implemented (outbox, FCM sender, mobile client) |
| HIGH | No CI/CD, no dependency or secret scanning | Added (not yet run on GitHub) |
| MEDIUM | Android allowed backups and defaulted cleartext; release signed with debug key and no shrinking | Backups and cleartext off, shrinking on, signing from `key.properties` when present (**BLOCKED**: keystore) |
| MEDIUM | API had no explicit security headers or request size limit; dashboard no CSP/HSTS | Added, with tests |
| MEDIUM | Ledger is single-entry, not double-entry | **NOT DONE**, documented with a migration path (`wallet-and-ledger.md`) |
| MEDIUM | No restore drill, no confirmed backups | **NOT DONE** (`disaster-recovery.md`) |
| LOW | `exceljs` pulls `uuid` with a moderate advisory (unused code path) | Accepted, tracked |
| LOW | Symmetric JWT key; no certificate pinning; CSP allows inline scripts | Accepted, listed in `security.md` |

## Status by item
1. Original architecture and problems: see the table above and `ARCHITECTURE.md`.
2. Vulnerabilities found: listed above; I found no path that allows unauthorized money movement, but this was a targeted review, not a full penetration test.
3. Security improvements: headers, size limit, CSP/HSTS, Android hardening, anonymous-access and IDOR tests. **VERIFIED**.
4. Financial integrity: wallet race and idempotency tests for withdrawals added (`WithdrawalBalanceRaceIntegrationTest`);
   existing top-up, hold, reversal and ledger-check tests re-run. **VERIFIED**. Double-entry: **NOT DONE**.
5. Withdrawals: implemented before this work, extended with notifications. Tested against a scripted gateway. **UNVERIFIED** with PVit.
6. Airtel Money payout: **BLOCKED** until PVit confirms `GIVE_CHANGE` for the Airtel account.
7. Moov Money payout: **BLOCKED** until PVit confirms `GIVE_CHANGE` for the Moov account.
8. Android notifications: code and channel done, backend tested. **UNVERIFIED** on a device; **BLOCKED** on the Firebase service account.
9. iOS notifications: entitlement and background mode present, code shared with Android. **UNVERIFIED**; **BLOCKED** on the APNs key in Firebase.
10. CI/CD: five workflows and Dependabot written. **UNVERIFIED** (first GitHub run pending). Locally equivalent commands pass.
11. Tests: see results below.
12. Performance: no profiling pass was done; N+1 and index review **NOT DONE** beyond the notes in `database.md`.
13. Documentation: README plus `security`, `payment-architecture`, `wallet-and-ledger`, `wallet-withdrawals`,
    `notifications`, `database`, `ci-cd`, `disaster-recovery`, and updates to `ARCHITECTURE` and `DEPLOYMENT`.
14. Flutter architecture refactor and Next.js redesign: **NOT DONE** beyond the changes above; the code already follows the
    feature-first structure and I did not refactor working code without a defect to fix.

## Test results (this branch)
```
cd backend   && ./mvnw -B verify                                  -> 365 tests, 0 failures, BUILD SUCCESS
cd mobile    && flutter analyze --no-fatal-infos                  -> no errors (3 deprecation infos, see notifications.md)
cd mobile    && flutter test                                       -> 603 passed
cd dashboard && pnpm lint && pnpm typecheck && pnpm test          -> clean, 149 passed
cd dashboard && ZELLA_API_URL=https://api.example.invalid pnpm build -> succeeds
```

Brief's 12 financial scenarios: simultaneous withdrawals (`WithdrawalBalanceRaceIntegrationTest`), duplicate payment
callbacks (`aConfirmedCallbackCreditsTheWalletExactlyOnce`), duplicate payout callbacks (payouts are reconciled by status pull, covered by
`aFailureConfirmedLaterIsReversedExactlyOnce`), payment timeout (`anUnknownOutcomeOfTheCollectionKeepsThePaymentPending...`),
payout timeout (`anUnknownOutcomeKeepsTheFundsHeldAndGoesToManualReview`), invalid signature (not applicable: the callback
carries no authority, `aCallbackAloneNeverCreditsWithoutTheProviderConfirming`), insufficient balance, failed payout and
refund, duplicate deposit requests, interruption (restart recovery by reconciliation jobs), unauthorized wallet access.
Database failure during a financial operation relies on single-transaction atomicity; no fault-injection test exists.

## Remaining risks, in order
1. Withdrawals never run against real PVit payouts (money-out path).
2. Push never delivered to a real phone; FCM and APNs credentials missing.
3. No restore drill, backup retention unknown.
4. Release APK/AAB still signed with the debug key until a keystore is provided.
5. Single environment; no staging; no branch protection.
6. Single-entry ledger; no automated reconciliation against the PVit balance.
7. CI never executed; CSP not checked in a browser across every dashboard page.
8. No load test; no query-plan review.

## External actions needed from you
Create the Firebase service account and set `ZELLA_FCM_CREDENTIALS_JSON`; upload the APNs key; create the Android upload
keystore and `key.properties`; ask PVit about `GIVE_CHANGE` for both operators; enable branch protection and required checks;
confirm Railway backups; deploy backend migrations V21, V22, V23 before shipping the new app.
