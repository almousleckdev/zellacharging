"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { ErrorState } from "@/shared/ui/ErrorState";
import { EmptyState } from "@/shared/ui/EmptyState";
import { PageHeader } from "@/shared/ui/PageHeader";
import { TableSkeleton } from "@/shared/ui/TableSkeleton";
import { useCommissions } from "../api/useCommissions";
import { CommissionsSummary } from "./CommissionsSummary";
import { CommissionsTable } from "./CommissionsTable";

export function CommissionsPage() {
  const { t } = useI18n();
  const { country } = useSession();
  const commissions = useCommissions(country);
  const data = commissions.data;
  const showVat = data?.summaries.some((summary) => summary.vatPercent > 0) ?? false;

  return (
    <div className="flex flex-col">
      <PageHeader title={t.commissions.title} subtitle={t.commissions.subtitle} trail={[t.shell.breadcrumbRoot, t.commissions.title]} />
      <p className="mb-6 max-w-3xl rounded-control border border-border bg-surface px-4 py-3 text-sm text-muted">{t.commissions.explained}</p>
      {commissions.isPending && (
        <div role="status" aria-busy="true" className="overflow-hidden rounded-card border border-border">
          <span className="sr-only">{t.common.loading}</span>
          <TableSkeleton />
        </div>
      )}
      {commissions.isError && <ErrorState message={t.commissions.loadError} onRetry={() => void commissions.refetch()} />}
      {data && (
        <div className="flex flex-col gap-6">
          {data.summaries.map((summary) => (
            <CommissionsSummary key={summary.country} summary={summary} />
          ))}
          {data.items.length === 0 ? (
            <EmptyState icon="percent" title={t.commissions.emptyTitle} text={t.commissions.emptyText} />
          ) : (
            <section className="overflow-hidden rounded-card border border-border bg-background shadow-[0_1px_2px_rgb(0_0_0/0.04)]">
              <h2 className="font-display px-6 py-5 text-base font-semibold text-foreground">{t.commissions.recent}</h2>
              <CommissionsTable items={data.items} showVat={showVat} />
            </section>
          )}
        </div>
      )}
    </div>
  );
}
