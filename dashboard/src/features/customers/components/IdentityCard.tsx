"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { Card } from "@/shared/ui/Card";
import { DataTable } from "@/shared/ui/DataTable";
import type { CustomerDetail } from "../schemas";
import { useIdentityColumns } from "./useIdentityColumns";

export function IdentityCard({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const columns = useIdentityColumns();

  return (
    <Card title={t.customers.detail.identity} flush>
      <DataTable caption={t.customers.detail.identity} columns={columns} rows={[customer]} rowKey={(row) => row.id} />
    </Card>
  );
}
