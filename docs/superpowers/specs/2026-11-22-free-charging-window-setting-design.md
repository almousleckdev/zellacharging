# Free charging window as a dashboard setting

Status: short design for approval, 2026-11-22. Amends `2026-11-22-free-sunday-charging-design.md` (rules 3 and 6) and NDZela ADR-0048.

## Intent and why

The owner wants the free charging time to be 00:00 to 23:59 (the whole day) instead of Sunday 12:00 to 22:00, and wants a place in the dashboards to manage the day and the times, so a code change or an environment variable is never needed again.

## Decisions (owner)

- One setting per country, shared by all free-charging drivers (not per agreement).
- Editable in both dashboards: the NDZela dashboard and the Zella dashboard (the Zella page forwards to NDZela, as for the agreements).
- Default for Gabon: Sunday, from 00:00, until the end of the day (23:59).

## Calls I made (the owner may change them)

- **What is configurable:** the day of the week, the start time and the end time. The end time can be "end of the day". The dashboards show it as 23:59 and the system stores it as "no end time" (midnight at the end of that day).
- **End rule is unchanged:** the free session stops at the end time (22:00 sharp as before, or midnight with the whole-day default). A session started at 23:50 stops at midnight.
- **A change applies to new starts at once.** A free session already running keeps the end time recorded when it started (the claim's window end), so a change never shortens or lengthens a running session. A Sunday already claimed stays claimed.
- **Where it lives:** NDZela owns it, in the country settings (new columns, new migration). NDZela's eligibility check reads it. The old environment variables (`app.free-charging.*`) become only the default when a country has no setting.
- **Permission:** `free_charging:manage` edits it and `free_charging:read` shows it. No new permission.
- **Audit:** every change is audited (`FREE_WINDOW_CHANGED`) with the old and new values, on both sides (NDZela, and Zella for changes made from its dashboard).
- **Display:** the eligibility answer carries the window (day, from, until). Zella passes it to the app, and the app and both dashboards build their texts from it. No more hard-coded "12h-22h" anywhere.
- **Validation:** start before end unless the end is "end of the day"; times in whole minutes; the day is one of the seven weekdays.

## Parts

1. NDZela backend: country setting, validation, read/update API for staff and for Zella, eligibility reads it and returns the window, audit, tests, docs and ADR amendment.
2. Zella backend: pass the window through `GET /v1/free-sunday`, forward the setting read and update from the Zella admin API (permission, country scope, audit), tests.
3. Dashboards: a "Créneau gratuit" card on the Gratuité page in both dashboards (day, start, end or end of day, save, shows the current values); labels built from the setting.
4. App: texts built from the window returned by the server (explanation line, "available on ..." text, closing notice at midnight shown as 23:59 / minuit).

## Non-goals

Per-driver windows, several windows per week, holiday rules.

## Risks

A wrong setting (for example a very short window) silently removes the benefit: the dashboard shows the current window and the audit trail shows who changed it. A whole-day window means free sessions can run until midnight: the existing 22:00-style stop covers it at the configured end.
