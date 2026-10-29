"use client";

import Link from "next/link";
import { useIncidentSummary } from "@/features/incidents/api/useIncidentSummary";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import type { OverviewTotals } from "../totals";

type Item = { key: string; count: number; label: string; href: string; urgent: boolean };

export function AttentionStrip({ totals }: { totals: OverviewTotals }) {
  const { t } = useI18n();
  const { country, scopedHref } = useSession();
  const incidents = useIncidentSummary(country);
  const labels = t.overview.attention;

  const items: Item[] = [
    { key: "high", count: incidents.data?.openHigh ?? 0, label: labels.highIncidents, href: "/incidents?severity=HIGH&status=OPEN", urgent: true },
    { key: "open", count: incidents.data?.open ?? 0, label: labels.openIncidents, href: "/incidents?status=OPEN", urgent: false },
    { key: "topups", count: totals.pendingTopups, label: labels.pendingTopups, href: "/payments?kind=TOPUP&state=PENDING", urgent: false },
    { key: "withdrawals", count: totals.pendingWithdrawals, label: labels.pendingWithdrawals, href: "/payments?kind=WITHDRAWAL", urgent: false },
    { key: "live", count: totals.sessions.live, label: labels.liveSessions, href: "/sessions?state=ACTIVE", urgent: false },
  ];
  const attention = items.some((item) => item.urgent && item.count > 0);

  return (
    <section aria-label={labels.title} className="grid gap-3 sm:grid-cols-2 lg:grid-cols-5">
      {items.map((item) => (
        <Link
          key={item.key}
          href={scopedHref(item.href)}
          className={cn(
            "flex items-center justify-between gap-3 rounded-card border bg-background px-4 py-3 transition-colors hover:bg-surface",
            item.urgent && item.count > 0 ? "border-danger-border bg-danger-soft" : "border-border",
          )}
        >
          <span className="text-sm text-muted">{item.label}</span>
          <span className={cn("figure text-xl font-semibold", item.urgent && item.count > 0 ? "text-danger" : "text-foreground")}>{item.count}</span>
        </Link>
      ))}
      <p className="sr-only" role="status">
        {attention ? labels.needsAttention : labels.allClear}
      </p>
    </section>
  );
}
