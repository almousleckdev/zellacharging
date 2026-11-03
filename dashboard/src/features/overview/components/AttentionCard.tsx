"use client";

import Link from "next/link";
import { useIncidentSummary } from "@/features/incidents/api/useIncidentSummary";
import { useSession } from "@/features/session/SessionProvider";
import { useStations } from "@/features/stations/api/useStations";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { Card } from "@/shared/ui/Card";
import { Icon } from "@/shared/ui/Icon";
import type { OverviewTotals } from "../totals";

type Item = { key: string; count: number; label: string; href: string; urgent: boolean };

export function AttentionCard({ totals }: { totals: OverviewTotals }) {
  const { t } = useI18n();
  const { country, scopedHref } = useSession();
  const incidents = useIncidentSummary(country);
  const stations = useStations(country);
  const labels = t.overview.attention;
  const offline = (stations.data ?? []).filter((row) => !row.station.online).length;

  const items: Item[] = [
    { key: "high", count: incidents.data?.openHigh ?? 0, label: labels.highIncidents, href: "/incidents?severity=HIGH&status=OPEN", urgent: true },
    { key: "open", count: incidents.data?.open ?? 0, label: labels.openIncidents, href: "/incidents?status=OPEN", urgent: false },
    { key: "offline", count: offline, label: labels.offlineStations, href: "/stations?status=offline", urgent: false },
    { key: "live", count: totals.sessions.live, label: labels.liveSessions, href: "/sessions?state=ACTIVE", urgent: false },
  ];

  return (
    <Card title={labels.title} flush>
      <ul className="flex flex-col divide-y divide-border border-t border-border">
        {items.map((item) => (
          <li key={item.key}>
            <Link href={scopedHref(item.href)} className="flex items-center gap-3 px-5 py-2.5 text-sm transition-colors hover:bg-surface">
              <span className="flex-1 text-foreground">{item.label}</span>
              <span
                className={cn(
                  "figure min-w-7 rounded-full px-2 py-0.5 text-center text-xs font-semibold",
                  item.urgent && item.count > 0 ? "bg-danger-soft text-danger" : "bg-surface-strong text-muted",
                )}
              >
                {item.count}
              </span>
              <Icon name="chevronRight" className="size-4 text-subtle" />
            </Link>
          </li>
        ))}
      </ul>
    </Card>
  );
}
