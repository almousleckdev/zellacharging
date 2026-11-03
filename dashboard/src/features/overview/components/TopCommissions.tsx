"use client";

import Link from "next/link";
import { commissionsSchema } from "@/features/commissions/schemas";
import { useCommissionColumns } from "@/features/commissions/components/useCommissionColumns";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useLogList } from "@/shared/lists/useLogList";
import { Card } from "@/shared/ui/Card";
import { DataTable } from "@/shared/ui/DataTable";

const TOP = 3;
const SHOWN = ["customer", "charges", "commission"];

function CommissionsCard() {
  const { t } = useI18n();
  const { country, scopedHref } = useSession();
  const query = useLogList("commissions", { country, text: "", from: "", to: "", page: 0, size: TOP, sort: "", extra: {} }, commissionsSchema);
  const columns = useCommissionColumns().filter((column) => SHOWN.includes(column.key));
  const rows = query.data?.items ?? [];
  const labels = t.overview.commissions;

  return (
    <Card
      title={labels.title}
      flush
      action={
        <Link href={scopedHref("/commissions")} className="text-sm font-medium text-primary-text hover:underline">
          {labels.all}
        </Link>
      }
    >
      {query.data && rows.length === 0 && <p className="px-5 pb-5 text-sm text-subtle">{labels.empty}</p>}
      {rows.length > 0 && (
        <DataTable
          caption={labels.title}
          columns={columns}
          rows={rows}
          rowKey={(row) => `${row.country}-${row.customerId}`}
          rowHref={(row) => scopedHref(`/customers/${row.country}/${row.customerId}?tab=sessions`)}
        />
      )}
    </Card>
  );
}

export function TopCommissions() {
  const { profile } = useSession();

  return profile.permissions.includes("PAYMENTS_READ") ? <CommissionsCard /> : null;
}
