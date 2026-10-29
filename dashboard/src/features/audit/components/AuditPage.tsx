"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { auditListSchema } from "../schemas";
import { useAuditColumns } from "./useAuditColumns";

export function AuditPage() {
  const { t } = useI18n();
  const columns = useAuditColumns();
  const actions: Record<string, string> = t.audit.actions;

  return (
    <LogListPage
      resource="audit"
      schema={auditListSchema}
      labels={t.audit}
      icon="clipboard"
      columns={columns}
      rowKey={(row) => row.id}
      filters={[
        {
          key: "action",
          label: t.audit.actionLabel,
          allLabel: t.audit.allActions,
          options: Object.entries(actions).map(([value, label]) => ({ value, label })),
        },
      ]}
    />
  );
}
