"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { ledgerListSchema } from "../historySchemas";
import { useLedgerColumns } from "./useLedgerColumns";

type CustomerWalletTabProps = { country: string; id: string; currency: string; decimals: number };

export function CustomerWalletTab({ country, id, currency, decimals }: CustomerWalletTabProps) {
  const { t } = useI18n();
  const columns = useLedgerColumns(country, currency, decimals);

  return (
    <LogListPage
      resource={`customers/${country}/${id}/ledger`}
      exportName="wallet"
      schema={ledgerListSchema}
      labels={{ ...t.customers.ledger, title: t.customers.tabs.wallet, subtitle: "", results: "", searchLabel: "", searchPlaceholder: "" }}
      icon="wallet"
      columns={columns}
      searchable={false}
      rowKey={(row) => row.id}
      embedded
    />
  );
}
