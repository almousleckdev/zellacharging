"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import type { CurrencySeries } from "@/features/overview/seriesMath";
import { isMoney } from "./reportColumns";
import type { ReportColumnKey } from "./reportColumns";

export function useReportFormat(group: CurrencySeries | undefined, bucket: string) {
  const { t, locale } = useI18n();
  const format = useFormatters();

  const value = (key: ReportColumnKey, amount: number) => {
    if (isMoney(key)) {
      return format.money([{ currency: group?.currency ?? "XAF", decimals: group?.decimals ?? 0, minor: amount }])[0];
    }
    return key === "energyKwh" ? `${format.number(amount, 1)} ${t.overview.unitKwh}` : format.number(amount);
  };

  const period = (day: string) => {
    const date = new Date(`${day}T00:00:00Z`);
    const options: Intl.DateTimeFormatOptions =
      bucket === "month" ? { month: "long", year: "numeric", timeZone: "UTC" } : { dateStyle: "medium", timeZone: "UTC" };
    const label = new Intl.DateTimeFormat(locale, options).format(date);
    return bucket === "week" ? `${t.reports.weekOf} ${label}` : label;
  };

  return { value, period, labels: t.reports.columns as Record<string, string> };
}
