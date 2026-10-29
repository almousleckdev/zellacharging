"use client";

import Link from "next/link";
import { sessionListSchema } from "@/features/sessions/schemas";
import { useSessionColumns } from "@/features/sessions/components/useSessionColumns";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useLogList } from "@/shared/lists/useLogList";
import { DataTable } from "@/shared/ui/DataTable";

const RECENT = 6;

export function RecentCharges() {
  const { t } = useI18n();
  const { country, scopedHref } = useSession();
  const query = useLogList("sessions", { country, text: "", from: "", to: "", page: 0, size: RECENT, sort: "", extra: {} }, sessionListSchema);
  const columns = useSessionColumns().filter((column) => ["station", "state", "energy", "cost", "date"].includes(column.key));

  return (
    <section className="overflow-hidden rounded-card border border-border bg-background shadow-[0_1px_2px_rgb(0_0_0/0.04)]">
      <header className="flex items-center justify-between px-5 py-4">
        <h2 className="font-display text-base font-semibold text-foreground">{t.overview.recent.charges}</h2>
        <Link href={scopedHref("/sessions")} className="text-sm font-medium text-primary-text hover:underline">
          {t.overview.recent.all}
        </Link>
      </header>
      <DataTable
        caption={t.overview.recent.charges}
        columns={columns}
        rows={query.data?.items ?? []}
        rowKey={(row) => `${row.country}-${row.id}`}
        rowHref={(row) => scopedHref(`/sessions/${row.country}/${row.id}`)}
      />
    </section>
  );
}
