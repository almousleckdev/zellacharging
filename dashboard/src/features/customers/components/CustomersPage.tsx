"use client";

import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { customerListSchema } from "../schemas";
import { CreateCustomerButton } from "./CreateCustomerButton";
import { CustomerActions } from "./CustomerActions";
import { useCustomerColumns } from "./useCustomerColumns";

const STATUSES = ["ACTIVE", "BLOCKED", "PENDING_VERIFICATION", "PENDING_PHONE"] as const;

export function CustomersPage() {
  const { t } = useI18n();
  const { scopedHref } = useSession();
  const columns = useCustomerColumns();
  const statuses: Record<string, string> = t.customers.statuses;

  return (
    <LogListPage
      resource="customers"
      schema={customerListSchema}
      labels={t.customers}
      icon="users"
      headerActions={<CreateCustomerButton />}
      columns={columns}
      rowKey={(row) => `${row.country}-${row.id}`}
      rowActions={{
        key: "actions",
        header: "",
        align: "right",
        cell: (row) => (
          <div onClick={(event) => event.stopPropagation()}>
            <CustomerActions customer={row} compact />
          </div>
        ),
      }}
      rowHref={(row) => scopedHref(`/customers/${row.country}/${row.id}`)}
      filters={[
        {
          key: "status",
          label: t.customers.statusLabel,
          allLabel: t.customers.allStatuses,
          options: STATUSES.map((value) => ({ value, label: statuses[value] })),
        },
      ]}
    />
  );
}
