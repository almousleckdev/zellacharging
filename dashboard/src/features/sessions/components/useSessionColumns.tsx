"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import { sessionCost } from "../format";
import { FreeBadge } from "./FreeBadge";
import type { SessionItem } from "../schemas";
import { SessionStateBadge } from "./SessionStateBadge";
import { StopChargeButton } from "./StopChargeButton";

export function useSessionColumns(): Column<SessionItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile, scopedHref } = useSession();
  const labels = t.sessions.columns;
  const fallback = profile.countries.find((item) => item.code === country) ?? profile.countries[0];
  const states: Record<string, string> = t.sessions.states;
  const cost = (row: SessionItem) => sessionCost(row, format, fallback?.currency ?? "XAF");
  const energy = (row: SessionItem) => (row.energyKwh === null ? "" : `${format.number(row.energyKwh, 3)} ${t.sessions.unitKwh}`);

  return [
    {
      key: "station",
      header: labels.station,
      text: (row) => `${row.stationId} #${row.connectorId}`,
      cell: (row) => (
        <span className="flex flex-col">
          <span className="font-medium text-foreground">{row.stationId}</span>
          <span className="text-xs text-subtle">
            {t.sessions.connector} {row.connectorId}
          </span>
        </span>
      ),
    },
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
    { key: "state", header: labels.state, text: (row) => states[row.state] ?? row.state, cell: (row) => <SessionStateBadge state={row.state} /> },
    { key: "energy", header: labels.energy, align: "right", sortKey: "energy", text: energy, cell: energy },
    { key: "cost", header: labels.cost, align: "right", sortKey: "amount", text: (row) => (row.freeCategory ? t.sessions.free[row.freeCategory] : cost(row)),
      cell: (row) => (row.freeCategory ? <FreeBadge category={row.freeCategory} /> : <span className="figure font-semibold">{cost(row)}</span>),
    },
    {
      key: "date",
      header: labels.date,
      align: "right",
      sortKey: "date",
      text: (row) => format.dateTime(row.startedAt ?? row.createdAt),
      cell: (row) => format.dateTime(row.startedAt ?? row.createdAt),
    },
    { key: "stop", header: "", align: "right", text: () => "", cell: (row) => <StopChargeButton session={row} compact /> },
  ];
}
