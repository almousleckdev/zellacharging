"use client";

import { useSession } from "@/features/session/SessionProvider";
import { sessionDigestSchema } from "@/features/sessions/schemas";
import { useSessionDigestColumns } from "@/features/sessions/components/useSessionDigestColumns";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";

export function PaymentsPage() {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = useSessionDigestColumns();

  return (
    <LogListPage
      resource="payments/by-customer"
      exportName="payments"
      schema={sessionDigestSchema}
      labels={t.payments}
      icon="wallet"
      columns={columns}
      rowKey={(row) => `${row.country}-${row.customerId}`}
      rowHref={(row) => scopedHref(`/customers/${row.country}/${row.customerId}?tab=sessions`)}
    />
  );
}
