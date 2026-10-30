"use client";

import { useSession } from "@/features/session/SessionProvider";
import { paymentListSchema } from "@/features/payments/schemas";
import { usePaymentColumns } from "@/features/payments/components/usePaymentColumns";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { DecisionButtons } from "./DecisionButtons";

const FIXED = { kind: "WITHDRAWAL", state: "PENDING_APPROVAL" };

export function WithdrawalsPage() {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = usePaymentColumns().filter((column) => column.key !== "state");

  return (
    <LogListPage
      resource="payments"
      schema={paymentListSchema}
      labels={t.withdrawals}
      icon="clipboard"
      columns={columns}
      rowKey={(row) => row.id}
      rowHref={(row) => scopedHref(`/payments/${row.country}/${row.kind}/${row.id}`)}
      fixed={FIXED}
      withRange={false}
      exportName="withdrawals"
      rowActions={{ key: "decision", header: "", align: "right", cell: (row) => <DecisionButtons withdrawal={row} /> }}
    />
  );
}
