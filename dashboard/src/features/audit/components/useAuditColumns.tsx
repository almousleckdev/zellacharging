"use client";

import { useFormatters } from "@/shared/i18n/useFormatters";
import { useI18n } from "@/shared/i18n/I18nProvider";
import type { Column } from "@/shared/ui/DataTable";
import type { AuditItem } from "../schemas";

export function useAuditColumns(): Column<AuditItem>[] {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.audit.columns;
  const actions: Record<string, string> = t.audit.actions;
  const actionName = (row: AuditItem) => actions[row.action] ?? row.action;

  return [
    { key: "date", header: labels.date, text: (row) => format.dateTime(row.createdAt), cell: (row) => format.dateTime(row.createdAt) },
    { key: "admin", header: labels.admin, text: (row) => row.adminEmail, cell: (row) => <span className="font-medium">{row.adminEmail}</span> },
    {
      key: "action",
      header: labels.action,
      text: actionName,
      cell: (row) => (
        <span className="flex flex-col">
          <span className="font-medium text-foreground">{actionName(row)}</span>
          <span className="font-mono text-xs text-subtle">{row.action}</span>
        </span>
      ),
    },
    {
      key: "subject",
      header: labels.subject,
      text: (row) => row.subject ?? "",
      cell: (row) => <span className="break-all font-mono text-xs">{row.subject ?? ""}</span>,
    },
    { key: "country", header: labels.country, text: (row) => row.country ?? "", cell: (row) => row.country ?? "" },
    { key: "detail", header: labels.detail, text: (row) => row.detail ?? "", cell: (row) => <span className="text-muted">{row.detail ?? ""}</span> },
  ];
}
