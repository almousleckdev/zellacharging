"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import type { Column } from "@/shared/ui/DataTable";
import type { LedgerItem } from "../historySchemas";

const CREDITS = new Set(["TOPUP", "RELEASE", "WITHDRAWAL_REVERSAL"]);

export function useLedgerColumns(currency: string, decimals: number): Column<LedgerItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.customers.ledger.columns;
  const types: Record<string, string> = t.customers.ledger.types;
  const amount = (row: LedgerItem) =>
    `${CREDITS.has(row.type) ? "+ " : "− "}${format.money([{ currency, decimals, minor: row.amountMinor }])[0]}`;

  return [
    { key: "date", header: labels.date, text: (row) => format.dateTime(row.createdAt), cell: (row) => format.dateTime(row.createdAt) },
    { key: "type", header: labels.type, text: (row) => types[row.type] ?? row.type, cell: (row) => <span className="font-medium">{types[row.type] ?? row.type}</span> },
    {
      key: "amount",
      header: labels.amount,
      align: "right",
      text: amount,
      cell: (row) => <span className={cn("figure font-semibold", !CREDITS.has(row.type) && "text-foreground")}>{amount(row)}</span>,
    },
  ];
}
