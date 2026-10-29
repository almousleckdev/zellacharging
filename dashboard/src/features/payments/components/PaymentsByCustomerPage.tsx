"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { paymentDigestSchema } from "../schemas";
import { usePaymentDigestColumns } from "./usePaymentDigestColumns";

const KINDS = ["TOPUP", "WITHDRAWAL"] as const;
const STATES = ["SUCCEEDED", "PAID", "PENDING", "PROCESSING", "REQUESTED", "REVIEW", "FAILED"] as const;

export function PaymentsByCustomerPage() {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = usePaymentDigestColumns();
  const kinds: Record<string, string> = t.payments.kinds;
  const states: Record<string, string> = t.payments.states;

  return (
    <LogListPage
      resource="payments/by-customer"
      exportName="payments"
      schema={paymentDigestSchema}
      labels={t.payments}
      icon="wallet"
      columns={columns}
      rowKey={(row) => `${row.country}-${row.customerId}`}
      rowHref={(row) => scopedHref(`/customers/${row.country}/${row.customerId}?tab=payments`)}
      filters={[
        { key: "kind", label: t.payments.kindLabel, allLabel: t.payments.allKinds, options: KINDS.map((value) => ({ value, label: kinds[value] })) },
        { key: "state", label: t.payments.stateLabel, allLabel: t.payments.allStates, options: STATES.map((value) => ({ value, label: states[value] })) },
      ]}
    />
  );
}
