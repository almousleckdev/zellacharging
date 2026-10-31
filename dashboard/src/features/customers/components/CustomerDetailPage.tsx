"use client";

import { useParams, useRouter } from "next/navigation";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { DetailShell } from "@/shared/ui/DetailShell";
import { useCustomer } from "../api/useCustomer";
import { CustomerActions } from "./CustomerActions";
import { CustomerTabs } from "./CustomerTabs";
import { CustomerStatusBadge } from "./CustomerStatusBadge";

export function CustomerDetailPage() {
  const { t } = useI18n();
  const router = useRouter();
  const { scopedHref } = useSession();
  const params = useParams<{ country: string; id: string }>();
  const customer = useCustomer(params.country, params.id);
  const labels = t.customers.detail;
  const data = customer.data;
  const name = data ? `${data.firstName} ${data.lastName}` : t.customers.title;

  return (
    <DetailShell
      backHref="/customers"
      backLabel={labels.back}
      title={name}
      icon="users"
      query={customer}
      notFound={labels.notFound}
      loadError={labels.loadError}
      badge={data && <CustomerStatusBadge status={data.status} />}
      actions={data && <CustomerActions customer={data} afterDelete={() => router.replace(scopedHref("/customers"))} />}
    >
      {data && <CustomerTabs customer={data} />}
    </DetailShell>
  );
}
