"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { sessionListSchema } from "../schemas";
import { useSessionColumns } from "./useSessionColumns";

const STATES = ["ACTIVE", "COMPLETED", "FAILED", "ABANDONED", "STOPPING", "START_ACCEPTED", "REQUESTED"] as const;

export function SessionsPage({ customerId }: { customerId?: string }) {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = useSessionColumns();
  const states: Record<string, string> = t.sessions.states;

  return (
    <LogListPage
      resource="sessions"
      schema={sessionListSchema}
      labels={t.sessions}
      icon="bolt"
      columns={customerId ? columns.filter((column) => column.key !== "customer") : columns}
      rowKey={(row) => `${row.country}-${row.id}`}
      rowHref={(row) => scopedHref(`/sessions/${row.country}/${row.id}`)}
      filters={[
        {
          key: "state",
          label: t.sessions.stateLabel,
          allLabel: t.sessions.allStates,
          options: STATES.map((value) => ({ value, label: states[value] })),
        },
      ]}
      fixed={customerId ? { customerId } : undefined}
      embedded={customerId !== undefined}
    />
  );
}
