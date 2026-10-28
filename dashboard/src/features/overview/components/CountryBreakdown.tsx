"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import type { CountryFigures } from "../schemas";

export function CountryBreakdown({ countries }: { countries: CountryFigures[] }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.overview.table;
  const money = (country: CountryFigures, minor: number) =>
    format.money([{ currency: country.currency, decimals: country.currencyDecimals, minor }], "code")[0];

  const columns: Column<CountryFigures>[] = [
    { key: "country", header: labels.country, cell: (row) => <span className="font-medium">{row.name}</span> },
    { key: "customers", header: labels.customers, align: "right", cell: (row) => format.number(row.customers.total) },
    { key: "live", header: labels.liveSessions, align: "right", cell: (row) => format.number(row.sessions.live) },
    {
      key: "energy",
      header: labels.energy,
      align: "right",
      cell: (row) => `${format.number(row.sessions.energyKwh, 1)} ${t.overview.unitKwh}`,
    },
    { key: "revenue", header: labels.revenue, align: "right", cell: (row) => money(row, row.sessions.revenueMinor) },
    { key: "balance", header: labels.balance, align: "right", cell: (row) => money(row, row.wallets.balanceMinor) },
  ];

  return (
    <section className="overflow-hidden rounded-card border border-border bg-background shadow-[0_1px_2px_rgb(11_18_32/0.04)]">
      <h2 className="font-display px-6 py-5 text-base font-semibold text-foreground">{labels.title}</h2>
      <DataTable caption={labels.title} columns={columns} rows={countries} rowKey={(row) => row.code} />
    </section>
  );
}
