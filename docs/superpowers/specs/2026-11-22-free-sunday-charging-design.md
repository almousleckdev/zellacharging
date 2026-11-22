# Free Sunday charging for selected NDZela drivers

Status: draft for approval, 2026-11-22. Touches Zella (`backend/`, `dashboard/`, `mobile/`) and NDZela (`/Users/mac/Desktop/ndzela`, owner allowed the changes). The OCPP platform is not touched.

## 1. Intent

NDZela rents cars to drivers who charge through the Zella app. The company wants to reward some drivers who are fully paid up with one free charge every Sunday. It is an individual privilege that administrators grant, pause and withdraw. It is not a rule for all drivers.

## 2. Business rules (decided with the owner)

1. Only drivers with an **active free-charging agreement** qualify.
2. The benefit is **one free session per Sunday**, never more, even with concurrent requests.
3. A session may **start** on Sunday from 12:00 to 22:00, Gabon time (Africa/Libreville). Saturday's rent is due Sunday 12:00, so at 12:00 a paid-up driver has zero unpaid days.
4. The driver must have **zero unpaid days at the moment the session starts**. One unpaid day or more removes the benefit until everything is paid.
5. The free session may start from any battery level and charges up to 100%. There is no kWh or money cap.
6. The free session **stops at 22:00 sharp** even if the car is not full. After 22:00 there is no free charging.
7. A free session that the driver stops early **uses up the Sunday**. Afterwards the driver pays from the wallet as usual.
8. A session that never starts, or starts and delivers no energy, **releases the Sunday**.
9. A driver with **3 or more unpaid days** is suspended in both apps and can use only the payment screen in the NDZela app. This is already built (NDZela ADR-0013 and ADR-0033). It is verified end to end here, not rebuilt.

## 3. Goals, non-goals, anti-goals

Goals: administrators manage agreements from both dashboards. The backend is the only judge of eligibility. A second use of the same Sunday is technically impossible. Every administrative action and every use is audited.

Non-goals: other weekdays, several free sessions per Sunday, energy caps, automatic enrolment, SMS notices, free access for non-drivers (staff free charging already exists as `customer_complimentary`).

Anti-goals: free charging for everybody; a driver silently losing their Sunday because a start failed; eligibility rules copied into the dashboards or the app; a second rules engine in Zella.

## 4. Constraints from the existing projects

- Zella rules: no comments in code, files under about 150 lines, one public type per file, clean architecture (`web -> application -> domain`), money as `long` minor units, migrations never edited, stable API error codes, FR and EN strings in `core/l10n/strings.dart`, `AppColors` only, no commit.
- NDZela rules: read the docs first, update the docs and add an ADR in the same change, every tenant-owned table carries `country_id` with an isolation test, files under about 200 lines, no unneeded abstractions.
- NDZela owns drivers, contracts and unpaid days (derived by `AccountCalculator`, exposed through `DriverArrearsService`). Zella owns the wallet and the charging (`FundingService.reserve` already supports a `waived` hold). The two talk through shared-key partner endpoints and the `ZellaCommand` outbox.
- Zella knows only whether a customer is suspended, not how many days are unpaid.

## 5. Approaches considered

- **A. NDZela owns agreements and rules, Zella enforces (chosen).** One rules engine and one set of data. The Zella dashboard forwards to NDZela. Cost: free charging needs NDZela to be reachable at start.
- **B. Zella owns the program and NDZela pushes unpaid days.** Works if NDZela is down, but it copies data that goes stale and moves NDZela's contract rules into Zella. Rejected.
- **C. Each dashboard keeps its own list.** Duplicated data and guaranteed disagreement. Rejected.

## 6. Design

### 6.1 NDZela (source of truth)

- **Table `free_charging_agreements`** (country-owned): driver, optional rental contract, `starts_on`, `ends_on`, status `ACTIVE | SUSPENDED | REVOKED`, free-text conditions, who and when. At most one non-revoked agreement per driver (partial unique index). Revoking keeps the row, so history survives. Removing from the program means REVOKED.
- **Eligibility service** (one class, pure rule plus the existing arrears reader). Inputs: agreement, driver status, linked Zella customer, unpaid days, `now`. Output: `eligible` plus one reason code from `NO_AGREEMENT`, `AGREEMENT_SUSPENDED`, `AGREEMENT_NOT_STARTED`, `AGREEMENT_ENDED`, `OUTSIDE_WINDOW`, `UNPAID_DAYS`, `DRIVER_SUSPENDED`, `NOT_LINKED`. The window (Sunday, 12:00, 22:00, country time zone) lives in one configuration object.
- **Read endpoint for Zella:** `GET /api/v1/partners/zella/customers/{customerId}/free-charging`, same shared-key pattern as ADR-0040. It answers `eligible`, `reason`, `agreementId` and `windowEndsAt`. It is rate limited.
- **Management API for staff** (new permissions `free_charging:read` and `free_charging:manage`): list agreements with eligibility and unpaid days, create, edit, suspend, resume, revoke, and a usage history built from sessions marked `FREE_SUNDAY`.
- **Management API for the Zella dashboard:** the same operations under the partner prefix and the shared key, with the acting Zella administrator passed along and recorded in the NDZela audit trail.
- Adding a driver requires a linked Zella customer, using the existing e-mail and phone matching.
- **Audit:** `FREE_AGREEMENT_CREATED`, `UPDATED`, `SUSPENDED`, `RESUMED`, `REVOKED`.
- **Feed:** the `freeCategory` values gain `FREE_SUNDAY`, shown as "Offert" (ADR-0044).

### 6.2 Zella (enforcement)

- **Table `free_sunday_claim`**: country, customer, Sunday date, session id, agreement id, state `CLAIMED | RELEASED`, timestamps. A **unique key on (customer, Sunday date)** and, since the final review, a second one on **(agreement, Sunday date)** (Zella V45; one driver can have two linked Zella customers) over non-released rows are the final guarantee against a second use. Residual: revoking and recreating an agreement the same Sunday gives a new agreement id (audited administrator action).
- **Start flow:** inside the transaction that already locks the wallet and reserves funds, a new `FreeSundayGrant` checks the window locally, asks NDZela through an application port (HTTP client with a short timeout), and, if eligible, inserts the claim and reserves with `waived = true`. A unique-key conflict means the Sunday is already used and the start continues as an ordinary paid charge. If NDZela is unreachable, the free benefit is refused and the start continues as an ordinary paid charge. Paid charging is never blocked by this feature.
- **Releasing:** when the session fails to start or closes with no energy, the claim becomes `RELEASED`. An early stop with energy delivered keeps it `CLAIMED`.
- **22:00 stop:** a sweeper step stops every session with a `CLAIMED` claim once its window ended, through the existing stop path, with stop reason `FREE_WINDOW_ENDED`. It retries on each cycle until the stop is accepted.
- **Quote and wallet responses** gain `freeSunday { available, endsAt, reason }`, computed by the same grant service, so the app shows what the start will do.
- **Admin API** `/v1/admin/free-charging/...`: a thin forwarder to NDZela plus the Zella usage history from claims. New admin permissions follow the V39 pattern.
- **Audit:** `FREE_SUNDAY_CLAIMED`, `RELEASED`, `WINDOW_STOPPED`, and one entry per forwarded administrative action.
- **Complimentary access** (`customer_complimentary`) is not reused. It is all-or-nothing.

### 6.3 Dashboards

Both get a page "Gratuité" (FR and EN) with the same behaviour: a table (driver, agreement dates, status, eligibility and reason, unpaid days, last free use), add driver, edit, suspend or resume, revoke, and a usage history tab. The NDZela page calls its own backend. The Zella page calls its own backend, which forwards. Neither page contains a rule. Both hide buttons by permission, and the backend enforces the permission.

### 6.4 Mobile (Zella app)

Shows a "Free Sunday charge" badge with the end time when `freeSunday.available`, a notice when the window is about to close, and the reason in plain words when it is not available. All texts come from reason codes in `strings.dart`, FR and EN. It contains no rule.

## 7. Failure behaviour

| Situation | Result |
| --- | --- |
| NDZela down at start | Free refused, normal paid charge possible |
| Two starts at once for the same Sunday | One free, the other paid or refused for funds |
| Charge fails to start | Claim released |
| Driver stops early | Sunday stays used |
| Stop command not accepted at 22:00 | Retried every sweep cycle, alert logged |
| Driver reaches 1 or more unpaid days | Not eligible; at 3 or more suspended in both apps |

## 8. Tests required

Concurrent starts for one Sunday; window edges (11:59:59, 12:00:00, 21:59:59, 22:00:00); unpaid-day eligibility; fail-closed on NDZela timeout; release on failed start; 22:00 stop and retry; tenant isolation in NDZela and row-level security in Zella; permission checks; audit rows; FR and EN texts; the existing 3-unpaid-day lock checked end to end.

## 9. Documents to update in the same change

NDZela: a new ADR, `payments-and-arrears.md`, `ocpp-integration.md`, permission catalogue. Zella: `NDZELA_INTEGRATION.md`, `INTEGRATION_CONTRACTS.md`, `CHARGING_WORKFLOW.md`.

## 10. Risks the owner should know

- If a charger ignores the 22:00 stop command, energy keeps flowing for free until it obeys. The OCPP quota is 60 requests per minute.
- A driver can still lend their Zella account to a friend; the same caveat as ADR-0033.
- A free session costs the company the electricity. No wallet or commission entry is created for it.

## 11. Order of work

1. Zella enforcement (claim, grant, stop, quote field, tests).
2. NDZela agreements, eligibility endpoint, management API, ADR.
3. Both dashboard pages.
4. Mobile display.

## 12. Left to the builder

Class and column names, exact migration numbers (Zella next is V42, NDZela next is V72), page layout and table filters, the retry interval of the 22:00 stop, and the partner key rotation procedure.

## 13. Calls I made that the owner may change

- Removing from the program is a REVOKED status, not a deleted row.
- The Zella dashboard forwards to NDZela and keeps no copy.
- On an NDZela outage the free benefit is refused and the paid path continues.
- The window and Sunday rule sit in one NDZela configuration object, not in the apps.
