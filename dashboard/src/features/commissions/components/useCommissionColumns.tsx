"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import type { CommissionItem } from "../schemas";

export function useCommissionColumns(): Column<CommissionItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const labels = t.commissions.columns;
  const money = (row: CommissionItem, minor: number) =>
    format.money([{ currency: row.currency, decimals: row.decimals, minor }])[0];

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
    { key: "charges", header: labels.charges, align: "right", text: (row) => row.sessions, cell: (row) => format.number(row.sessions) },
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
    { key: "last", header: labels.last, align: "right", text: (row) => format.dateTime(row.lastAt), cell: (row) => format.dateTime(row.lastAt) },
  ];
}
