"use client";

import { countryLabel } from "@/features/session/countryScope";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ErrorState } from "@/shared/ui/ErrorState";
import { interpolate } from "@/shared/i18n/interpolate";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { PageHeader } from "@/shared/ui/PageHeader";
import { useOverview } from "../api/useOverview";
import { overviewTotals } from "../totals";
import { CountryBreakdown } from "./CountryBreakdown";
import { MoneyCard } from "./MoneyCard";
import { OverviewMetrics } from "./OverviewMetrics";
import { OverviewSkeleton } from "./OverviewSkeleton";

export function OverviewPage() {
  const { t } = useI18n();
  const { profile, country } = useSession();
  const format = useFormatters();
  const overview = useOverview(country);
  const scope = countryLabel(country, profile.countries, t.shell.country.all);

  return (
    <div className="flex flex-col">
      <PageHeader
        title={t.overview.title}
        subtitle={t.overview.subtitle}
        trail={[t.shell.breadcrumbRoot, t.overview.title]}
        actions={
          <p className="text-sm text-muted">
            {t.overview.viewing} : <span className="font-semibold text-foreground">{scope}</span>
          </p>
        }
      />
      {overview.isPending && (
        <div role="status" aria-busy="true">
          <span className="sr-only">{t.common.loading}</span>
          <OverviewSkeleton />
        </div>
      )}
      {overview.isError && <ErrorState message={t.overview.loadError} onRetry={() => void overview.refetch()} />}
      {overview.data && (
        <div className="flex flex-col gap-6">
          {overview.data.since && (
            <p className="rounded-control border border-border bg-surface px-4 py-3 text-sm text-muted">
              {interpolate(t.overview.since, { date: format.date(overview.data.since) })}
            </p>
          )}
          <OverviewMetrics totals={overviewTotals(overview.data.countries)} />
          <MoneyCard totals={overviewTotals(overview.data.countries)} />
          {overview.data.countries.length > 1 && <CountryBreakdown countries={overview.data.countries} />}
        </div>
      )}
    </div>
  );
}
