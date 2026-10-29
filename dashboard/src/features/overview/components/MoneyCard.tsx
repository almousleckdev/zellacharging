"use client";

import { interpolate } from "@/shared/i18n/interpolate";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Badge } from "@/shared/ui/Badge";
import { Card } from "@/shared/ui/Card";
import { SegmentBar } from "@/shared/ui/SegmentBar";
import type { MoneyAmount } from "@/shared/lib/format";
import type { OverviewTotals } from "../totals";

type RowProps = { label: string; hint?: string; value: string; badge?: string; strong?: boolean };

function Row({ label, hint, value, badge, strong }: RowProps) {
  return (
    <div className="flex items-start justify-between gap-6 py-3.5 first:pt-0 last:pb-0">
      <div className="flex flex-col gap-0.5">
        <p className="flex items-center gap-2 text-sm font-medium text-foreground">
          {label}
          {badge && <Badge tone="warning">{badge}</Badge>}
        </p>
        {hint && <p className="text-xs text-subtle">{hint}</p>}
      </div>
      <p className={strong ? "figure text-xl font-semibold text-foreground" : "figure text-lg font-semibold text-foreground"}>{value}</p>
    </div>
  );
}

export function MoneyCard({ totals }: { totals: OverviewTotals }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.overview.money;
  const line = (amounts: MoneyAmount[]) => {
    const used = amounts.filter((amount) => amount.minor !== 0);
    return format.money(used.length > 0 ? used : amounts.slice(0, 1)).join(" · ");
  };
  const single = totals.walletBalance.length === 1;
  const owed = single ? totals.walletBalance[0].minor : 0;
  const sales = single ? (totals.captured[0]?.minor ?? 0) : 0;

  return (
    <Card title={labels.title} description={labels.subtitle}>
      {single && owed + sales > 0 && (
        <div className="mb-6 flex flex-col gap-2">
          <SegmentBar
            label={labels.split}
            segments={[
              { amount: sales, className: "bg-primary" },
              { amount: owed, className: "bg-border-strong" },
            ]}
          />
          <div className="flex flex-wrap items-center gap-x-5 gap-y-1 text-xs text-muted">
            <span className="flex items-center gap-1.5">
              <span aria-hidden="true" className="size-2.5 rounded-full bg-primary" />
              {labels.yours} · {line(totals.captured)}
            </span>
            <span className="flex items-center gap-1.5">
              <span aria-hidden="true" className="size-2.5 rounded-full bg-border-strong" />
              {labels.customers} · {line(totals.walletBalance)}
            </span>
          </div>
        </div>
      )}
      <div className="divide-y divide-border">
        <Row
          label={labels.topups}
          value={line(totals.topups)}
          badge={totals.pendingTopups > 0 ? interpolate(t.overview.hints.topups, { count: totals.pendingTopups }) : undefined}
        />
        <Row
          label={labels.withdrawals}
          value={line(totals.withdrawals)}
          badge={totals.pendingWithdrawals > 0 ? interpolate(t.overview.hints.withdrawals, { count: totals.pendingWithdrawals }) : undefined}
        />
        <Row label={labels.owed} hint={interpolate(labels.owedHint, { reserved: line(totals.reserved) })} value={line(totals.walletBalance)} />
        <Row label={labels.sales} hint={labels.salesHint} value={line(totals.captured)} strong />
      </div>
    </Card>
  );
}
