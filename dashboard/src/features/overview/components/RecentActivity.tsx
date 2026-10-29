"use client";

import Link from "next/link";
import { usePaymentColumns } from "@/features/payments/components/usePaymentColumns";
import { paymentListSchema } from "@/features/payments/schemas";
import { useSessionColumns } from "@/features/sessions/components/useSessionColumns";
import { sessionListSchema } from "@/features/sessions/schemas";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { DEFAULT_PAGE_SIZE } from "@/shared/lists/filters";
import type { ListFilters } from "@/shared/lists/filters";
import { useLogList } from "@/shared/lists/useLogList";
import { DataTable } from "@/shared/ui/DataTable";
import type { Column } from "@/shared/ui/DataTable";

const RECENT = 6;

function Recent<Row>({ title, link, href, columns, rows, rowKey, rowHref }: {
  title: string;
  link: string;
  href: string;
  columns: Column<Row>[];
  rows: Row[];
  rowKey: (row: Row) => string;
  rowHref: (row: Row) => string;
}) {
  const { scopedHref } = useSession();

  return (
    <section className="overflow-hidden rounded-card border border-border bg-background shadow-[0_1px_2px_rgb(0_0_0/0.04)]">
      <header className="flex items-center justify-between px-5 py-4">
        <h2 className="font-display text-base font-semibold text-foreground">{title}</h2>
        <Link href={scopedHref(href)} className="text-sm font-medium text-primary-text hover:underline">
          {link}
        </Link>
      </header>
      <DataTable caption={title} columns={columns} rows={rows} rowKey={rowKey} rowHref={rowHref} />
    </section>
  );
}

export function RecentActivity() {
  const { t } = useI18n();
  const { country, scopedHref } = useSession();
  const filters: ListFilters = { country, text: "", from: "", to: "", page: 0, size: DEFAULT_PAGE_SIZE, sort: "", extra: { recent: "1" } };
  const sessions = useLogList("sessions", { ...filters, size: RECENT }, sessionListSchema);
  const payments = useLogList("payments", { ...filters, size: RECENT }, paymentListSchema);
  const sessionColumns = useSessionColumns().filter((column) => ["station", "customer", "state", "cost"].includes(column.key));
  const paymentColumns = usePaymentColumns().filter((column) => ["payment", "customer", "state", "amount"].includes(column.key));
  const labels = t.overview.recent;

  return (
    <div className="grid items-start gap-6 2xl:grid-cols-2">
      <Recent
        title={labels.sessions}
        link={labels.all}
        href="/sessions"
        columns={sessionColumns}
        rows={sessions.data?.items ?? []}
        rowKey={(row) => `${row.country}-${row.id}`}
        rowHref={(row) => scopedHref(`/sessions/${row.country}/${row.id}`)}
      />
      <Recent
        title={labels.payments}
        link={labels.all}
        href="/payments"
        columns={paymentColumns}
        rows={payments.data?.items ?? []}
        rowKey={(row) => `${row.kind}-${row.id}`}
        rowHref={(row) => scopedHref(`/payments/${row.country}/${row.kind}/${row.id}`)}
      />
    </div>
  );
}
