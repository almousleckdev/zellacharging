"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import { isSettled, signedAmount } from "../format";
import type { PaymentItem } from "../schemas";
import { PaymentStateBadge } from "./PaymentStateBadge";

export function usePaymentColumns(): Column<PaymentItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const labels = t.payments.columns;
  const kinds: Record<string, string> = t.payments.kinds;
  const states: Record<string, string> = t.payments.states;
  const amount = (row: PaymentItem) => signedAmount(row, format);

  return [
    {
      key: "payment",
      header: labels.payment,
      text: (row) => `${kinds[row.kind] ?? row.kind} ${row.reference}`,
      cell: (row) => (
        <span className="flex flex-col">
          <span className="font-medium text-foreground">{kinds[row.kind] ?? row.kind}</span>
          <span className="font-mono text-xs text-subtle">{row.reference}</span>
        </span>
      ),
    },
    {
      key: "customer",
      header: labels.customer,
      text: (row) => `${row.customerName} (${row.operator} ${row.msisdn})`,
      cell: (row) => (
        <CellLink href={scopedHref(`/customers/${row.country}/${row.customerId}`)} className="flex flex-col">
          <span className="font-medium">{row.customerName}</span>
          <span className="text-xs text-subtle">
            {row.operator} · {row.msisdn}
          </span>
        </CellLink>
      ),
    },
    { key: "state", header: labels.state, text: (row) => states[row.state] ?? row.state, cell: (row) => <PaymentStateBadge state={row.state} /> },
    {
      key: "amount",
      header: labels.amount,
      align: "right",
      sortKey: "amount",
      text: amount,
      cell: (row) => <span className={cn("figure font-semibold", !isSettled(row) && "text-subtle")}>{amount(row)}</span>,
    },
    { key: "date", header: labels.date, align: "right", sortKey: "date", text: (row) => format.dateTime(row.createdAt), cell: (row) => format.dateTime(row.createdAt) },
  ];
}
