# CLAUDE.md — Ndzela (customer platform)

Mobile app (Flutter, `mobile/`) and customer backend (Spring Boot Modulith, `backend/`) for EV charging.
The OCPP platform in `../backend` belongs to another developer: never modify it.

## Absolute rules (backend and Flutter)

1. **NEVER commit to git.** No `git commit`, no `git push`, no branches. The user does it.
2. **No comments in code.** No Javadoc, no `//`, no `/* */`, no commented-out code. Names carry the meaning.
3. **No long files.** Keep every file under ~150 lines, one clear responsibility, one public type per file.
4. **No duplicated code.** Extract and reuse; never copy logic between files, features or layers.
5. **Clean architecture.** Dependencies point inward: `web -> application -> domain`; `infrastructure` implements
   application ports. Domain has no framework imports. Business logic never lives in controllers or widgets.
6. Never claim a feature works without running it (tests, curl, simulator).
7. **NEVER use random colors.** Every color comes from the `AppColors` palette in `mobile/lib/core/theme/app_colors.dart`
   (Orange-500 brand, neutrals, success/danger, and the charge greens). No inline `Color(0x...)`, no `Colors.*` shades
   (only `AppColors.onPrimary`/`AppColors.surface` for white), no per-screen accent colors, no dark "black theme" cards
   inside the app. A new color is added to `AppColors` first, with a purpose, or it is not used.
8. **NEVER use an iOS widget on Android or an Android widget on iOS.** Use Flutter's adaptive widgets so each platform gets
   its own native look: `showAdaptiveDialog` / `AlertDialog.adaptive`, `Switch.adaptive`, `Checkbox.adaptive`,
   `CircularProgressIndicator.adaptive`, `Slider.adaptive`, platform page transitions. A `Cupertino*` widget is only
   allowed behind a platform check with its Material counterpart for Android.
9. **Languages: French and English, French is the default.** No user-facing text outside `core/l10n/strings.dart`, and
   every string has both a French and an English version.

## Backend (`backend/`)

- Java 21, Spring Boot 4.1, Spring Modulith, PostgreSQL 16 + Flyway, Testcontainers (no H2).
- Module layout: `<module>/domain`, `<module>/application` (use cases + `port/`), `<module>/infrastructure`, `<module>/web`.
- Money is `long` minor units + currency + decimals (XAF has 0 decimals). Never floating point.
- Migrations `V{n}__name.sql`, never edited after creation.
- Stable error `code` on every API error (RFC 7807); the app localizes from the code.
- Google and Apple sign-in are implemented last, after everything else.

## Flutter (`mobile/`)

- Riverpod (no codegen), go_router, dio. Immutable plain classes, no freezed/build_runner.
- Feature-first: `lib/features/<name>/{domain,data,presentation}`; shared code in `lib/core` and `lib/shared/widgets`.
- Theme color is Orange-500 (`AppColors.primary`). All user-facing text lives in `core/l10n/strings.dart` (French default, English).
- Reusable widget first: check `lib/shared/widgets` before writing any new UI.
- No business logic in widgets. Server is authoritative for session and payment state.
- Reference UI in `mobile/assets/design/` is an example only, not a pixel spec.
- API base URL defaults to the deployed backend `https://apicharging.up.railway.app`. For a local backend use `--dart-define=API_BASE_URL=http://10.0.2.2:8090` (Android emulator) or `http://<Mac LAN IP>:8090` (physical phone).

## Local development

```bash
cd users && docker compose up -d
cd backend && SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
./mvnw verify
```

Maildev inbox: http://localhost:1080. Phone OTP codes (6 digits) are written to the backend log for now.
