"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { commissionsSchema } from "../schemas";
import { mergeSummaries } from "../summary";
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
      rowKey={(row) => `${row.country}-${row.customerId}`}
      rowHref={(row) => scopedHref(`/customers/${row.country}/${row.customerId}?tab=sessions`)}
      above={(data) => (
        <div className="flex flex-col gap-6">
          {mergeSummaries(data.summaries).map((summary) => (
            <CommissionsSummary key={summary.currency} summary={summary} />
          ))}
        </div>
      )}
    />
  );
}
