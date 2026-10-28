"use client";

import Link from "next/link";
import { useCommissions } from "@/features/commissions/api/useCommissions";
import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { MetricCard } from "@/shared/ui/MetricCard";
import type { OverviewTotals } from "../totals";

export function OverviewMetrics({ totals }: { totals: OverviewTotals }) {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, scopedHref } = useSession();
  const commissions = useCommissions(country);
  const labels = t.overview;
  const summaries = commissions.data?.summaries ?? [];
  const sessions = summaries.reduce((sum, summary) => sum + summary.sessions, 0);
  const rate = summaries[0]?.serviceFeePercent ?? 0;

  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <MetricCard
        label={labels.metrics.customers}
        icon="users"
        values={[format.number(totals.customers.total)]}
        hint={interpolate(labels.hints.customers, {
          active: format.number(totals.customers.active),
          recent: format.number(totals.customers.recent),
        })}
      />
      <MetricCard
        label={labels.metrics.liveSessions}
        icon="bolt"
        values={[format.number(totals.sessions.live)]}
        hint={interpolate(labels.hints.liveSessions, { count: format.number(totals.sessions.startedLast24Hours) })}
      />
      <MetricCard
        label={labels.metrics.energy}
        icon="station"
        values={[`${format.number(totals.sessions.energyKwh, 1)} ${labels.unitKwh}`]}
        hint={interpolate(labels.hints.energy, { count: format.number(totals.sessions.completed) })}
      />
      {commissions.data && (
        <MetricCard
          label={labels.commission.label}
          icon="percent"
          values={format.money(summaries.map((summary) => ({ currency: summary.currency, decimals: summary.decimals, minor: summary.serviceFeeMinor })))}
          hint={interpolate(labels.commission.hint, { rate: format.number(rate, rate % 1 === 0 ? 0 : 1), count: format.number(sessions) })}
          badge={
            <Link href={scopedHref("/commissions")} className="font-medium text-primary-text hover:underline">
              {labels.commission.link}
            </Link>
          }
        />
      )}
    </div>
  );
}
