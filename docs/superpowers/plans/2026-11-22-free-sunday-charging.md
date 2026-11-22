# Free Sunday Charging Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Selected NDZela drivers get one free charge per Sunday (12:00 to 22:00 Gabon time) when they have zero unpaid days, managed from both dashboards and enforced by the backends.

**Architecture:** NDZela owns agreements, the window rule and eligibility (single rules engine) and answers a signed read from Zella. Zella enforces at charge start with a database-unique Sunday claim, stops free sessions at 22:00, and its dashboard forwards administration to NDZela. The apps only display server answers.

**Tech Stack:** Zella backend: Java 21, Spring Boot 4.1, Modulith, Postgres 16, Flyway, Testcontainers. NDZela backend: Java 21, Spring Boot 4, Lombok. Dashboards: Next.js, TypeScript. Mobile: Flutter, Riverpod.

**Spec:** `docs/superpowers/specs/2026-11-22-free-sunday-charging-design.md`

## Global Constraints

- **Never commit, push or branch.** The owner commits. There are no commit steps in this plan.
- Zella code: no comments, files under about 150 lines, one public type per file, `web -> application -> domain`, money as `long` minor units, migrations never edited (Zella next is `V42`), stable error codes, FR and EN strings only in `core/l10n/strings.dart`, colors only from `AppColors`.
- NDZela code: read `docs/` first, files under about 200 lines, update docs and add an ADR in the same change, every new tenant table has `country_id` and an isolation test (NDZela next migration is `V72`).
- Never modify the OCPP platform (`/Users/mac/Desktop/development/zell-ocpp`).
- Window: Sunday, start inclusive 12:00:00, end exclusive 22:00:00, `Africa/Libreville`, defined only in NDZela.
- One free session per customer per Sunday. A session that fails or delivers no energy releases the Sunday. An early stop keeps it used.
- The free session stops at 22:00 with stop reason `FREE_WINDOW_ENDED`.
- NDZela unreachable at start means no free benefit and the normal paid path continues.
- Free feed category: `FREE_SUNDAY`.

## Shared contract (both sides code to this)

NDZela `GET /api/v1/partners/zella/customers/{customerId}/free-charging`, header `X-Api-Key`, answer:

```json
{"eligible":true,"reason":"ELIGIBLE","agreementId":"uuid","sundayDate":"2026-11-22","windowEndsAt":"2026-11-22T21:00:00Z"}
```

`reason` is one of `ELIGIBLE, NO_AGREEMENT, AGREEMENT_SUSPENDED, AGREEMENT_NOT_STARTED, AGREEMENT_ENDED, OUTSIDE_WINDOW, UNPAID_DAYS, DRIVER_SUSPENDED, NOT_LINKED`. `agreementId`, `sundayDate`, `windowEndsAt` are null when not eligible. Zella adds one local reason, `UNAVAILABLE`, when NDZela does not answer.

Management operations, both under `/api/v1/partners/zella/free-charging/agreements` (shared key plus header `X-Actor`) and under the staff API `/api/v1/free-charging/agreements`: `GET` list, `POST` create, `PUT /{id}` edit, `POST /{id}/suspend`, `POST /{id}/resume`, `POST /{id}/revoke`, and `GET /usage`.

## Review Focus

- A start at exactly 11:59:59 or 22:00:00 is refused; 12:00:00 and 21:59:59 are accepted.
- A driver whose Saturday rent is unpaid at 12:00:00 Sunday is refused with `UNPAID_DAYS`.
- A retried start with the same idempotency key returns the earlier session and creates no second claim.
- An agreement suspended or revoked while a free session runs does not stop that session; only 22:00 does.
- The 22:00 sweep never sends a second stop for a session already stopping or finished, and an unreachable charger is retried.

---

## Part 1: NDZela backend

### Task 1: Agreements, audit and permissions (NDZela)

**Files:**
- Create: migration `V72__free_charging_agreements.sql` in NDZela's migration folder
- Create: `backend/src/main/java/com/ndzela/admin/freecharging/{domain,application,api}` with `FreeChargingAgreement`, `AgreementStatus`, `FreeChargingAgreementRepository`, `FreeChargingAgreementService`
- Test: `backend/src/test/java/com/ndzela/admin/freecharging/FreeChargingAgreementServiceTest.java`, `FreeChargingTenantIsolationTest.java`

**Interfaces:**
- Produces: `FreeChargingAgreementService` with `create(UUID driverId, LocalDate startsOn, LocalDate endsOn, String conditions)`, `update(UUID id, LocalDate startsOn, LocalDate endsOn, String conditions)`, `suspend(UUID id)`, `resume(UUID id)`, `revoke(UUID id)`, `list()`, `findActiveFor(UUID driverId): Optional<FreeChargingAgreement>`. `AgreementStatus` is `ACTIVE | SUSPENDED | REVOKED`.
- Permissions seeded in `V72`: `free_charging:read` (SUPER_ADMIN, ADMIN, GESTIONNAIRE, COMPTABLE, VIEWER) and `free_charging:manage` (SUPER_ADMIN, ADMIN).

- [ ] **Step 1: Write failing tests.** `createsAnActiveAgreementForALinkedDriver`; `refusesASecondNonRevokedAgreementForTheSameDriver` (error code `FREE_AGREEMENT_EXISTS`); `refusesADriverWithoutALinkedZellaAccount` (`FREE_AGREEMENT_NOT_LINKED`); `refusesEndBeforeStart`; `revokeKeepsTheRowAndAllowsANewAgreement`; each action writes its audit entry (`FREE_AGREEMENT_CREATED`, `UPDATED`, `SUSPENDED`, `RESUMED`, `REVOKED`); `anotherCountrysStaffCannotReadOrChangeIt`.
- [ ] **Step 2: Run them.** `cd /Users/mac/Desktop/ndzela/backend && ./mvnw -q test -Dtest='FreeCharging*'`. Expected: FAIL (classes missing).
- [ ] **Step 3: Implement.** Table columns: id, `country_id`, `driver_id`, optional `contract_id`, `starts_on`, `ends_on`, status, conditions, created/updated by and at. Partial unique index on `driver_id` where status is not `REVOKED`. Use the existing audit service, tenant holder and driver-link lookup (`IdTagLinkService`) as the other features do.
- [ ] **Step 4: Run the tests.** Expected: PASS.

### Task 2: Window and eligibility rule (NDZela)

**Files:**
- Create: `freecharging/domain/FreeChargingWindow.java`, `freecharging/application/FreeChargingEligibility.java`, `freecharging/domain/EligibilityReason.java`, properties `app.free-charging.{day,from,until}` read through one record
- Test: `FreeChargingWindowTest.java`, `FreeChargingEligibilityTest.java`

**Interfaces:**
- Consumes: Task 1 `findActiveFor`; `DriverArrearsService.get(UUID).unpaidDays()`; the driver status; the linked-customer lookup.
- Produces: `FreeChargingWindow.contains(Instant now, ZoneId zone): boolean`, `sundayOf(Instant now, ZoneId zone): LocalDate`, `endOf(Instant now, ZoneId zone): Instant`. `FreeChargingEligibility.check(String zellaCustomerId, Instant now): Eligibility` where `Eligibility(boolean eligible, EligibilityReason reason, UUID agreementId, LocalDate sundayDate, Instant windowEndsAt)`.

- [ ] **Step 1: Write failing tests.** Window: 11:59:59 false, 12:00:00 true, 21:59:59 true, 22:00:00 false, a Saturday 15:00 false, a Monday false. Eligibility: one test per reason in the shared contract, plus `eligibleWithZeroUnpaidDaysInsideTheWindow` and `oneUnpaidDayGivesUNPAID_DAYS`, `suspendedDriverGivesDRIVER_SUSPENDED`, `agreementEndedYesterdayGivesAGREEMENT_ENDED`, `agreementEndingTodayIsStillEligible`.
- [ ] **Step 2: Run to see them fail.** `./mvnw -q test -Dtest='FreeChargingWindowTest,FreeChargingEligibilityTest'`.
- [ ] **Step 3: Implement.** Check order: not linked, no agreement, status, dates, driver status, window, unpaid days. The window is the only place that knows Sunday, 12:00 and 22:00.
- [ ] **Step 4: Run to see them pass.**

### Task 3: Partner endpoints and staff API (NDZela)

**Files:**
- Create: `charging/api/ZellaFreeChargingController.java` (read plus management, shared-key pattern of `ZellaPartnerController`), `freecharging/api/FreeChargingController.java` (staff JWT, permission-guarded), DTOs
- Test: `ZellaFreeChargingControllerTest.java`, `FreeChargingControllerTest.java`

**Interfaces:**
- Consumes: Tasks 1 and 2.
- Produces: the endpoints in the shared contract. Management calls from Zella carry `X-Actor`, stored in the audit entry as `zella:<actor>`. List rows hold driver name, dates, status, eligibility reason, unpaid days and last free use.

- [ ] **Step 1: Write failing tests.** Missing or wrong key gives 401 with no data; the read endpoint returns the contract JSON for an eligible driver; a staff user without `free_charging:manage` gets 403 on create; `X-Actor` appears in the audit entry; rate limit applies.
- [ ] **Step 2: Run, expect FAIL.** **Step 3: Implement.** **Step 4: Run, expect PASS.**

### Task 4: Usage history and the `FREE_SUNDAY` mark (NDZela)

**Files:**
- Modify: the Zella feed reader (`ZellaFeedService`, `ZellaSessionWriter`) and the `session_payments.free_category` constraint through a new migration `V73__free_sunday_category.sql`
- Create: `GET /free-charging/usage` backed by sessions whose `free_category = 'FREE_SUNDAY'`
- Test: `FreeSundayUsageTest.java`

**Interfaces:**
- Consumes: Zella feed item `freeCategory` value `FREE_SUNDAY` (Task 8).
- Produces: usage rows `(sessionId, driverId, driverName, startedAt, energyKwh, stopReason)`.

- [ ] **Step 1: Write failing tests.** A feed item with `FREE_SUNDAY` is stored and shown as "Offert" without a payment button; usage lists it; it never counts as revenue.
- [ ] **Step 2 to 4:** run, implement, run.

---

## Part 2: Zella backend

### Task 5: Sunday claim storage (Zella)

**Files:**
- Create: `V42__free_sunday_claim.sql`, `partner/FreeSundayClaims.java` (port), `partner/infrastructure/JdbcFreeSundayClaims.java`
- Test: `partner/infrastructure/JdbcFreeSundayClaimsTest.java`

**Interfaces:**
- Produces: `boolean claim(UUID customerId, LocalDate sundayDate, UUID sessionId, UUID agreementId, Instant windowEndsAt)` (true if taken), `void release(UUID sessionId)`, `Optional<UUID> agreementOf(UUID sessionId)`, `List<UUID> sessionsPastWindow(Instant now)`.
- Table `free_sunday_claim`: country code with the project's row-level security, customer, Sunday date, session id, agreement id, window end, state `CLAIMED | RELEASED`, timestamps. Unique on (customer, Sunday date) where state is `CLAIMED`.

- [ ] **Step 1: Write failing tests (Testcontainers).** `secondClaimForTheSameSundayIsRefused`; `fiftyParallelClaimsForOneCustomerAndSundayYieldExactlyOne`; `releasedClaimCanBeTakenAgain`; `claimsAreInvisibleToAnotherCountry`.
- [ ] **Step 2: Run, expect FAIL.** `cd backend && ./mvnw -q test -Dtest=JdbcFreeSundayClaimsTest`. **Step 3: Implement. Step 4: Run, expect PASS.**

### Task 6: NDZela eligibility client and offer (Zella)

**Files:**
- Create: `partner/FreeSundayAccess.java` (public interface), `partner/application/FreeSundayOffers.java`, `partner/application/port/NdzelaFreeCharging.java`, `partner/infrastructure/HttpNdzelaFreeCharging.java`
- Test: `FreeSundayOffersTest.java`, `HttpNdzelaFreeChargingTest.java`

**Interfaces:**
- Produces: `FreeSundayAccess.offer(UUID customerId): FreeSundayOffer` and `claimFor(UUID customerId, UUID sessionId): boolean`, with `FreeSundayOffer(boolean available, String reason, Instant endsAt)`. Reuse the existing NDZela base URL and key settings and the timeout style of the other partner calls.

- [ ] **Step 1: Write failing tests.** `eligibleAnswerBecomesAnAvailableOffer`; `timeoutGivesUNAVAILABLEAndNoOffer`; `errorStatusGivesUNAVAILABLE`; `claimForTakesTheClaimOnlyWhenEligible`; `claimForReturnsFalseWhenSundayAlreadyClaimed`.
- [ ] **Step 2 to 4:** run, implement (the grant calls the port, then `FreeSundayClaims.claim`), run.

### Task 7: Free reservation and start integration (Zella)

**Files:**
- Modify: `wallet/WalletFunding.java` (add `Money reserveFree(UUID customerId, UUID sessionId, long amountMinor)`), `wallet/application/FundingService.java`, `charging/application/SessionService.java`, `charging/application/SessionSettlement.java`
- Test: `charging/application/FreeSundayStartTest.java`

**Interfaces:**
- Consumes: Task 6 `FreeSundayAccess.claimFor`.
- Produces: `reserveFree` inserts the hold with `waived = true` without a funds check. In `start`, the order is idempotent-earlier check, live-session check, connector check, then `claimFor`; on true use `reserveFree`, otherwise `reserve`. The claim is released when the session insert fails, when the start fails (`fail`), and when a session closes with zero energy.

- [ ] **Step 1: Write failing tests.** `eligibleDriverStartsWithAWaivedHoldAndAClaim`; `secondStartTheSameSundayIsPaid`; `secondStartWithNoFundsFailsWithINSUFFICIENT_FUNDS`; `failedStartReleasesTheClaim`; `zeroEnergyCloseReleasesTheClaim`; `earlyStopWithEnergyKeepsTheClaim`; `retryWithTheSameIdempotencyKeyCreatesNoSecondClaim`; `twoParallelStartsOneFreeOnePaid`.
- [ ] **Step 2: Run, expect FAIL.** **Step 3: Implement. Step 4: Run, expect PASS.**

### Task 8: Public offer endpoint and free feed category (Zella)

**Files:**
- Create: `charging/web/FreeSundayController.java`, `FreeSundayResponse.java`
- Modify: `charging/infrastructure/JdbcChargeFeed.java` and `SessionBilling` so a session with a `CLAIMED` claim reports `freeCategory = FREE_SUNDAY`
- Test: `FreeSundayControllerTest.java`, `FreeSundayFeedTest.java`

**Interfaces:**
- Produces: `GET /v1/free-sunday` returning `{available, reason, endsAt}` for the signed-in customer, with the reason as a stable code. The partner feed item carries `freeCategory: "FREE_SUNDAY"`.

- [ ] **Step 1: Write failing tests.** Unauthenticated gets 401; reason codes are returned untranslated; a claimed session appears in the feed with `FREE_SUNDAY` and a zero charge.
- [ ] **Step 2 to 4:** run, implement, run.

### Task 9: The 22:00 stop (Zella)

**Files:**
- Create: `charging/application/FreeWindowStopper.java`
- Modify: `charging/application/SessionSweeper.java` (call it once per country sweep)
- Test: `FreeWindowStopperTest.java`

**Interfaces:**
- Consumes: Task 5 `sessionsPastWindow(Instant)`, the existing stop path (`StopCommander`).
- Produces: `FreeWindowStopper.stopExpired(): void`, stop reason `FREE_WINDOW_ENDED`, audit `FREE_SUNDAY_WINDOW_STOPPED`.

- [ ] **Step 1: Write failing tests with a fixed clock.** `stopsAClaimedSessionAfterTheWindowEnds`; `doesNotStopBeforeTheEnd`; `ignoresAlreadyStoppedOrStoppingSessions`; `retriesWhenTheChargerRefuses` (second sweep sends again); `neverStopsAPaidSession`.
- [ ] **Step 2 to 4:** run, implement, run.

### Task 10: Admin forwarder, permissions and audit (Zella)

**Files:**
- Create: `V43__free_charging_permission.sql` (permissions `FREE_CHARGING_READ`, `FREE_CHARGING_MANAGE`, same pattern as V39), `admin/web/AdminFreeChargingController.java`, `partner/application/FreeChargingAdministration.java`
- Test: `AdminFreeChargingControllerTest.java`

**Interfaces:**
- Produces: `/v1/admin/free-charging/agreements` (list, create, edit, suspend, resume, revoke) and `/v1/admin/free-charging/usage`, each forwarding to NDZela with `X-Actor`, and one Zella audit entry per management call. An NDZela failure returns a stable error `FREE_CHARGING_UNAVAILABLE`.

- [ ] **Step 1: Write failing tests.** Missing permission gives 403; each call forwards the actor; NDZela down gives `FREE_CHARGING_UNAVAILABLE` and no audit success entry.
- [ ] **Step 2 to 4:** run, implement, run. Then run the whole suite: `./mvnw verify`.

---

## Part 3: Dashboards

### Task 11: "Gratuité" page in the NDZela dashboard

**Files:** a new `dashboard/src/features/free-charging/` (page, table, add and edit form, usage tab), the sidebar entry, `permissions.ts` mirror, FR and EN labels, tests next to the feature.

- [ ] **Step 1: Write failing tests.** The table shows status, reason and unpaid days from the API; buttons are hidden without `free_charging:manage`; add, suspend, resume and revoke call the right endpoints; revoke asks for confirmation.
- [ ] **Step 2: Run the dashboard tests, expect FAIL. Step 3: Implement with existing `components/ui` and shared table patterns. Step 4: run, expect PASS.**

### Task 12: "Gratuité" page in the Zella dashboard

**Files:** a new `dashboard/src/features/free-charging/` and route handlers under the BFF pattern in `src/server`, the sidebar entry, FR and EN labels, tests.

- [ ] **Step 1 to 4:** same tests as Task 11 against `/v1/admin/free-charging`, plus `NDZela unavailable shows a retry message`. No rule or eligibility logic in the page.

---

## Part 4: Mobile

### Task 13: Free Sunday display (Zella app)

**Files:**
- Create: `lib/features/charging/domain/free_sunday_offer.dart`, `data/free_sunday_repository.dart`, `presentation/free_sunday_badge.dart`, provider, strings in `lib/core/l10n/strings.dart` (FR default and EN for every reason code and the closing notice)
- Modify: the home page and the charge confirmation screen to show the badge, and the Profile "My coupons" screen (`lib/features/coupons`), where the free Sunday benefit appears as its own entry (card) above the coupons, with its state (available until the end time, used this Sunday, or the reason it is unavailable) and no rule in the app
- Test: `test/ui/free_sunday_badge_test.dart`

**Interfaces:**
- Consumes: `GET /v1/free-sunday`. The badge shows the end time when available, the reason in words when not, and a notice for the last 30 minutes of the window.

- [ ] **Step 1: Write failing tests.** Available shows the badge and the end time; each reason code has an FR and an EN text; unavailable hides the badge on Home but shows the reason on the confirmation screen; no color outside `AppColors`.
- [ ] **Step 2: `flutter test test/ui/free_sunday_badge_test.dart`, expect FAIL. Step 3: implement. Step 4: expect PASS, then `flutter analyze` and the whole `flutter test`.**

---

## Part 5: Documents and end-to-end check

### Task 14: Documents and checks

- [ ] **Step 1:** NDZela: add the ADR for this feature, update `payments-and-arrears.md`, `ocpp-integration.md` and the permission catalogue. Zella: update `NDZELA_INTEGRATION.md`, `INTEGRATION_CONTRACTS.md` and `CHARGING_WORKFLOW.md`.
- [ ] **Step 2: End-to-end on throwaway data.** With a throwaway Zella database and NDZela locally, for a fixed Sunday clock: an eligible driver starts free at 12:00; a second start is paid; the same driver with one unpaid day is refused with `UNPAID_DAYS`; a free session is stopped at 22:00; a driver with three unpaid days is suspended in both apps and sees only the payment screen in the NDZela driver app, and is restored after paying all days.
- [ ] **Step 3:** Run `./mvnw verify` in both backends, the dashboards' tests and `flutter test`. Report real results only.

## Self-review notes

- Spec coverage: rules 1 to 9 map to Tasks 2, 5, 7, 9 and 14; dashboards to Tasks 11 and 12; mobile to Task 13; audit to Tasks 1, 3, 9 and 10; docs to Task 14.
- One change from the spec: Zella does not check the window itself. It relies on NDZela's `OUTSIDE_WINDOW`, `sundayDate` and `windowEndsAt`, so the Sunday rule exists in one place only.
