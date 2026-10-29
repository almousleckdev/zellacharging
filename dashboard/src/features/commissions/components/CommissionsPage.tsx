"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { commissionsSchema } from "../schemas";
import { CommissionsSummary } from "./CommissionsSummary";
import { useCommissionColumns } from "./useCommissionColumns";

export function CommissionsPage() {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = useCommissionColumns();

  return (
    <LogListPage
      resource="commissions"
      schema={commissionsSchema}
      labels={t.commissions}
      icon="percent"
      columns={columns}
      rowKey={(row) => `${row.country}-${row.sessionId}`}
      rowHref={(row) => scopedHref(`/sessions/${row.country}/${row.sessionId}`)}
      above={(data) => (
        <div className="flex flex-col gap-6">
          <p className="max-w-3xl rounded-control border border-border bg-surface px-4 py-3 text-sm text-muted">{t.commissions.explained}</p>
          {data.summaries.map((summary) => (
            <CommissionsSummary key={summary.country} summary={summary} />
          ))}
        </div>
      )}
    />
  );
}
