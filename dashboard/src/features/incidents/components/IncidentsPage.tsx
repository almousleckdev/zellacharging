"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { incidentListSchema } from "../schemas";
import { ReviewMenu } from "./ReviewMenu";
import { subjectHref, useIncidentColumns } from "./useIncidentColumns";

const SEVERITIES = ["HIGH", "MEDIUM", "LOW"] as const;
const STATUSES = ["OPEN", "ACKNOWLEDGED", "RESOLVED"] as const;
const KINDS = ["SESSION_FAILED", "SESSION_ABANDONED", "HOLD_REVIEW", "TOPUP_FAILED", "TOPUP_STUCK", "WITHDRAWAL_FAILED", "WITHDRAWAL_REVIEW"] as const;

export function IncidentsPage() {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = useIncidentColumns();
  const labels = t.incidents;
  const pick = (names: Record<string, string>, values: readonly string[]) => values.map((value) => ({ value, label: names[value] }));

  return (
    <LogListPage
      resource="incidents"
      schema={incidentListSchema}
      labels={labels}
      icon="bell"
      columns={columns}
      searchable={false}
      rowKey={(row) => `${row.country}-${row.key}`}
      rowHref={(row) => scopedHref(subjectHref(row))}
      rowActions={{ key: "actions", header: "", align: "right", cell: (row) => <ReviewMenu incident={row} /> }}
      filters={[
        { key: "severity", label: labels.severityLabel, allLabel: labels.allSeverities, options: pick(labels.severities, SEVERITIES) },
        { key: "status", label: labels.statusLabel, allLabel: labels.allStatuses, options: pick(labels.statuses, STATUSES) },
        { key: "kind", label: labels.kindLabel, allLabel: labels.allKinds, options: pick(labels.kinds, KINDS) },
      ]}
    />
  );
}
