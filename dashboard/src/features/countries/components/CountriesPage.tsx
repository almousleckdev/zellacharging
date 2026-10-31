"use client";

import Link from "next/link";
import { useOverview } from "@/features/overview/api/useOverview";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";
import { ListPanel } from "@/shared/ui/ListPanel";
import { PageHeader } from "@/shared/ui/PageHeader";

type Row = NonNullable<ReturnType<typeof useOverview>["data"]>["countries"][number];

export function CountriesPage() {
  const { t } = useI18n();
  const format = useFormatters();
  const { profile } = useSession();
  const overview = useOverview("ALL");
  const labels = t.countries;
  const allowed = new Set(profile.countries.map((item) => item.code));
  const rows = (overview.data?.countries ?? []).filter((item) => allowed.has(item.code));
  const money = (row: Row, minor: number) => format.money([{ currency: row.currency, decimals: row.currencyDecimals, minor }], "code")[0];

  const columns: Column<Row>[] = [
    {
      key: "country",
      header: labels.title,
      cell: (row) => (
        <span className="flex flex-col">
          <span className="font-medium text-foreground">{row.name}</span>
          <span className="text-xs text-subtle">
            {row.code} · {row.currency}
          </span>
        </span>
      ),
    },
    {
      key: "customers",
      header: labels.customers,
      align: "right",
      cell: (row) => `${format.number(row.customers.total)} · ${format.number(row.customers.active)} ${labels.active}`,
    },
    { key: "live", header: labels.live, align: "right", cell: (row) => format.number(row.sessions.live) },
    { key: "energy", header: labels.energy, align: "right", cell: (row) => `${format.number(row.sessions.energyKwh, 1)} ${t.overview.unitKwh}` },
    { key: "sales", header: labels.sales, align: "right", cell: (row) => <span className="figure font-semibold">{money(row, row.wallets.capturedMinor)}</span> },
    { key: "balances", header: labels.balances, align: "right", cell: (row) => money(row, row.wallets.balanceMinor) },
    {
      key: "open",
      header: "",
      align: "right",
      cell: (row) => (
        <Link href={`/?country=${row.code}`} className="text-sm font-medium text-primary-text hover:underline">
          {labels.open}
        </Link>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <PageHeader title={labels.title} subtitle={labels.subtitle} />
      <ListPanel
        pending={overview.isPending}
        failed={overview.isError}
        onRetry={() => void overview.refetch()}
        loadError={labels.loadError}
        empty={{ icon: "globe", title: labels.title, text: "" }}
        isEmpty={overview.data !== undefined && rows.length === 0}
      >
        {rows.length > 0 && <DataTable caption={labels.title} columns={columns} rows={rows} rowKey={(row) => row.code} />}
      </ListPanel>
    </div>
  );
}
