# Zella Charging website

Public landing page and legal pages (privacy, terms, account deletion, contact), French and English.

```bash
pnpm install
pnpm dev      # http://localhost:3000
pnpm build
```

Set these variables at build time (all optional, placeholders are used otherwise):

| Variable | Purpose |
|---|---|
| `NEXT_PUBLIC_SITE_URL` | Public URL, used by the sitemap and metadata |
| `NEXT_PUBLIC_CONTACT_EMAIL`, `NEXT_PUBLIC_TEAM_EMAIL`, `NEXT_PUBLIC_PRIVACY_EMAIL`, `NEXT_PUBLIC_PHONE` | Contact page values (defaults are the Zella ones) |
| `NEXT_PUBLIC_LEGAL_NAME` | Publisher name shown in the footer |
| `NEXT_PUBLIC_APP_STORE_URL` | App Store link (defaults to the published Zella listing; button shows "Coming soon" if set empty) |
| `NEXT_PUBLIC_PLAY_STORE_URL` | Google Play link (same behaviour) |

Store listings use `/<lang>/privacy`, `/<lang>/terms` and `/<lang>/delete-account`.
Set the backend `ZELLA_AGREEMENT_URL` to the terms page so the app links to it.

## App screenshots

The hero uses `public/screens/hero.png` (a transparent phone mockup). The six feature sections each have a portrait
slot: drop PNG files named `public/screens/2.png` to `7.png` (find a charger, scan, live tracking, wallet,
notifications, security; about 1170 x 2532) and rebuild. A slot without a file stays an empty frame.

## Contact form

The form on `/<lang>/contact` posts from the server to the backend `POST /v1/contact`, which emails
`ZELLA_CONTACT_INBOX` (set it on the backend) with the visitor as reply-to. Set `API_BASE_URL` on the website
(defaults to the production API). The endpoint is rate limited to 5 messages per IP per hour.
