"use client";

import { useSeries } from "@/features/overview/api/useSeries";
import { periodsByCurrency } from "@/features/overview/seriesMath";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { MetricBand } from "@/shared/ui/MetricBand";
import { MetricCard } from "@/shared/ui/MetricCard";
import { collectionFigures } from "../collectionFigures";

const MONTH_DAYS = 31;

export function CollectionBand() {
  const { t } = useI18n();
  const format = useFormatters();
  const { country } = useSession();
  const series = useSeries(country, MONTH_DAYS);
  const periods = series.data ? periodsByCurrency(series.data, MONTH_DAYS) : [];
  const period = periods.find((item) => item.current.sessions > 0) ?? periods[0];

  if (!period) {
    return null;
  }

  const labels = t.sessions.collection;
  const figures = collectionFigures(period.daily, period.today);
  const money = (minor: number) => format.money([{ currency: period.currency, decimals: period.decimals, minor }])[0];

  return (
    <MetricBand columns={5}>
      <MetricCard
        label={labels.charges}
        icon="bolt"
        value={format.number(figures.today.sessions)}
        hint={interpolate(labels.chargesHint, { completed: format.number(figures.today.completed), failed: format.number(figures.today.failed) })}
      />
      <MetricCard label={labels.today} icon="chart" value={money(figures.today.capturedMinor)} hint={labels.todayHint} />
      <MetricCard label={labels.yesterday} icon="chart" value={money(figures.yesterdayMinor)} hint={labels.yesterdayHint} />
      <MetricCard label={labels.week} icon="chart" value={money(figures.weekMinor)} hint={labels.weekHint} />
      <MetricCard label={labels.month} icon="chart" value={money(figures.monthMinor)} hint={labels.monthHint} />
    </MetricBand>
  );
}
