"use client";

import Link from "next/link";
import { useSession } from "@/features/session/SessionProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import type { PaymentItem } from "../schemas";
import { PaymentStateBadge } from "./PaymentStateBadge";

const SETTLED = new Set(["SUCCEEDED", "PAID"]);

export function PaymentsTable({ items }: { items: PaymentItem[] }) {
  const { t } = useI18n();
  const format = useFormatters();
  const { scopedHref } = useSession();
  const labels = t.payments.columns;
  const kinds: Record<string, string> = t.payments.kinds;

  const columns: Column<PaymentItem>[] = [
    {
      key: "payment",
      header: labels.payment,
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
      cell: (row) => (
        <Link href={scopedHref(`/customers/${row.country}/${row.customerId}`)} className="flex flex-col hover:text-primary-text">
          <span className="font-medium">{row.customerName}</span>
          <span className="text-xs text-subtle">
            {row.operator} · {row.msisdn}
          </span>
        </Link>
      ),
    },
    { key: "state", header: labels.state, cell: (row) => <PaymentStateBadge state={row.state} /> },
    {
      key: "amount",
      header: labels.amount,
      align: "right",
      cell: (row) => (
        <span className={cn("figure font-semibold", !SETTLED.has(row.state) && "text-subtle")}>
          {SETTLED.has(row.state) ? (row.kind === "WITHDRAWAL" ? "− " : "+ ") : ""}
          {format.money([{ currency: row.currency, decimals: row.decimals, minor: row.amountMinor }])[0]}
        </span>
      ),
    },
    { key: "date", header: labels.date, align: "right", cell: (row) => format.dateTime(row.createdAt) },
  ];

  return <DataTable caption={t.payments.title} columns={columns} rows={items} rowKey={(row) => `${row.kind}-${row.id}`} />;
}
