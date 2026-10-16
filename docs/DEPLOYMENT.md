# Staging deployment

## Railway (in use)

Project `ndzela-staging`, environment `production`, three services:

| Service | What it is | Public |
|---|---|---|
| `backend` | Built from `users/backend/Dockerfile`, configured by `users/backend/railway.json` | `https://backend-production-d2d1.up.railway.app` |
| `Postgres` | Railway Postgres plugin, reached over the private network | no |
| `maildev` | Inbox for verification emails, basic auth (`MAILDEV_WEB_USER`, `MAILDEV_WEB_PASS`) | yes, port 1080 |

Redeploy after a code change: `cd users/backend && railway up -s backend --ci`.
Variables live in Railway, not in the repository: `railway variables -s backend`.

To connect the OCPP platform once they reply, set both and redeploy:

```bash
railway variables set -s backend NDZELA_OCPP_BASE_URL=https://... NDZELA_OCPP_API_KEY=...
```

Outbound IP: Railway services share changing outbound addresses unless the project has the
static outbound IP feature (paid plan). If the OCPP team enforces an IP allowlist on our
client, either enable that feature and give them the address, or let them issue the staging
key without an allowlist while request signing stays on.

The VPS instructions below are the alternative and were not used.

Goal: give the OCPP team a public HTTPS URL for the Ndzela backend so they can issue our
partner access (API key, IP allowlist, station ownership). Everything below was built and
the container was run locally; **it has not been deployed to a real server yet**.

## What you need

| Item | Notes |
|---|---|
| A Linux VPS (2 GB RAM is enough) | Docker and the compose plugin installed, ports 80 and 443 open |
| A domain or subdomain | An `A` record pointing at the VPS, for example `api.ndzela.example` |
| OCPP base URL and API key | Given by the OCPP team after they receive our URL |

## Deploy

```bash
git clone <repo> && cd <repo>/users/deploy
cp staging.env.example staging.env
```

Fill `staging.env`. Generate the secrets with `openssl rand -hex 32` (JWT secret,
code pepper, database password). Until the OCPP team has replied, `NDZELA_OCPP_BASE_URL`
and `NDZELA_OCPP_API_KEY` can hold placeholders: the backend starts, and catalog and
charging calls fail with `CATALOG_UNAVAILABLE` or `CHARGING_UNAVAILABLE` until real values
are set.

```bash
docker compose -f docker-compose.staging.yml --env-file staging.env up -d --build
curl https://$DOMAIN/actuator/health
```

Caddy obtains the TLS certificate automatically on first request. The expected answer is
`{"groups":["liveness","readiness"],"status":"UP"}`.

After the OCPP team replies, edit `staging.env` and run the same `up -d` command again.

## What to send the OCPP team

1. Base URL: `https://<DOMAIN>`
2. The VPS public IPv4 address (`curl -4 ifconfig.me` on the server). Their API keys
   carry an IP allowlist, so this is the address they must allow for our client.
3. The access we need: a partner client with scopes READ, CHARGING_COMMANDS and
   ID_TAGS_WRITE, the ownership of our test charger assigned to that client, and the
   base URL of their environment.

We do not receive webhooks yet. The backend polls and reconciles, so no callback URL is
needed for now.

## Behaviour of this staging build

* Top-ups use the **sandbox payment provider**: they succeed instantly and no money moves.
  Anyone who can register can credit a wallet. Do not announce the URL publicly.
* Verification emails go to a Maildev inbox that is only reachable from the server:
  `ssh -L 1080:localhost:1080 user@server`, then open `http://localhost:1080`.
  Phone OTP codes are written to the backend log: `docker compose logs backend`.
* The price per kWh used for the charge limit is empty unless `NDZELA_PRICE_PER_KWH` is set.
  Without it, a charge is not stopped when the held amount is consumed.
* The database lives in the `ndzela-pg` volume. Back it up before any rebuild that you
  are not sure about.

## Before real customers

Replace the sandbox provider with the PVit adapter, an SMS and email provider, and a
production secret store. Those are not part of this staging build.
