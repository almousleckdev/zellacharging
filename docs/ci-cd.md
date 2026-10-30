# CI/CD

GitHub Actions in `.github/workflows`:

| Workflow | Triggers | Steps |
|---|---|---|
| `backend.yml` | PR and push to `main` touching `backend/` | Java 21, `./mvnw verify` (compile, unit and Testcontainers tests, packaging) |
| `mobile.yml` | PR and push touching `mobile/` | format check, `flutter analyze --no-fatal-infos`, tests, Android release APK, iOS release build without signing |
| `dashboard.yml` | PR and push touching `dashboard/` | frozen install, lint, typecheck, tests, production build, `pnpm audit --prod --audit-level high` |
| `codeql.yml` | PR, push, weekly | CodeQL for Java/Kotlin and JavaScript/TypeScript |
| `secrets-and-dependencies.yml` | PR, push | gitleaks; dependency review (fails on high severity) on PRs |
| Dependabot | weekly | Maven, pub, npm, GitHub Actions |

Rules: workflows have read-only permissions unless needed, use no production secrets, and never deploy. Deployment
stays a deliberate manual step (`railway up`) until staging and rollback are rehearsed.

Not covered: Java formatting (no formatter is configured; adding Spotless would reformat the code base), integration
tests against PVit (no sandbox credentials), iOS signing and store upload, Android signing (needs the keystore as a secret).
The workflows were written and checked for syntax but **have not yet run on GitHub**; the first run may need small fixes.

Branches: work on `feature/*` or `chore/*`, open a pull request, require the checks above before merging to `main`
(configure branch protection in GitHub settings).
