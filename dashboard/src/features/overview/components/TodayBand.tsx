"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { MetricBand } from "@/shared/ui/MetricBand";
import { MetricCard } from "@/shared/ui/MetricCard";
import { deltaPercent } from "../seriesMath";
import type { CurrencyPeriod } from "../seriesMath";
import { todayFigures } from "../todayFigures";

export function TodayBand({ period }: { period: CurrencyPeriod }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.overview.today;
  const { today, yesterday, week } = todayFigures(period);
  const money = (minor: number) => format.money([{ currency: period.currency, decimals: period.decimals, minor }])[0];
  const versus = { label: labels.versus, newLabel: t.overview.kpi.new };

  return (
    <MetricBand columns={3}>
      <MetricCard
        label={labels.collected}
        icon="chart"
        value={money(today.capturedMinor)}
        hint={labels.collectedHint}
        spark={week.map((day) => day.capturedMinor)}
        trend={{ ...versus, delta: deltaPercent(today.capturedMinor, yesterday?.capturedMinor ?? 0) }}
      />
      <MetricCard
        label={labels.charges}
        icon="bolt"
        value={format.number(today.sessions)}
        hint={interpolate(labels.chargesHint, { completed: format.number(today.completed), failed: format.number(today.failed) })}
        spark={week.map((day) => day.sessions)}
        trend={{ ...versus, delta: deltaPercent(today.sessions, yesterday?.sessions ?? 0) }}
      />
      <MetricCard
        label={labels.energy}
        icon="station"
        value={`${format.number(today.energyKwh, 1)} ${t.overview.unitKwh}`}
        hint={labels.energyHint}
        spark={week.map((day) => day.energyKwh)}
        trend={{ ...versus, delta: deltaPercent(today.energyKwh, yesterday?.energyKwh ?? 0) }}
      />
    </MetricBand>
  );
}
