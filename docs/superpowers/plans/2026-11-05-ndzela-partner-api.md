# NDZela Partner API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give NDZela a key-protected, read-mostly API to find which Zella customers are its drivers, confirm links, and pull what each linked driver paid per charge.

**Architecture:** A new Spring Modulith module `partner` (`domain`, `application` + `port/`, `infrastructure`, `web`) with its own `SecurityFilterChain` for `/v1/partners/**` authenticated by a hashed `X-Api-Key` bound to one country. It reaches `account`, `charging` and `wallet` only through small public interfaces at their module roots. Charging gains a `session_charge` table written at settlement so the fee breakdown is stored, not recomputed.

**Tech Stack:** Java 21, Spring Boot 4.1, Spring Modulith, Spring Security, JdbcClient, PostgreSQL 16 + Flyway, Testcontainers (no H2), RLS tenancy.

**Spec:** `docs/superpowers/specs/2026-11-05-ndzela-partner-api-design.md` (contract agreed in `docs/NDZELA_INTEGRATION.md` section 8b)

## Global Constraints

- **NEVER run `git commit`, `git push` or create branches** (project CLAUDE.md overrides any "commit" habit). Where a task would end with a commit it ends with "run the checks and report"; the user commits.
- **No comments in code** (no Javadoc, `//`, `/* */`). Names carry the meaning.
- Every file under ~150 lines, one public type per file, no duplicated logic.
- Dependencies point inward: `web -> application -> domain`; `infrastructure` implements `application/port`. Domain has no framework imports. Business logic never in controllers.
- Other modules are reached only through interfaces at their module root (like `WalletFunding`, `StartableConnectors`, `CustomerDirectory`); never read their tables from `partner`.
- Money is `long` minor units plus `currency` plus `currencyDecimals` (XAF: 0). Never floating point.
- Migrations `V{n}__name.sql`, never edited after creation. Next numbers are `V30` and `V31`.
- Stable error `code` on every API error (RFC 7807): `PARTNER_KEY_INVALID`, `PARTNER_BATCH_TOO_LARGE`, `PARTNER_CURSOR_INVALID`, `PARTNER_LIMIT_INVALID`, `PARTNER_CONTACT_INVALID`, `TOO_MANY_REQUESTS` (existing).
- Tenant tables carry `country_code char(2) not null default current_setting('app.country')`, a composite FK `(customer_id, country_code) -> customer (id, country_code)` and `tenant_isolation` RLS (pattern: `V26__social_identity.sql`). `partner_api_key` is the exception: it is read before any tenant is known, so it has no RLS.
- Integration tests extend `PostgresTestBase` (Testcontainers), tenant `GA`. Run: `cd backend && ./mvnw -q test -Dtest=<Class>` (Docker must be running). Full gate: `cd backend && ./mvnw verify`.
- Defaults (settings prefix `zella.partner`): rate 120 per minute per key, lookup batch max 200, feed page default 100 and max 500.
- Lookup never creates an `idTag` (it would register it on the OCPP platform). Names are returned on `FULL` matches only. Only `ACTIVE` customers are matched (my call; see Review Focus).

## Review Focus

- Lookup email with capitals or surrounding spaces, or a phone that is not E.164: must be 400 `PARTNER_CONTACT_INVALID`, never a silent non-match. (Task 5)
- Two entries in one batch with the same email, or one customer matched by email for `ref` A and phone for `ref` B: each `ref` gets its own result and no result leaks another entry's data. (Task 5)
- A `BLOCKED` or `PENDING_PHONE` customer: not matched, so a blocked account's identity is never confirmed to NDZela. (Task 5)
- A session whose hold is captured for less than the computed total (cost above the authorized budget): `totalChargedMinor` equals what the wallet actually paid, not the computed total. (Task 7)
- A session updated after NDZela already read past it (late settlement, refund state): it must reappear with a newer `updatedAt`, never be skipped; an empty page with a cursor returns that same cursor and `hasMore=false`. (Task 8)

---

## File Structure

| File | Responsibility |
|---|---|
| `partner/package-info.java` | `@ApplicationModule` |
| `partner/PartnerKeys.java`, `IssuedKey.java`, `PartnerKeyInfo.java` | public root API used by `admin` |
| `partner/domain/PartnerKey.java` | key entity: hash, country, expiry, revocation |
| `partner/application/PartnerSettings.java` | `@ConfigurationProperties("zella.partner")` |
| `partner/application/PartnerKeyService.java`, `PartnerPrincipal.java` + `port/PartnerKeyStore.java` | generate, hash, verify, revoke; principal record |
| `partner/application/LookupService.java` | batch lookup with FULL/PARTIAL rules |
| `partner/application/LinkService.java` + `port/LinkStore.java` | confirm and remove links |
| `partner/application/SessionFeedService.java` + `FeedCursor.java` | cursor paging over the charging feed |
| `partner/infrastructure/JdbcPartnerKeyStore.java`, `JdbcLinkStore.java` | persistence |
| `partner/web/PartnerSecurityConfiguration.java`, `PartnerKeyFilter.java` | auth, tenant binding, rate limit |
| `partner/web/LookupController.java`, `LinkController.java`, `SessionFeedController.java` + request/response records | endpoints |
| `account/CustomerContacts.java`, `CustomerMatch.java`, `account/infrastructure/JdbcCustomerContacts.java` | customers by email or phone |
| `charging/CustomerIdTags.java`, `charging/ChargeFeed.java`, `ChargeFeedItem.java`, `ChargeFeedPage.java` | root interfaces for partner |
| `charging/application/SessionChargeRecorder.java`, `charging/infrastructure/JdbcSessionCharges.java`, `JdbcChargeFeed.java`, `SessionChargeBackfill.java` | session_charge write, feed read, backfill |
| `admin/web/AdminPartnerKeyController.java` | issue, list, revoke keys (`ADMINS_MANAGE`) |
| `db/migration/V30__partner.sql`, `V31__session_charge.sql` | schema |

---

### Task 1: Partner keys (storage, hashing, verification)

**Files:**
- Create: `db/migration/V30__partner.sql` (tables `partner_api_key`, `partner_link`)
- Create: `partner/package-info.java`, `partner/PartnerKeys.java`, `partner/IssuedKey.java`, `partner/PartnerKeyInfo.java`, `partner/domain/PartnerKey.java`, `partner/application/PartnerSettings.java`, `partner/application/PartnerKeyService.java`, `partner/application/port/PartnerKeyStore.java`, `partner/infrastructure/JdbcPartnerKeyStore.java`
- Modify: `application.yml` (add `zella.partner` block), `application` class if `@ConfigurationPropertiesScan` is not already present
- Test: `test/.../partner/PartnerKeyServiceIntegrationTest.java`, `test/.../partner/PartnerFixtures.java`

**Interfaces:**
- Produces:
  - `public interface PartnerKeys { IssuedKey issue(String partner, String country, UUID actorId); void revoke(UUID keyId, UUID actorId); List<PartnerKeyInfo> list(String partner); }`
  - `public record IssuedKey(UUID id, String key)`
  - `public record PartnerKeyInfo(UUID id, String partner, String country, Instant createdAt, Instant expiresAt, Instant revokedAt, Instant lastUsedAt)`
  - `record PartnerPrincipal(UUID keyId, String partner, String country)` (in `partner/application`, public to the module)
  - `Optional<PartnerPrincipal> PartnerKeyService.authenticate(String rawKey)`
  - `PartnerSettings(int ratePerMinute, int maxLookupBatch, int defaultFeedLimit, int maxFeedLimit)`
  - Test helper `PartnerFixtures`: `String keyFor(String country)`, `UUID customer(String firstName, String lastName, String email, String phone, String status)`, `String idTagFor(UUID customer)`.

- [ ] **Step 1: Write the failing tests** in `PartnerKeyServiceIntegrationTest`: `issuedKeyAuthenticatesAndIsStoredOnlyAsHash` (select `key_hash` from `partner_api_key`, assert it differs from the raw key and the raw key is nowhere in the table), `unknownKeyDoesNotAuthenticate`, `revokedKeyStopsAuthenticating`, `expiredKeyStopsAuthenticating` (set `expires_at` in the past), `twoActiveKeysWorkIndependentlyAndRevokingOneKeepsTheOther`, `authenticatingRecordsLastUsedAt`, `keysCarryTheirCountry`.
- [ ] **Step 2: Run** `./mvnw -q test -Dtest=PartnerKeyServiceIntegrationTest`. Expected: compilation FAIL (types missing).
- [ ] **Step 3: Migration `V30__partner.sql`:** `partner_api_key(id uuid pk, partner text not null check (partner in ('NDZELA')), country_code char(2) not null, key_hash text not null unique, created_at timestamptz not null, expires_at timestamptz, revoked_at timestamptz, last_used_at timestamptz)` with no RLS; `partner_link(partner text, customer_id uuid, country_code char(2) default current_setting('app.country'), linked_at timestamptz not null, primary key (partner, customer_id), foreign key (customer_id, country_code) references customer (id, country_code))` with the standard `tenant_isolation` RLS. Grants come from the default privileges set in V9.
- [ ] **Step 4: Implement** `PartnerKeyService` (`@Service`, implements `PartnerKeys`): key format `zpk_` + 43 URL-safe base64 chars from `SecureRandom` (32 bytes); store `SHA-256` hex of the key; `authenticate` hashes the input, looks it up with `ConstantTime` helper from `shared`, rejects revoked or expired using the injected `Clock`, updates `last_used_at` at most once a minute. `issue` and `revoke` call `AuditTrail.record("PARTNER_KEY_ISSUED" / "PARTNER_KEY_REVOKED", actorId, keyId.toString(), partner + ":" + country)`. `JdbcPartnerKeyStore` runs with no tenant bound (empty `app.country`), which is fine because the table has no RLS.
- [ ] **Step 5: Run** the test class again. Expected: PASS. Also run `ModuleStructureTest`. Expected: PASS.
- [ ] **Step 6: Report** results to the user (no commit).

---

### Task 2: Partner security chain (key filter, tenant binding, rate limit)

**Files:**
- Create: `partner/web/PartnerSecurityConfiguration.java`, `partner/web/PartnerKeyFilter.java`
- Modify: `shared/web/SecurityConfiguration.java` (extract the shared header customizer into a reusable bean/method so the partner chain does not duplicate the header block; keep behavior identical)
- Test: `test/.../partner/PartnerAuthenticationIntegrationTest.java`

**Interfaces:**
- Consumes: `PartnerKeyService.authenticate(String) -> Optional<PartnerPrincipal>`, `RateLimiter.tryAcquire(String bucket, String key, int limit, Duration window)`, `PartnerSettings.ratePerMinute()`, `TenantContext.open(String)`.
- Produces: all `/v1/partners/**` requests carry a `PartnerPrincipal` as the authentication principal and run inside `TenantContext` of the key's country. Controllers take `PartnerPrincipal` as an argument via `@AuthenticationPrincipal`.

- [ ] **Step 1: Write the failing tests** with a throwaway `GET /v1/partners/ping` test controller registered only in the test source set: `missingKeyIs401WithCodePartnerKeyInvalid`, `wrongKeyIs401`, `revokedKeyIs401`, `customerJwtIsRejectedOnPartnerPaths`, `partnerKeyIsRejectedOnCustomerPaths` (`GET /v1/wallet` with `X-Api-Key` stays 401), `validKeyBindsItsCountry` (the ping returns `TenantContext.require()`), `rateLimitReturns429WithRetryAfterAfter120Requests` (set `zella.partner.rate-per-minute=3` in the test), `secondKeyHasItsOwnBucket`, `responsesCarryTheSecurityHeaders`.
- [ ] **Step 2: Run** the class. Expected: FAIL (401 on `ping` for the wrong reason or 404).
- [ ] **Step 3: Implement** a second `SecurityFilterChain` with `@Order(1)` and `securityMatcher("/v1/partners/**")`: stateless, csrf off, shared headers, `PartnerKeyFilter` before `AbstractPreAuthenticatedProcessingFilter`. The existing chain keeps `anyRequest().authenticated()` and never sees these paths. `PartnerKeyFilter` (a `OncePerRequestFilter`): read `X-Api-Key`; on failure resolve `ApiException(401, "PARTNER_KEY_INVALID", ...)` through the `handlerExceptionResolver`; apply the rate limit with bucket `partner`, key `keyId`; open `TenantContext` for the key's country for the rest of the chain.
- [ ] **Step 4: Run** the class and `ApiProtectionIntegrationTest`, `SecurityHardeningIntegrationTest`. Expected: PASS (existing security behavior unchanged).
- [ ] **Step 5: Report** (no commit).

---

### Task 3: Admin endpoints to issue, list and revoke keys

**Files:**
- Create: `admin/web/AdminPartnerKeyController.java`, `admin/web/PartnerKeyResponse.java`
- Test: `test/.../admin/AdminPartnerKeyIntegrationTest.java`

**Interfaces:**
- Consumes: `PartnerKeys.issue/revoke/list`, `AdminAccess` argument resolver (`access.can(Permission.ADMINS_MANAGE)`).
- Produces: `POST /v1/admin/partners/{partner}/keys` (body `{"country":"GA"}`) -> 201 `{ "id", "key", "country", "createdAt" }` (the key is returned only here); `GET /v1/admin/partners/{partner}/keys` -> 200 list without any key material or hash; `DELETE /v1/admin/partners/{partner}/keys/{id}` -> 204. Permission `ADMINS_MANAGE`, otherwise 403 with the existing forbidden code. Unknown partner or country not covered by the admin -> 404 or 403 following `AdminTargets` conventions.

- [ ] **Step 1: Write the failing tests** using `AdminFixtures`: `superAdminIssuesAKeyShownOnce`, `listNeverContainsTheKeyOrItsHash`, `revokeStopsTheKeyAtOnce` (use the issued key against the Task 2 ping), `adminWithoutAdminsManageIsForbidden` (`AdminRole.SUPPORT`), `unknownPartnerIs404`, `issuingAndRevokingWriteAuditRowsWithoutTheKey` (select from `audit_record`, assert no row contains the key).
- [ ] **Step 2: Run** the class. Expected: FAIL (404 on the new routes).
- [ ] **Step 3: Implement** the controller thin: validate `partner` against the one supported value, delegate to `PartnerKeys`, map to `PartnerKeyResponse`.
- [ ] **Step 4: Run** the class and `ModuleStructureTest` (admin -> partner dependency must be allowed through the root interface only). Expected: PASS.
- [ ] **Step 5: Report** (no commit).

---

### Task 4: Customer contact lookup and idTag lookup (module roots)

**Files:**
- Create: `account/CustomerContacts.java`, `account/CustomerMatch.java`, `account/infrastructure/JdbcCustomerContacts.java`, `charging/CustomerIdTags.java`, `charging/infrastructure/JdbcCustomerIdTags.java`
- Test: `test/.../account/CustomerContactsIntegrationTest.java`, `test/.../charging/CustomerIdTagsIntegrationTest.java`

**Interfaces:**
- Produces:
  - `public interface CustomerContacts { List<CustomerMatch> activeByEmailOrPhone(Collection<String> emails, Collection<String> phonesE164); boolean activeExists(UUID customerId); }`
  - `public record CustomerMatch(UUID id, String firstName, String lastName, String email, String phoneE164)`
  - `public interface CustomerIdTags { Map<UUID, String> existing(Collection<UUID> customerIds); }`
- Both run in the bound tenant (RLS applies; no explicit country filter needed, but add one for defense as `StatisticsQueries` do).

- [ ] **Step 1: Write the failing tests:** `findsByEmailOnly`, `findsByPhoneOnly`, `findsByEmailAndPhone`, `ignoresBlockedAndPendingCustomers`, `doesNotCrossCountries` (insert a customer in `ML`, query as `GA`), `activeExistsIsFalseForOtherCountry`; for idTags `returnsOnlyExistingTagsAndNeverCreatesOne` (count rows in `customer_id_tag` before and after).
- [ ] **Step 2: Run** both classes. Expected: compilation FAIL.
- [ ] **Step 3: Implement** the interfaces and the Jdbc classes; use `= any(:ids)` array parameters, at most 400 values per call enforced by the caller.
- [ ] **Step 4: Run** both classes and `ModuleStructureTest`. Expected: PASS.
- [ ] **Step 5: Report** (no commit).

---

### Task 5: Lookup endpoint

**Files:**
- Create: `partner/application/LookupService.java`, `partner/domain/MatchQuality.java`, `partner/web/LookupController.java`, `partner/web/LookupRequest.java`, `partner/web/LookupResponse.java`
- Test: `test/.../partner/LookupIntegrationTest.java`

**Interfaces:**
- Consumes: `CustomerContacts.activeByEmailOrPhone`, `CustomerIdTags.existing`, `PartnerSettings.maxLookupBatch()`, `PartnerPrincipal`.
- Produces: `POST /v1/partners/ndzela/id-tags/lookup`; request `{ "drivers": [ { "ref": "string", "email": "string", "phoneE164": "string" } ] }`; response `{ "results": [ { "ref", "matches": [ { "customerId", "idTag", "matchedOn": ["email","phone"], "quality": "FULL"|"PARTIAL", "firstName", "lastName" } ] } ] }` where `firstName` and `lastName` are present only for `FULL` and `idTag` is null when the customer never charged.
- Rules: `FULL` = one customer whose email equals the entry's and phone equals the entry's; otherwise each customer matching exactly one field is a `PARTIAL` with `matchedOn` listing that field. Email must be lowercase without spaces and phone `^\+[1-9][0-9]{7,14}$`, else 400 `PARTNER_CONTACT_INVALID`. 1 to 200 entries, else 400 `PARTNER_BATCH_TOO_LARGE` (empty list also 400). Audit `PARTNER_LOOKUP` with the batch size and the number of full and partial matches only.

- [ ] **Step 1: Write the failing tests:** `fullMatchReturnsNamesAndBothFields`, `emailOnlyIsPartialWithoutNames`, `phoneOnlyIsPartialWithoutNames`, `splitMatchGivesTwoPartialsForOneRef`, `noMatchReturnsEmptyMatchesAndNothingElse`, `refIsEchoedPerEntry`, `duplicateEmailsInOneBatchAnswerEachRefIndependently`, `batchOf201Is400`, `emptyBatchIs400`, `uppercaseEmailIs400`, `phoneWithSpacesIs400`, `idTagIsNullBeforeTheFirstCharge` (assert `customer_id_tag` row count unchanged), `blockedCustomerIsNeverMatched`, `otherCountryCustomerIsNeverMatched`, `auditRowHoldsCountsOnlyAndNoEmailOrPhone`, `lookupWithoutKeyIs401`.
- [ ] **Step 2: Run** the class. Expected: FAIL (404).
- [ ] **Step 3: Implement** `LookupService.lookup(List<DriverQuery>) -> List<LookupResult>` (pure: one query for all emails and phones, then in-memory matching per entry) and the thin controller with bean validation on the request records.
- [ ] **Step 4: Run** the class. Expected: PASS.
- [ ] **Step 5: Report** (no commit).

---

### Task 6: Links (confirm and remove)

**Files:**
- Create: `partner/application/LinkService.java`, `partner/application/port/LinkStore.java`, `partner/infrastructure/JdbcLinkStore.java`, `partner/web/LinkController.java`
- Test: `test/.../partner/LinkIntegrationTest.java`

**Interfaces:**
- Consumes: `CustomerContacts.activeExists(UUID)`, table `partner_link` (Task 1).
- Produces: `PUT /v1/partners/ndzela/links/{customerId}` -> 204 (idempotent); `DELETE /v1/partners/ndzela/links/{customerId}` -> 204 (idempotent); `LinkStore.linkedCustomerIds(String partner) -> List<UUID>` for Task 8. `PUT` for a customer that is unknown, inactive or in another country -> 404 `NOT_FOUND`. Audit `PARTNER_LINK_CREATED` and `PARTNER_LINK_REMOVED` with the customer id as subject.

- [ ] **Step 1: Write the failing tests:** `putLinksAnActiveCustomer`, `putTwiceStillOneRow`, `putUnknownCustomerIs404`, `putCustomerOfAnotherCountryIs404`, `putBlockedCustomerIs404`, `deleteRemovesAndIsIdempotent`, `deleteUnknownIs204`, `linksAreAuditedWithoutPersonalData`, `partnerWithAMaliKeyCannotLinkAGabonCustomer` (key for `ML`).
- [ ] **Step 2: Run** the class. Expected: FAIL (404).
- [ ] **Step 3: Implement** `LinkService.link(UUID)`, `unlink(UUID)`, `linkedCustomerIds()`; `JdbcLinkStore` uses `insert ... on conflict do nothing`.
- [ ] **Step 4: Run** the class. Expected: PASS.
- [ ] **Step 5: Report** (no commit).

---

### Task 7: Store the charge breakdown (`session_charge`)

**Files:**
- Create: `db/migration/V31__session_charge.sql`, `charging/application/SessionChargeRecorder.java`, `charging/application/port/SessionCharges.java`, `charging/infrastructure/JdbcSessionCharges.java`, `charging/infrastructure/SessionChargeBackfill.java`
- Modify: `charging/application/SessionSettlement.java` (record the charge at every settlement outcome), `wallet/WalletFunding.java` (add `Optional<Long> captured(UUID sessionId)`) and its implementation
- Test: `test/.../charging/SessionChargeIntegrationTest.java`, extend `SessionReconcilerTest` only if it constructs `SessionSettlement`

**Interfaces:**
- Produces: table `session_charge(session_id uuid pk, customer_id uuid not null, country_code char(2) not null default current_setting('app.country'), state text not null check (state in ('CHARGED','NOT_CHARGED','UNDER_REVIEW','REFUNDED')), energy_cost_minor bigint, vat_minor bigint, service_fee_minor bigint, total_minor bigint, currency char(3), decimals smallint, estimated boolean not null default false, updated_at timestamptz not null)` with composite FK to `customer` and the standard RLS. Port `SessionCharges { void record(SessionCharge charge); boolean exists(UUID sessionId); }` and domain record `SessionCharge(UUID sessionId, UUID customerId, ChargeState state, Long energyCostMinor, Long vatMinor, Long serviceFeeMinor, Long totalMinor, String currency, Integer decimals, boolean estimated, Instant updatedAt)`.
- Rules: `COMPLETED` or `ABANDONED` with a cost -> `CHARGED` with the `FeeBreakdown` parts and `total_minor` = amount actually captured by the wallet (`WalletFunding.captured`), not the computed total; finished without a cost -> `UNDER_REVIEW` with null amounts; `FAILED` -> `NOT_CHARGED` with null amounts. Recording is an upsert on `session_id` so a replayed settlement does not duplicate.
- Backfill: at startup, per active country, for sessions in a terminal state with no `session_charge` row, insert with `estimated = true`, total from `WalletFunding.captured` when present, parts from the current `FeeCalculator`. Idempotent.

- [ ] **Step 1: Write the failing tests:** `completedSessionStoresTheBreakdownAtSettlement`, `breakdownDoesNotChangeWhenTheFeeRatesChangeLater` (settle, change `zella.fees.service-fee-percent` through the runtime settings, read the row again), `totalIsWhatTheWalletCapturedWhenCostExceedsTheBudget`, `failedSessionStoresNotCharged`, `sessionWithoutCostStoresUnderReview`, `replayedSettlementKeepsOneRow`, `backfillMarksOldRowsEstimatedAndIsIdempotent`, `rowsAreIsolatedByCountry`.
- [ ] **Step 2: Run** the class. Expected: FAIL (table missing).
- [ ] **Step 3: Implement** the migration, the port and Jdbc adapter, `SessionChargeRecorder.record(ChargingSession, FeeBreakdown, long capturedMinor)` called from `SessionSettlement.settleFinished` and the failed branch, `WalletFunding.captured`, and the backfill runner (one `ApplicationRunner` that loops `CountryDirectory.active()` inside `TenantContext.runAs`, same shape as `SessionSweeper`).
- [ ] **Step 4: Run** the class, `SessionReconcilerTest`, `StartRulesIntegrationTest`, `ModuleStructureTest`, and the wallet tests. Expected: PASS.
- [ ] **Step 5: Report** (no commit).

---

### Task 8: Payment feed endpoint

**Files:**
- Create: `charging/ChargeFeed.java`, `charging/ChargeFeedItem.java`, `charging/ChargeFeedPage.java`, `charging/infrastructure/JdbcChargeFeed.java`, `partner/application/FeedCursor.java`, `partner/application/SessionFeedService.java`, `partner/web/SessionFeedController.java`, `partner/web/FeedResponse.java`
- Test: `test/.../partner/SessionFeedIntegrationTest.java`

**Interfaces:**
- Consumes: `LinkStore.linkedCustomerIds()` (Task 6), `session_charge` (Task 7), `charging_session`, `CustomerIdTags`.
- Produces:
  - `public interface ChargeFeed { ChargeFeedPage after(Instant updatedAt, UUID sessionId, int limit, Collection<UUID> customerIds); }`
  - `public record ChargeFeedPage(List<ChargeFeedItem> items, boolean hasMore)`
  - `public record ChargeFeedItem(UUID sessionId, int transactionId, UUID customerId, String idTag, String stationId, int connectorId, String sessionState, Instant startedAt, Instant stoppedAt, BigDecimal energyKwh, Long energyCostMinor, Long vatMinor, Long serviceFeeMinor, Long totalChargedMinor, String currency, Integer currencyDecimals, String paymentState, boolean breakdownEstimated, Instant updatedAt)`
  - `GET /v1/partners/ndzela/sessions?cursor=&limit=` -> `{ "items": [...], "nextCursor": "...", "hasMore": bool }`; item field names exactly as in the spec (`transactionId`, `customerId`, `idTag`, `stationId`, `connectorId`, `sessionState`, `startedAt`, `stoppedAt`, `energyKwh`, `energyCostMinor`, `vatMinor`, `serviceFeeMinor`, `totalChargedMinor`, `currency`, `currencyDecimals`, `paymentMethod` = `"WALLET"`, `paymentState`, `breakdownEstimated`, `updatedAt`).
  - `FeedCursor.encode(Instant, UUID) -> String` (URL-safe base64 of `epochMicros:sessionId`) and `FeedCursor.decode(String) -> FeedCursor`; a tampered value -> 400 `PARTNER_CURSOR_INVALID`. `limit` outside 1..500 -> 400 `PARTNER_LIMIT_INVALID`; default 100.
- Rules: only sessions of linked customers with a `transaction_id`; `paymentState` is `PENDING` while no `session_charge` row exists and the session is live, otherwise the row's state; order by `(greatest(charging_session.updated_at, session_charge.updated_at), session_id)` ascending, strictly after the cursor pair; empty page with a cursor returns that same cursor and `hasMore=false`; empty first call returns `nextCursor: null`. Audit `PARTNER_FEED_READ` with item count only.

- [ ] **Step 1: Write the failing tests:** `firstCallWithoutCursorReadsFromTheBeginning`, `cursorContinuesWithoutRepeatOrSkip` (page size 2 over 5 sessions), `equalTimestampsNeitherRepeatNorSkip` (force identical `updated_at`), `unlinkedCustomerNeverAppears`, `unlinkStopsNewItemsForThatCustomer`, `sessionWithoutTransactionIdIsExcluded`, `liveSessionIsPending`, `settledSessionShowsBreakdownAndWalletMethod`, `failedStartThatReachedTheChargerIsNotCharged`, `lateSettlementReappearsWithANewerUpdatedAt`, `refundedStateIsRepresentedOnTheSameTransactionId` (set `session_charge.state='REFUNDED'` directly), `emptyPageWithCursorReturnsTheSameCursor`, `tamperedCursorIs400`, `limit0And501Are400`, `defaultLimitIs100`, `keyForAnotherCountryNeverSeesGaSessions`, `estimatedBackfillRowsAreFlagged`, `feedWithoutKeyIs401`.
- [ ] **Step 2: Run** the class. Expected: FAIL (404).
- [ ] **Step 3: Implement** `JdbcChargeFeed` with one keyset query (`where (ts, id) > (:ts, :id) and customer_id = any(:ids) and transaction_id is not null order by ts, id limit :limit + 1`); `SessionFeedService.read(String cursor, Integer limit)` decodes, calls the root interface and the idTag lookup once per page, and builds the next cursor from the last item.
- [ ] **Step 4: Run** the class. Expected: PASS.
- [ ] **Step 5: Report** (no commit).

---

### Task 9: Documentation, runbook and full verification

**Files:**
- Modify: `docs/INTEGRATION_CONTRACTS.md` (the partner contract: endpoints, fields, errors, cursor), `docs/DATA_MODEL.md` (`partner_api_key`, `partner_link`, `session_charge`), `docs/SECURITY_AND_RELIABILITY.md` (partner keys, personal data shared, rate limit, no IP allow-list), `docs/OPERATIONS_RUNBOOK.md` (create, rotate with two active keys, revoke; share the key through a password manager), `docs/NDZELA_INTEGRATION.md` (mark the sections now built)
- Test: whole backend suite

- [ ] **Step 1: Update the four docs** with the exact values from Tasks 1 to 8 (field names, error codes, defaults, migration numbers).
- [ ] **Step 2: Run** `cd backend && ./mvnw verify`. Expected: BUILD SUCCESS, including `ModuleStructureTest` and every new test class.
- [ ] **Step 3: Smoke test** locally: `docker compose up -d`, start the backend with `SPRING_PROFILES_ACTIVE=dev`, create a key through the admin endpoint, then `curl -H "X-Api-Key: <key>" -X POST localhost:8090/v1/partners/ndzela/id-tags/lookup -H 'Content-Type: application/json' -d '{"drivers":[{"ref":"d1","email":"x@y.com","phoneE164":"+24177000000"}]}'`. Expected: 200 with an empty `matches` for an unknown driver; the same call without the header: 401 `PARTNER_KEY_INVALID`.
- [ ] **Step 4: Report** to the user: results, the list of files changed, and the steps to create the production key (the user commits, deploys to Railway and shares the key).

---

## Self-Review

- **Spec coverage:** keys/rotation/revoke/audit (1, 3), tenant binding and rate limit (2), lookup with FULL/PARTIAL and no names on partial (5), links (6), `session_charge` plus backfill plus estimated flag (7), feed with cursor, backfill, refund state, only linked customers (8), docs and runbook (9), no webhook (non-goal), `REFUNDED` reserved (8 test sets it directly). Open item not coded: the written terms confirmation (user).
- **Types:** `PartnerPrincipal`, `PartnerKeys`, `CustomerContacts`, `CustomerIdTags`, `ChargeFeed`, `LinkStore.linkedCustomerIds` are defined before use; names match across tasks.
- **Proportion:** nine tasks for one new module plus one table; bodies are left to the implementer, tests carry the exact names and assertions.
