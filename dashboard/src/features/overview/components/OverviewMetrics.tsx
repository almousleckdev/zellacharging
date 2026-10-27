"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";
import { MetricCard } from "@/shared/ui/MetricCard";
import type { CountryFigures } from "../schemas";
import { overviewTotals } from "../totals";

export function OverviewMetrics({ countries }: { countries: CountryFigures[] }) {
  const { t } = useI18n();
  const format = useFormatters();
  const totals = overviewTotals(countries);
  const labels = t.overview;

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
      <MetricCard
        label={labels.metrics.revenue}
        icon="chart"
        values={format.money(totals.revenue)}
        hint={labels.hints.revenue}
      />
      <MetricCard
        label={labels.metrics.walletBalance}
        icon="wallet"
        values={format.money(totals.walletBalance)}
        hint={interpolate(labels.hints.walletBalance, { reserved: format.money(totals.reserved).join(" · ") })}
      />
      <MetricCard
        label={labels.metrics.topups}
        icon="arrowDown"
        values={format.money(totals.topups)}
        badge={totals.pendingTopups > 0 ? <Badge tone="warning">{interpolate(labels.hints.topups, { count: totals.pendingTopups })}</Badge> : undefined}
        hint={totals.pendingTopups > 0 ? undefined : interpolate(labels.hints.topups, { count: 0 })}
      />
      <MetricCard
        label={labels.metrics.withdrawals}
        icon="arrowUp"
        values={format.money(totals.withdrawals)}
        badge={totals.pendingWithdrawals > 0 ? <Badge tone="warning">{interpolate(labels.hints.withdrawals, { count: totals.pendingWithdrawals })}</Badge> : undefined}
        hint={totals.pendingWithdrawals > 0 ? undefined : interpolate(labels.hints.withdrawals, { count: 0 })}
      />
    </div>
  );
}
