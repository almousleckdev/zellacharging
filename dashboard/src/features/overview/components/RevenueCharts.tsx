"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { BarChart } from "@/shared/ui/charts/BarChart";
import { LineChart } from "@/shared/ui/charts/LineChart";
import { Card } from "@/shared/ui/Card";
import type { CurrencyPeriod } from "../seriesMath";

const SALES = "var(--color-series-1)";
const COMMISSION = "var(--color-series-2)";

export function useDayLabel() {
  const { locale } = useI18n();
  return (iso: string) => new Intl.DateTimeFormat(locale, { day: "numeric", month: "short", timeZone: "UTC" }).format(new Date(`${iso}T00:00:00Z`));
}

export function RevenueChart({ period }: { period: CurrencyPeriod }) {
  const { t, locale } = useI18n();
  const format = useFormatters();
  const dayLabel = useDayLabel();
  const labels = t.overview.charts;
  const scale = 10 ** period.decimals;

  return (
    <Card title={labels.revenueTitle} description={labels.revenueText}>
      <LineChart
        label={labels.revenueTitle}
        locale={locale}
        series={[
          { name: labels.sales, color: SALES },
          { name: labels.commission, color: COMMISSION },
        ]}
        points={period.daily.map((day) => ({ label: dayLabel(day.day), values: [day.capturedMinor / scale, day.serviceFeeMinor / scale] }))}
        format={(value) => format.money([{ currency: period.currency, decimals: period.decimals, minor: Math.round(value * scale) }])[0]}
      />
    </Card>
  );
}

export function ActivityBars({ period, metric }: { period: CurrencyPeriod; metric: "sessions" | "energy" }) {
  const { t, locale } = useI18n();
  const format = useFormatters();
  const dayLabel = useDayLabel();
  const labels = t.overview.charts;
  const energy = metric === "energy";

  return (
    <Card title={energy ? labels.energyTitle : labels.chargesTitle} description={energy ? labels.energyText : labels.chargesText}>
      <BarChart
        label={energy ? labels.energyTitle : labels.chargesTitle}
        locale={locale}
        series={{ name: energy ? labels.energy : labels.charges, color: energy ? COMMISSION : SALES }}
        points={period.daily.map((day) => ({ label: dayLabel(day.day), values: [energy ? day.energyKwh : day.sessions] }))}
        format={(value) => (energy ? `${format.number(value, 1)} ${t.overview.unitKwh}` : format.number(value))}
      />
    </Card>
  );
}
