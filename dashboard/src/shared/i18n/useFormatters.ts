"use client";

import { useMemo } from "react";
import { formatDate, formatDateTime, formatMoneyLines, formatNumber } from "@/shared/lib/format";
import type { CurrencyDisplay, MoneyAmount } from "@/shared/lib/format";
import { useI18n } from "./I18nProvider";

const INTL_LOCALES = { fr: "fr-FR", en: "en-US" } as const;
const FALLBACK_CURRENCY = "XAF";

export function useFormatters() {
  const { locale } = useI18n();
  const intl = INTL_LOCALES[locale];

  return useMemo(
    () => ({
      money: (amounts: MoneyAmount[], display?: CurrencyDisplay) =>
        formatMoneyLines(amounts, intl, FALLBACK_CURRENCY, display),
      number: (value: number, fractionDigits = 0) => formatNumber(value, intl, fractionDigits),
      date: (iso: string) => formatDate(iso, intl),
      dateTime: (iso: string) => formatDateTime(iso, intl),
    }),
    [intl],
  );
}
