"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import type { CommissionItem } from "../schemas";

export function CommissionsTable({ items, showVat }: { items: CommissionItem[]; showVat: boolean }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.commissions.columns;
  const money = (row: CommissionItem, minor: number) =>
    format.money([{ currency: row.currency, decimals: row.decimals, minor }])[0];

  const columns: Column<CommissionItem>[] = [
    {
      key: "session",
      header: labels.session,
      cell: (row) => (
        <span className="flex flex-col">
          <span className="font-medium text-foreground">{row.customerName}</span>
          <span className="text-xs text-subtle">{row.stationId}</span>
        </span>
      ),
    },
    { key: "date", header: labels.date, cell: (row) => format.dateTime(row.at) },
    { key: "electric", header: labels.electric, align: "right", cell: (row) => money(row, row.electricMinor) },
    {
      key: "commission",
      header: labels.commission,
      align: "right",
      cell: (row) => <span className="figure font-semibold text-primary-text">{money(row, row.serviceFeeMinor)}</span>,
    },
    ...(showVat ? [{ key: "vat", header: labels.vat, align: "right" as const, cell: (row: CommissionItem) => money(row, row.vatMinor) }] : []),
    {
      key: "total",
      header: labels.total,
      align: "right",
      cell: (row) => <span className="figure font-semibold">{money(row, row.totalMinor)}</span>,
    },
  ];

  return <DataTable caption={t.commissions.title} columns={columns} rows={items} rowKey={(row) => row.sessionId} />;
}
