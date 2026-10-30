# Zella Charging

Customer platform for electric-vehicle charging in Gabon: a Flutter mobile app, a Spring Boot customer backend and a Next.js
admin dashboard. Customers top up a wallet with Airtel Money or Moov Money (through the PVit aggregator), start and stop
charges on stations managed by a separate OCPP platform, and can withdraw their balance back to mobile money.

The OCPP platform is a different project owned by another developer and is only reached through its REST API.

## Architecture

```
Flutter app ──┐                       ┌── PVit (Airtel Money, Moov Money)
              ├── Customer backend ───┤
Next.js admin ┘   Spring Boot 4       ├── OCPP platform (stations, sessions)
(BFF + cookies)   PostgreSQL 16       ├── Firebase Cloud Messaging
                  Redis (rate limits) └── SMTP (verification and reset codes)
```

| Directory | What | Stack |
|---|---|---|
| `backend/` | Customer API, wallet, ledger, payments, push, admin API | Java 21, Spring Boot 4.1, Spring Modulith, PostgreSQL 16, Flyway |
| `mobile/` | Customer app, Android and iOS | Flutter, Riverpod, go_router, dio |
| `dashboard/` | Admin console | Next.js 16, TypeScript, Tailwind CSS |
| `docs/` | Design, operations and security documentation | Markdown |
| `deploy/` | Staging compose and Caddy files | Docker |

Start with [docs/architecture.md](docs/ARCHITECTURE.md) and [docs/production-readiness.md](docs/production-readiness.md).

## Local development

Prerequisites: Java 21, Docker, Flutter (stable), Node 22 with pnpm.

```bash
docker compose up -d
cd backend && SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
cd dashboard && cp .env.example .env && pnpm install && pnpm dev
cd mobile && flutter pub get && flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8090
```

PostgreSQL listens on 5434, Redis on 6379, Maildev on http://localhost:1080 (verification and reset codes arrive there).
The backend serves on 8090, the dashboard on 3000. Phone codes are written to the backend log while SMS is not connected.

## Configuration

Everything is read from environment variables. Real values never go in Git; `.env.example` files list the names.

| Variable | Purpose |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD`, `DB_MIGRATION_USER`, `DB_MIGRATION_PASSWORD` | PostgreSQL. The runtime role must be the restricted `zella_app` member, migrations use the owner. |
| `ZELLA_JWT_SECRET`, `ZELLA_CODE_PEPPER`, `ZELLA_ADMIN_TOTP_KEY` | Token signing, hashing pepper, encryption key for admin authenticator secrets |
| `ZELLA_ADMIN_EMAIL`, `ZELLA_ADMIN_PASSWORD` | One-time bootstrap of the first super admin; remove the password afterwards |
| `ZELLA_OCPP_BASE_URL`, `ZELLA_OCPP_API_KEY`, `ZELLA_OCPP_SIGNING_SECRET` | OCPP platform access |
| `PVIT_*` (see `docs/GO_LIVE_PAYMENTS.md`) | Payment provider credentials and per-operator codes |
| `ZELLA_WITHDRAWALS_ENABLED` | Withdrawals are off until the provider payout is verified in production |
| `ZELLA_FCM_CREDENTIALS_JSON` | Firebase service account JSON; push is inactive while empty |
| `ZELLA_REPORTING_START` | Admin figures only count operations after this instant |
| `ZELLA_RATE_LIMIT_STORE`, `REDIS_URL` | `redis` shares limits between instances |
| `ZELLA_API_URL` | Dashboard: the backend base URL (server side only) |
| `API_BASE_URL` | Mobile: `--dart-define` for the backend base URL |

## Firebase

Android: `mobile/android/app/google-services.json`. iOS: `mobile/ios/Runner/GoogleService-Info.plist`, the push
entitlement is in `Runner.entitlements` and remote notifications are enabled in `Info.plist`. These client files contain
public identifiers, not secrets. The server credential (`ZELLA_FCM_CREDENTIALS_JSON`) is a secret and lives only in
the deployment environment. The APNs authentication key must be uploaded once in the Firebase console
(Project settings, Cloud Messaging). Details: [docs/notifications.md](docs/notifications.md).

## Tests and quality

```bash
cd backend && ./mvnw verify                          # unit and Testcontainers integration tests
cd mobile && dart format --set-exit-if-changed lib test && flutter analyze --no-fatal-infos && flutter test
cd dashboard && pnpm lint && pnpm typecheck && pnpm test && pnpm build
```

GitHub Actions run the same commands on every pull request, plus CodeQL, secret scanning and dependency review
([docs/ci-cd.md](docs/ci-cd.md)).

## Production builds and deployment

The backend is a Docker image (`backend/Dockerfile`) deployed on Railway with `backend/railway.json` (health check on
`/actuator/health`). Flyway migrations run at startup. The dashboard is a Next.js server build. Mobile builds are
`flutter build appbundle --release` and `flutter build ipa`. Android release signing reads `mobile/android/key.properties`
(not in Git). See [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) and [docs/disaster-recovery.md](docs/disaster-recovery.md).

## Security

Short version: short-lived JWT access tokens, rotating refresh tokens with reuse detection, bcrypt, progressive lockout,
per-route rate limits, row-level security per country, server-side money in integer minor units, wallet credits only
after the provider confirms, idempotency keys on every money operation. Full detail and known gaps:
[docs/security.md](docs/security.md).

## Troubleshooting

| Symptom | Check |
|---|---|
| Backend refuses to start | A required secret is missing (`ZELLA_JWT_SECRET`, `ZELLA_CODE_PEPPER`, `ZELLA_ADMIN_TOTP_KEY`) |
| Migration fails on Railway | The owner credentials in `DB_MIGRATION_*`; never edit an applied migration |
| Top-up stays pending | Provider status is pulled by `TopupReconciliationJob`; see `docs/OPERATIONS_RUNBOOK.md` |
| No push received | `ZELLA_FCM_CREDENTIALS_JSON` set, device registered (`device_token`), outbox rows (`notification_outbox`) |
| Release Android app cannot reach the API | Build with the main manifest (`INTERNET` is declared there) |
