"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { activityListSchema } from "../historySchemas";
import { useActivityColumns } from "./useActivityColumns";

export function CustomerActivityTab({ country, id }: { country: string; id: string }) {
  const { t } = useI18n();
  const columns = useActivityColumns();

  return (
    <LogListPage
      resource={`customers/${country}/${id}/activity`}
      exportName="activity"
      schema={activityListSchema}
      labels={{ ...t.customers.activity, title: t.customers.tabs.activity, subtitle: "", results: "", searchLabel: "", searchPlaceholder: "" }}
      icon="clipboard"
      columns={columns}
      searchable={false}
      withRange={false}
      rowKey={(row) => `${row.source}-${row.createdAt}-${row.action}`}
      embedded
    />
  );
}
