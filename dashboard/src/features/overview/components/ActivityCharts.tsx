"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { BarChart } from "@/shared/ui/charts/BarChart";
import { LineChart } from "@/shared/ui/charts/LineChart";
import { Card } from "@/shared/ui/Card";
import type { CurrencyPeriod } from "../seriesMath";

const SERIES_1 = "var(--color-series-1)";
const SERIES_2 = "var(--color-series-2)";
const SERIES_3 = "var(--color-series-3)";

export function ActivityCharts({ period }: { period: CurrencyPeriod }) {
  const { t, locale } = useI18n();
  const format = useFormatters();
  const labels = t.overview.charts;
  const dayLabel = (iso: string) => new Intl.DateTimeFormat(locale, { day: "numeric", month: "short", timeZone: "UTC" }).format(new Date(`${iso}T00:00:00Z`));
  const money = (minor: number) => format.money([{ currency: period.currency, decimals: period.decimals, minor }])[0];
  const scale = 10 ** period.decimals;

  return (
    <div className="grid gap-6 xl:grid-cols-2">
      <div className="xl:col-span-2">
        <Card title={labels.moneyTitle} description={labels.moneyText}>
          <LineChart
            label={labels.moneyTitle}
            locale={locale}
            series={[
              { name: labels.topups, color: SERIES_2 },
              { name: labels.sales, color: SERIES_1 },
              { name: labels.withdrawals, color: SERIES_3 },
            ]}
            points={period.daily.map((day) => ({
              label: dayLabel(day.day),
              values: [day.topupsMinor / scale, day.capturedMinor / scale, day.withdrawalsMinor / scale],
            }))}
            format={(value) => money(Math.round(value * scale))}
          />
        </Card>
      </div>
      <Card title={labels.sessionsTitle} description={labels.sessionsText}>
        <BarChart
          label={labels.sessionsTitle}
          locale={locale}
          series={{ name: labels.sessions, color: SERIES_1 }}
          points={period.daily.map((day) => ({ label: dayLabel(day.day), values: [day.sessions] }))}
          format={(value) => format.number(value)}
        />
      </Card>
      <Card title={labels.energyTitle} description={labels.energyText}>
        <BarChart
          label={labels.energyTitle}
          locale={locale}
          series={{ name: labels.energy, color: SERIES_2 }}
          points={period.daily.map((day) => ({ label: dayLabel(day.day), values: [day.energyKwh] }))}
          format={(value) => `${format.number(value, 1)} ${t.overview.unitKwh}`}
        />
      </Card>
    </div>
  );
}
