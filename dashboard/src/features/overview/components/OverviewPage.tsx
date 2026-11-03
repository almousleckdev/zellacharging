"use client";

import { PendingWithdrawalsPanel } from "@/features/withdrawals/components/PendingWithdrawalsPanel";
import { countryLabel } from "@/features/session/countryScope";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { interpolate } from "@/shared/i18n/interpolate";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { ErrorState } from "@/shared/ui/ErrorState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { PillTabs } from "@/shared/ui/PillTabs";
import { useOverview } from "../api/useOverview";
import { useSeries } from "../api/useSeries";
import { periodsByCurrency } from "../seriesMath";
import { overviewTotals } from "../totals";
import { AttentionCard } from "./AttentionCard";
import { CountryRevenue } from "./CountryRevenue";
import { OverviewSkeleton } from "./OverviewSkeleton";
import { QuickAccess } from "./QuickAccess";
import { RecentCharges } from "./RecentCharges";
import { RevenueChart } from "./RevenueCharts";
import { RevenueKpis } from "./RevenueKpis";
import { TopCommissions } from "./TopCommissions";

const PERIODS = [7, 30, 90] as const;

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
  const active = periods.filter((item) => item.current.sessions + item.previous.sessions > 0);
  const primary = active[0] ?? periods[0];
  const name = profile.firstName ?? profile.email;

  return (
    <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,1fr)_21rem]">
      <div className="flex min-w-0 flex-col gap-6">
        <PageHeader
          title={interpolate(t.overview.greeting, { name })}
          subtitle={interpolate(t.overview.subtitle, { scope })}
          actions={
            <PillTabs
              label={t.overview.period.label}
              options={PERIODS.map((value) => ({ value, label: `${value} ${t.overview.period.days}` }))}
              value={days as (typeof PERIODS)[number]}
              onChange={(value) => update({ days: value === 30 ? null : String(value) }, false)}
            />
          }
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
            <RevenueKpis period={primary} days={days} />
            <RevenueChart period={primary} days={days} />
          </>
        )}
      </div>
      <aside className="flex min-w-0 flex-col gap-6">
        <PendingWithdrawalsPanel />
        {overview.data && <AttentionCard totals={overviewTotals(overview.data.countries)} />}
        <QuickAccess />
      </aside>
      {overview.data && series.data && primary && (
        <div className="flex min-w-0 flex-col gap-6 xl:col-span-2">
          <RecentCharges />
          <div className="grid items-start gap-6 lg:grid-cols-2">
            <TopCommissions />
            {series.data.countries.length > 1 && <CountryRevenue series={series.data} days={days} />}
          </div>
        </div>
      )}
    </div>
  );
}
