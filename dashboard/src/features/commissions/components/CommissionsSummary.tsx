"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { MetricCard } from "@/shared/ui/MetricCard";
import type { CommissionSummary } from "../schemas";

export function CommissionsSummary({ summary }: { summary: CommissionSummary }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.commissions;
  const money = (minor: number) => format.money([{ currency: summary.currency, decimals: summary.decimals, minor }])[0];
  const rate = format.number(summary.serviceFeePercent, summary.serviceFeePercent % 1 === 0 ? 0 : 1);

  return (
    <div className="grid gap-4 sm:grid-cols-2 min-[1700px]:grid-cols-4">
      <MetricCard
        label={interpolate(labels.earned, { rate })}
        icon="percent"
        value={money(summary.serviceFeeMinor)}
        hint={interpolate(labels.earnedHint, { count: format.number(summary.sessions) })}
      />
      <MetricCard label={labels.electric} icon="bolt" value={money(summary.electricMinor)} hint={labels.electricHint} />
      <MetricCard
        label={labels.vat}
        icon="tag"
        value={money(summary.vatMinor)}
        hint={interpolate(labels.vatHint, { rate: format.number(summary.vatPercent, summary.vatPercent % 1 === 0 ? 0 : 1) })}
      />
      <MetricCard
        label={labels.total}
        icon="wallet"
        value={money(summary.electricMinor + summary.serviceFeeMinor + summary.vatMinor)}
        hint={labels.totalHint}
      />
    </div>
  );
}
