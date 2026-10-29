"use client";

import { countryLabel } from "@/features/session/countryScope";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { ErrorState } from "@/shared/ui/ErrorState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { useOverview } from "../api/useOverview";
import { useSeries } from "../api/useSeries";
import { periodsByCurrency } from "../seriesMath";
import { overviewTotals } from "../totals";
import { ActivityCharts } from "./ActivityCharts";
import { AttentionStrip } from "./AttentionStrip";
import { CountryBreakdown } from "./CountryBreakdown";
import { KpiGrid } from "./KpiGrid";
import { MoneyCard } from "./MoneyCard";
import { OverviewSkeleton } from "./OverviewSkeleton";
import { PERIODS, PeriodSwitch } from "./PeriodSwitch";
import { RecentActivity } from "./RecentActivity";

export function OverviewPage() {
  const { t } = useI18n();
  const { profile, country } = useSession();
  const format = useFormatters();
  const { params, update } = useUrlParams();
  const requested = Number(params.get("days"));
  const days = (PERIODS as readonly number[]).includes(requested) ? requested : 30;
  const overview = useOverview(country);
  const series = useSeries(country, days);
  const scope = countryLabel(country, profile.countries, t.shell.country.all);
  const periods = series.data ? periodsByCurrency(series.data, days).filter((item) => item.current.sessions + item.current.topupCount + item.previous.sessions + item.previous.topupCount > 0) : [];
  const primary = periods[0] ?? (series.data ? periodsByCurrency(series.data, days)[0] : undefined);

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={t.overview.title}
        subtitle={interpolate(t.overview.subtitle, { scope })}
        trail={[t.shell.breadcrumbRoot, t.overview.title]}
        actions={<PeriodSwitch days={days} onChange={(value) => update({ days: value === 30 ? null : String(value) }, false)} />}
      />
      {(overview.isPending || series.isPending) && (
        <div role="status" aria-busy="true">
          <span className="sr-only">{t.common.loading}</span>
          <OverviewSkeleton />
        </div>
      )}
      {(overview.isError || series.isError) && <ErrorState message={t.overview.loadError} onRetry={() => void overview.refetch()} />}
      {overview.data && primary && (
        <>
          {overview.data.since && (
            <p className="rounded-control border border-border bg-surface px-4 py-3 text-sm text-muted">
              {interpolate(t.overview.since, { date: format.date(overview.data.since) })}
            </p>
          )}
          <AttentionStrip totals={overviewTotals(overview.data.countries)} />
          <KpiGrid period={primary} days={days} />
          <div className="grid items-start gap-6 xl:grid-cols-3">
            <div className="xl:col-span-2">
              <ActivityCharts period={primary} />
            </div>
            <MoneyCard totals={overviewTotals(overview.data.countries)} />
          </div>
          {periods.length > 1 && <p className="text-sm text-muted">{t.overview.mixedCurrencies}</p>}
          <RecentActivity />
          {overview.data.countries.length > 1 && <CountryBreakdown countries={overview.data.countries} />}
        </>
      )}
    </div>
  );
}
