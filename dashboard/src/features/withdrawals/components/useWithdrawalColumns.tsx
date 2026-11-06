"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import { OperatorBadge } from "@/shared/ui/OperatorBadge";
import { isPaidOut, withdrawalAmount } from "../format";
import type { WithdrawalItem } from "../schemas";
import { WithdrawalStateBadge } from "./WithdrawalStateBadge";

export function useWithdrawalColumns(): Column<WithdrawalItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const labels = t.withdrawals.columns;
  const states: Record<string, string> = t.withdrawals.states;
  const amount = (row: WithdrawalItem) => withdrawalAmount(row, format);

  return [
    {
      key: "payment",
      header: labels.payment,
      text: (row) => row.reference,
      cell: (row) => <span className="font-mono text-xs text-foreground">{row.reference}</span>,
    },
    {
      key: "customer",
      header: labels.customer,
      exports: [
        { header: labels.customer, value: (row) => row.customerName },
        { header: labels.operator, value: (row) => row.operator },
        { header: labels.phone, value: (row) => row.msisdn },
      ],
      cell: (row) => (
        <CellLink href={scopedHref(`/customers/${row.country}/${row.customerId}`)} className="flex flex-col">
          <span className="font-medium">{row.customerName}</span>
          <span className="flex items-center gap-1.5 text-xs text-subtle">
            <OperatorBadge operator={row.operator} />
            {row.msisdn}
          </span>
        </CellLink>
      ),
    },
    { key: "state", header: labels.state, text: (row) => states[row.state] ?? row.state, cell: (row) => <WithdrawalStateBadge state={row.state} /> },
    {
      key: "amount",
      header: labels.amount,
      align: "right",
      sortKey: "amount",
      text: amount,
      cell: (row) => <span className={cn("figure font-semibold", !isPaidOut(row) && "text-subtle")}>{amount(row)}</span>,
    },
    { key: "date", header: labels.date, align: "right", sortKey: "date", text: (row) => format.dateTime(row.createdAt), cell: (row) => format.dateTime(row.createdAt) },
  ];
}
