"use client";

import Link from "next/link";
import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import type { SessionItem } from "../schemas";
import { SessionStateBadge } from "./SessionStateBadge";

function percent(value: number | null): string {
  return value === null ? "–" : `${Math.round(value)} %`;
}

export function SessionsTable({ items }: { items: SessionItem[] }) {
  const { t } = useI18n();
  const format = useFormatters();
  const { country, profile, scopedHref } = useSession();
  const labels = t.sessions.columns;
  const fallback = profile.countries.find((item) => item.code === country) ?? profile.countries[0];

  const cost = (row: SessionItem) => {
    if (row.costMinor === null) {
      return "–";
    }
    const currency = row.currency ?? fallback?.currency ?? "XAF";
    return format.money([{ currency, decimals: row.decimals ?? 0, minor: row.costMinor }])[0];
  };

  const columns: Column<SessionItem>[] = [
    {
      key: "station",
      header: labels.station,
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
      cell: (row) => (
        <Link href={scopedHref(`/customers/${row.country}/${row.customerId}`)} className="font-medium hover:text-primary-text">
          {row.customerName}
        </Link>
      ),
    },
    { key: "state", header: labels.state, cell: (row) => <SessionStateBadge state={row.state} /> },
    {
      key: "energy",
      header: labels.energy,
      align: "right",
      cell: (row) => (row.energyKwh === null ? "–" : `${format.number(row.energyKwh, 2)} ${t.sessions.unitKwh}`),
    },
    { key: "batteryStart", header: labels.batteryStart, align: "right", cell: (row) => percent(row.startBatteryPercent) },
    { key: "batteryEnd", header: labels.batteryEnd, align: "right", cell: (row) => percent(row.endBatteryPercent) },
    { key: "cost", header: labels.cost, align: "right", cell: (row) => <span className="figure font-semibold">{cost(row)}</span> },
    { key: "date", header: labels.date, align: "right", cell: (row) => format.dateTime(row.startedAt ?? row.createdAt) },
  ];

  return <DataTable caption={t.sessions.title} columns={columns} rows={items} rowKey={(row) => `${row.country}-${row.id}`} />;
}
