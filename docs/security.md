# Security

State as of this branch. Status words: **implemented** (code and test), **configured** (setting only), **gap** (not done).

## Authentication
- Passwords: bcrypt cost 12. **implemented**
- Access tokens: HS256 JWT, 15 minutes (admin 10 minutes). Refresh tokens: random, stored as SHA-256, 30 days (admin 12 hours),
  rotated on every use; presenting a used token revokes the whole token family (reuse detection). **implemented**
- Brute force: five failed logins lock the account for 15 minutes; login, register, verify, resend, forgot and reset
  are rate limited per IP; top-ups, charge starts and withdrawals per customer. Redis shares the counters between
  instances and falls back to memory if Redis is down. **implemented**
- Password reset: six-digit code, five minutes, hashed with a pepper and a per-purpose stream so a verification code
  cannot reset a password; the response never reveals whether an account exists; all refresh tokens are revoked after a
  reset or a password change. **implemented**
- Admin: invitation, mandatory authenticator (TOTP) at login, secrets encrypted with AES-GCM. **implemented**
- Gap: access tokens use a symmetric key (HS256). Moving to an asymmetric key allows key rotation without a shared secret.

## Authorization
- Every customer endpoint derives the customer from the token, never from a request field; reads by id filter on the
  owner and answer 404 for foreign ids (tests: `ApiProtectionIntegrationTest`, `TopupSettlementIntegrationTest`,
  `WithdrawalIntegrationTest`). **implemented**
- Country isolation: PostgreSQL row-level security on every tenant table; the runtime role cannot bypass it; the
  application refuses to start with an unrestricted role when `require-restricted-role` is on. **implemented**
- Admin permissions and country grants are checked in the backend; the dashboard only hides what the API refuses.
  **implemented**

## Transport and headers
- API: `X-Content-Type-Options`, `X-Frame-Options: DENY`, `Referrer-Policy: no-referrer`, `Content-Security-Policy:
  default-src 'none'`, `Strict-Transport-Security`, `Permissions-Policy`, `Cache-Control: no-store`. **implemented**
- Dashboard: CSP, HSTS, frame denial, nosniff, referrer and permissions policy. The CSP allows inline scripts because
  Next.js needs them without a nonce setup. **implemented, gap: nonce-based CSP**
- Request bodies above 2 MB are refused with 413 before being read (declared length only; chunked uploads rely on the
  servlet container). **implemented**
- CORS: no cross-origin access is enabled; the mobile app is not a browser and the dashboard calls the backend from its
  server. **implemented**
- CSRF: disabled on the API because it uses bearer tokens and no cookies. The dashboard session cookies are `HttpOnly` and
  `SameSite=Strict`. **implemented**

## Input and errors
- Request records are validated with Jakarta Validation; money is `long` minor units; SQL uses bound parameters only.
- Errors are RFC 7807 with a stable `code` and a correlation id; stack traces are never returned. **implemented**
- Logs carry the correlation id. Verification and reset codes are only sent by email and never logged. **implemented**

## Secrets
- No credential is committed. `.env` files are ignored; `.env.example` has placeholders. The Firebase client files are
  public identifiers; restrict the API keys by app id in the Google Cloud console. **configured**
- Provider credentials (PVit) are read from the environment at runtime. **implemented**
- CI runs gitleaks on every pull request. **implemented**
- Gap: the Git history was not scanned for old leaks in this pass.

## Mobile
- Tokens are in the platform secure store (Keychain, Android Keystore through `flutter_secure_storage`). **implemented**
- Android: backups disabled, cleartext traffic disabled, only the launcher activity is exported, release builds shrink
  code. **implemented**
- Gap: release signing still falls back to the debug key until `key.properties` exists. Certificate pinning is not
  implemented. No jailbreak/root detection.

## Dependencies and automation
- Dependabot (Maven, pub, npm, Actions), CodeQL (Java, TypeScript), dependency review on pull requests, `pnpm audit`.
- Known: `exceljs` depends on a `uuid` version with a moderate advisory (buffer argument of v3/v5/v6, not used here).

## Not done
- Penetration test, dynamic scanning, WAF, centralized log retention, formal threat model.
