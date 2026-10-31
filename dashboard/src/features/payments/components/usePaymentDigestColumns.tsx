"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { CellLink } from "@/shared/ui/CellLink";
import type { Column } from "@/shared/ui/DataTable";
import { OperatorBadge } from "@/shared/ui/OperatorBadge";
import { signedAmount } from "../format";
import type { PaymentDigest } from "../schemas";
import { PaymentStateBadge } from "./PaymentStateBadge";

export function usePaymentDigestColumns(): Column<PaymentDigest>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const labels = t.payments.digest;
  const kinds: Record<string, string> = t.payments.kinds;
  const states: Record<string, string> = t.payments.states;
  const money = (row: PaymentDigest, minor: number) =>
    format.money([{ currency: row.last.currency, decimals: row.last.decimals, minor }])[0];
  const lastDate = (row: PaymentDigest) => format.dateTime(row.last.createdAt);
  const lastAmount = (row: PaymentDigest) => signedAmount(row.last, format);

  return [
    {
      key: "customer",
      header: labels.customer,
      text: (row) => row.customerName,
      cell: (row) => (
        <CellLink href={scopedHref(`/customers/${row.country}/${row.customerId}`)} className="flex flex-col">
          <span className="font-medium text-foreground">{row.customerName}</span>
          <span className="flex items-center gap-1.5 text-xs text-subtle">
            <OperatorBadge operator={row.last.operator} />
            {row.last.msisdn}
          </span>
        </CellLink>
      ),
    },
    { key: "count", header: labels.operations, align: "right", sortKey: "count", text: (row) => row.operations, cell: (row) => format.number(row.operations) },
    {
      key: "last",
      header: labels.last,
      sortKey: "last",
      text: (row) => `${kinds[row.last.kind] ?? row.last.kind} · ${states[row.last.state] ?? row.last.state} · ${lastDate(row)}`,
      cell: (row) => (
        <span className="flex items-center gap-3">
          <span className="flex flex-col">
            <span className="font-medium text-foreground">{kinds[row.last.kind] ?? row.last.kind}</span>
            <span className="text-xs text-subtle">{lastDate(row)}</span>
          </span>
          <PaymentStateBadge state={row.last.state} />
        </span>
      ),
    },
    { key: "lastAmount", header: labels.lastAmount, align: "right", text: lastAmount, cell: lastAmount },
    { key: "topups", header: labels.topups, align: "right", sortKey: "spent", text: (row) => money(row, row.topupsMinor), cell: (row) => <span className="figure font-semibold">{money(row, row.topupsMinor)}</span> },
    { key: "withdrawn", header: labels.withdrawn, align: "right", text: (row) => money(row, row.withdrawalsMinor), cell: (row) => money(row, row.withdrawalsMinor) },
  ];
}
