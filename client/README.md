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
| `NEXT_PUBLIC_APP_STORE_URL` | App Store link (button shows "Coming soon" while empty) |
| `NEXT_PUBLIC_PLAY_STORE_URL` | Google Play link (same behaviour) |

Store listings use `/<lang>/privacy`, `/<lang>/terms` and `/<lang>/delete-account`.
Set the backend `ZELLA_AGREEMENT_URL` to the terms page so the app links to it.

## App screenshots

The home page has 7 screenshot slots: `1` in the hero and `2` to `7`, one beside each feature in order (find a
charger, scan, live tracking, wallet, notifications, security). Drop PNG files named `public/screens/1.png` to
`public/screens/7.png` (portrait phone screenshots, about 1170 x 2532) and rebuild. A slot without a file stays an
empty frame.
