"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { LineChart } from "@/shared/ui/charts/LineChart";
import { Card } from "@/shared/ui/Card";
import { interpolate } from "@/shared/i18n/interpolate";
import { TrendPill } from "@/shared/ui/TrendPill";
import { deltaPercent } from "../seriesMath";
import type { CurrencyPeriod } from "../seriesMath";

const SALES = "var(--color-series-1)";
const COMMISSION = "var(--color-series-2)";

export function useDayLabel() {
  const { locale } = useI18n();
  return (iso: string) => new Intl.DateTimeFormat(locale, { day: "numeric", month: "short", timeZone: "UTC" }).format(new Date(`${iso}T00:00:00Z`));
}

export function RevenueChart({ period, days }: { period: CurrencyPeriod; days: number }) {
  const { t, locale } = useI18n();
  const format = useFormatters();
  const dayLabel = useDayLabel();
  const labels = t.overview.charts;
  const scale = 10 ** period.decimals;
  const money = (minor: number) => format.money([{ currency: period.currency, decimals: period.decimals, minor }])[0];

  return (
    <Card title={labels.revenueTitle} description={labels.revenueText}>
      <div className="mb-4 flex flex-col gap-2">
        <p className="figure text-3xl font-semibold text-foreground">{money(period.current.capturedMinor)}</p>
        <TrendPill
          trend={{
            delta: deltaPercent(period.current.capturedMinor, period.previous.capturedMinor),
            label: interpolate(t.overview.kpi.versus, { days }),
            newLabel: t.overview.kpi.new,
          }}
        />
      </div>
      <LineChart
        label={labels.revenueTitle}
        locale={locale}
        series={[
          { name: labels.sales, color: SALES },
          { name: labels.commission, color: COMMISSION },
        ]}
        points={period.daily.map((day) => ({ label: dayLabel(day.day), values: [day.capturedMinor / scale, day.serviceFeeMinor / scale] }))}
        format={(value) => money(Math.round(value * scale))}
      />
    </Card>
  );
}
