export const locales = ["fr", "en"] as const;
export type Locale = (typeof locales)[number];

export const isLocale = (value: string): value is Locale =>
  (locales as readonly string[]).includes(value);

export const site = {
  name: "Zella Charging",
  url: process.env.NEXT_PUBLIC_SITE_URL ?? "https://zella.example",
  supportEmail: process.env.NEXT_PUBLIC_SUPPORT_EMAIL ?? "support@zella.example",
  supportPhone: process.env.NEXT_PUBLIC_SUPPORT_PHONE ?? "",
  legalName: process.env.NEXT_PUBLIC_LEGAL_NAME ?? "Zella Charging",
  appStoreUrl: process.env.NEXT_PUBLIC_APP_STORE_URL ?? "",
  playStoreUrl: process.env.NEXT_PUBLIC_PLAY_STORE_URL ?? "",
  copyrightYear: 2026,
  updated: "2026-10-30",
};
