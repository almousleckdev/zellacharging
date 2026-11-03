"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { PageHeader } from "@/shared/ui/PageHeader";
import { PillTabs } from "@/shared/ui/PillTabs";
import { withdrawalListSchema } from "../schemas";
import { DecisionButtons } from "./DecisionButtons";
import { useWithdrawalColumns } from "./useWithdrawalColumns";

type Tab = "pending" | "history";

const STATES = ["PENDING_APPROVAL", "REQUESTED", "PROCESSING", "PAID", "REVIEW", "FAILED"] as const;
const RESET = { q: null, state: null, page: null, size: null, sort: null };

export function WithdrawalsPage() {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const { params, update } = useUrlParams();
  const tab: Tab = params.get("tab") === "history" ? "history" : "pending";
  const labels = t.withdrawals;
  const states: Record<string, string> = t.withdrawals.states;
  const allColumns = useWithdrawalColumns();
  const columns = tab === "pending" ? allColumns.filter((column) => column.key !== "state") : allColumns;
  const pending = tab === "pending";

  return (
    <div className="flex flex-col gap-6">
      <PageHeader
        title={labels.title}
        subtitle={labels.subtitle}
        actions={
          <PillTabs
            label={labels.tabs.label}
            options={[
              { value: "pending", label: labels.tabs.pending },
              { value: "history", label: labels.tabs.history },
            ]}
            value={tab}
            onChange={(value) => update({ ...RESET, tab: value === "pending" ? null : value })}
          />
        }
      />
      <LogListPage
        key={tab}
        embedded
        resource="withdrawals"
        schema={withdrawalListSchema}
        labels={pending ? labels : { ...labels, emptyTitle: labels.historyEmptyTitle, emptyText: labels.historyEmptyText }}
        icon="clipboard"
        columns={columns}
        rowKey={(row) => row.id}
        rowHref={(row) => scopedHref(`/withdrawals/${row.country}/${row.id}`)}
        fixed={pending ? { state: "PENDING_APPROVAL" } : undefined}
        filters={pending ? [] : [{ key: "state", label: labels.stateLabel, allLabel: labels.allStates, options: STATES.map((value) => ({ value, label: states[value] })) }]}
        withRange={!pending}
        exportName="withdrawals"
        rowActions={{ key: "decision", header: "", align: "right", cell: (row) => <DecisionButtons withdrawal={row} /> }}
      />
    </div>
  );
}
