"use client";

import { countryLabel } from "@/features/session/countryScope";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { ErrorState } from "@/shared/ui/ErrorState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { useOverview } from "../api/useOverview";
import { useSeries } from "../api/useSeries";
import { periodsByCurrency } from "../seriesMath";
import { overviewTotals } from "../totals";
import { AttentionStrip } from "./AttentionStrip";
import { CountryRevenue } from "./CountryRevenue";
import { NetworkHealth } from "./NetworkHealth";
import { OverviewSkeleton } from "./OverviewSkeleton";
import { PERIODS, PeriodSwitch } from "./PeriodSwitch";
import { RecentCharges } from "./RecentCharges";
import { ActivityBars, RevenueChart } from "./RevenueCharts";
import { RevenueKpis } from "./RevenueKpis";

export function OverviewPage() {
  const { t } = useI18n();
  const { profile, country } = useSession();
  const { params, update } = useUrlParams();
  const requested = Number(params.get("days"));
  const days = (PERIODS as readonly number[]).includes(requested) ? requested : 30;
  const overview = useOverview(country);
  const series = useSeries(country, days);
  const scope = countryLabel(country, profile.countries, t.shell.country.all);
  const periods = series.data ? periodsByCurrency(series.data, days) : [];
  const active = periods.filter((item) => item.current.sessions + item.previous.sessions + item.current.topupCount > 0);
  const primary = active[0] ?? periods[0];

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
      {overview.data && series.data && primary && (
        <>
          <AttentionStrip totals={overviewTotals(overview.data.countries)} />
          <RevenueKpis period={primary} days={days} />
          <div className="grid items-start gap-6 xl:grid-cols-3">
            <div className="xl:col-span-2">
              <RevenueChart period={primary} />
            </div>
            <CountryRevenue series={series.data} days={days} />
          </div>
          <div className="grid items-start gap-6 xl:grid-cols-3">
            <ActivityBars period={primary} metric="sessions" />
            <ActivityBars period={primary} metric="energy" />
            <NetworkHealth />
          </div>
          <RecentCharges />
        </>
      )}
    </div>
  );
}
