"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { Badge } from "@/shared/ui/Badge";
import type { Column } from "@/shared/ui/DataTable";
import type { ActivityItem } from "../historySchemas";

export function useActivityColumns(): Column<ActivityItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.customers.activity.columns;
  const actions: Record<string, string> = { ...t.audit.actions, ...t.customers.activity.actions };
  const sources: Record<string, string> = t.customers.activity.sources;

  return [
    { key: "date", header: labels.date, text: (row) => format.dateTime(row.createdAt), cell: (row) => format.dateTime(row.createdAt) },
    {
      key: "source",
      header: labels.source,
      text: (row) => sources[row.source] ?? row.source,
      cell: (row) => <Badge tone={row.source === "ADMIN" ? "warning" : "neutral"}>{sources[row.source] ?? row.source}</Badge>,
    },
    { key: "actor", header: labels.actor, text: (row) => row.actor ?? "", cell: (row) => row.actor ?? t.common.none },
    { key: "action", header: labels.action, text: (row) => actions[row.action] ?? row.action, cell: (row) => <span className="font-medium">{actions[row.action] ?? row.action}</span> },
    { key: "detail", header: labels.detail, text: (row) => row.detail ?? "", cell: (row) => <span className="text-muted">{row.detail ?? t.common.none}</span> },
  ];
}
