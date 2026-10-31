"use client";

import Link from "next/link";
import { sessionListSchema } from "@/features/sessions/schemas";
import { useSessionColumns } from "@/features/sessions/components/useSessionColumns";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useLogList } from "@/shared/lists/useLogList";
import { Card } from "@/shared/ui/Card";
import { DataTable } from "@/shared/ui/DataTable";

const RECENT = 6;

export function RecentCharges() {
  const { t } = useI18n();
  const { country, scopedHref } = useSession();
  const query = useLogList("sessions", { country, text: "", from: "", to: "", page: 0, size: RECENT, sort: "", extra: {} }, sessionListSchema);
  const rows = query.data?.items ?? [];
  const columns = useSessionColumns().filter((column) => ["station", "state", "energy", "cost", "date"].includes(column.key));

  return (
    <Card
      title={t.overview.recent.charges}
      flush
      action={
        <Link href={scopedHref("/sessions")} className="text-sm font-medium text-primary-text hover:underline">
          {t.overview.recent.all}
        </Link>
      }
    >
      {query.data && rows.length === 0 && <p className="px-5 pb-5 text-sm text-subtle">{t.overview.recent.empty}</p>}
      {rows.length > 0 && (
        <DataTable
          caption={t.overview.recent.charges}
          columns={columns}
          rows={rows}
          rowKey={(row) => `${row.country}-${row.id}`}
          rowHref={(row) => scopedHref(`/sessions/${row.country}/${row.id}`)}
        />
      )}
    </Card>
  );
}
