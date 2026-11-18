"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import { sessionCost } from "../format";
import type { SessionDigest } from "../schemas";
import { FreeBadge } from "./FreeBadge";
import { SessionStateBadge } from "./SessionStateBadge";
import { StopChargeButton } from "./StopChargeButton";

export function useSessionDigestColumns(): Column<SessionDigest>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile, scopedHref } = useSession();
  const labels = t.sessions.digest;
  const states: Record<string, string> = t.sessions.states;
  const fallback = profile.countries.find((item) => item.code === country) ?? profile.countries[0];
  const money = (row: SessionDigest) =>
    format.money([{ currency: row.last.currency ?? fallback?.currency ?? "XAF", decimals: row.last.decimals ?? 0, minor: row.spentMinor }])[0];
  const lastDate = (row: SessionDigest) => format.dateTime(row.last.startedAt ?? row.last.createdAt);
  const lastCost = (row: SessionDigest) => sessionCost(row.last, format, fallback?.currency ?? "XAF");

  return [
    {
      key: "customer",
      header: labels.customer,
      text: (row) => row.customerName,
      cell: (row) => (
        <CellLink href={scopedHref(`/customers/${row.country}/${row.customerId}`)} className="font-medium">
          {row.customerName}
        </CellLink>
      ),
    },
    { key: "count", header: labels.charges, align: "right", sortKey: "count", text: (row) => row.sessions, cell: (row) => format.number(row.sessions) },
    {
      key: "last",
      header: labels.last,
      sortKey: "last",
      text: (row) => `${row.last.stationId} #${row.last.connectorId} · ${states[row.last.state] ?? row.last.state} · ${lastDate(row)}`,
      cell: (row) => (
        <span className="flex items-center gap-3">
          <span className="flex flex-col">
            <span className="font-medium text-foreground">{row.last.stationId}</span>
            <span className="text-xs text-subtle">{lastDate(row)}</span>
          </span>
          <SessionStateBadge state={row.last.state} />
        </span>
      ),
    },
    {
      key: "lastCost",
      header: labels.lastAmount,
      align: "right",
      text: (row) => (row.last.freeCategory ? t.sessions.free[row.last.freeCategory] : lastCost(row)),
      cell: (row) => (row.last.freeCategory ? <FreeBadge category={row.last.freeCategory} /> : lastCost(row)),
    },
    {
      key: "energy",
      header: labels.energy,
      align: "right",
      text: (row) => `${format.number(row.energyKwh, 1)} ${t.sessions.unitKwh}`,
      cell: (row) => `${format.number(row.energyKwh, 1)} ${t.sessions.unitKwh}`,
    },
    { key: "spent", header: labels.spent, align: "right", sortKey: "spent", text: money, cell: (row) => <span className="figure font-semibold">{money(row)}</span> },
    { key: "stop", header: "", align: "right", text: () => "", cell: (row) => <StopChargeButton session={row.last} compact /> },
  ];
}
