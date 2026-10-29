"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { paymentListSchema } from "../schemas";
import { usePaymentColumns } from "./usePaymentColumns";

const KINDS = ["TOPUP", "WITHDRAWAL"] as const;
const STATES = ["SUCCEEDED", "PAID", "PENDING", "PROCESSING", "REQUESTED", "REVIEW", "FAILED"] as const;

export function PaymentsPage({ customerId }: { customerId?: string }) {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = usePaymentColumns();
  const kinds: Record<string, string> = t.payments.kinds;
  const states: Record<string, string> = t.payments.states;

  return (
    <LogListPage
      resource="payments"
      schema={paymentListSchema}
      labels={t.payments}
      icon="wallet"
      columns={customerId ? columns.filter((column) => column.key !== "customer") : columns}
      rowKey={(row) => `${row.kind}-${row.id}`}
      rowHref={(row) => scopedHref(`/payments/${row.country}/${row.kind}/${row.id}`)}
      filters={[
        { key: "kind", label: t.payments.kindLabel, allLabel: t.payments.allKinds, options: KINDS.map((value) => ({ value, label: kinds[value] })) },
        { key: "state", label: t.payments.stateLabel, allLabel: t.payments.allStates, options: STATES.map((value) => ({ value, label: states[value] })) },
      ]}
      fixed={customerId ? { customerId } : undefined}
      embedded={customerId !== undefined}
    />
  );
}
