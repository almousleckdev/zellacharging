"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { sessionDigestSchema } from "../schemas";
import { CollectionBand } from "./CollectionBand";
import { useSessionDigestColumns } from "./useSessionDigestColumns";

const STATES = ["ACTIVE", "COMPLETED", "FAILED", "ABANDONED", "STOPPING", "START_ACCEPTED", "REQUESTED"] as const;

export function SessionsByCustomerPage({ stationId }: { stationId?: string }) {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = useSessionDigestColumns();
  const states: Record<string, string> = t.sessions.states;

  return (
    <LogListPage
      resource="sessions/by-customer"
      exportName="sessions"
      schema={sessionDigestSchema}
      labels={t.sessions}
      icon="bolt"
      columns={columns}
      rowKey={(row) => `${row.country}-${row.customerId}`}
      rowHref={(row) => scopedHref(`/customers/${row.country}/${row.customerId}?tab=sessions`)}
      filters={[{ key: "state", label: t.sessions.stateLabel, allLabel: t.sessions.allStates, options: STATES.map((value) => ({ value, label: states[value] })) }]}
      fixed={stationId ? { station: stationId } : undefined}
      searchable={stationId === undefined}
      above={() => <CollectionBand />}
      embedded={stationId !== undefined}
    />
  );
}
