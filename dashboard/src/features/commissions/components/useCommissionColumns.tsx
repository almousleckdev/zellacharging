"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import type { Column } from "@/shared/ui/DataTable";
import type { CommissionItem } from "../schemas";

export function useCommissionColumns(): Column<CommissionItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.commissions.columns;
  const money = (row: CommissionItem, minor: number) =>
    format.money([{ currency: row.currency, decimals: row.decimals, minor }])[0];

  return [
    {
      key: "session",
      header: labels.session,
      text: (row) => `${row.customerName} (${row.stationId})`,
      cell: (row) => (
        <span className="flex flex-col">
          <span className="font-medium text-foreground">{row.customerName}</span>
          <span className="text-xs text-subtle">{row.stationId}</span>
        </span>
      ),
    },
    { key: "date", header: labels.date, text: (row) => format.dateTime(row.at), cell: (row) => format.dateTime(row.at) },
    { key: "electric", header: labels.electric, align: "right", text: (row) => money(row, row.electricMinor), cell: (row) => money(row, row.electricMinor) },
    {
      key: "commission",
      header: labels.commission,
      align: "right",
      text: (row) => money(row, row.serviceFeeMinor),
      cell: (row) => <span className="figure font-semibold text-primary-text">{money(row, row.serviceFeeMinor)}</span>,
    },
    { key: "vat", header: labels.vat, align: "right", text: (row) => money(row, row.vatMinor), cell: (row) => money(row, row.vatMinor) },
    {
      key: "total",
      header: labels.total,
      align: "right",
      text: (row) => money(row, row.totalMinor),
      cell: (row) => <span className="figure font-semibold">{money(row, row.totalMinor)}</span>,
    },
  ];
}
