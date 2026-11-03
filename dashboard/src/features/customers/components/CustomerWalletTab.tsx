"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { LogListPage } from "@/shared/lists/LogListPage";
import { ledgerListSchema } from "../historySchemas";
import type { CustomerDetail } from "../schemas";
import { useLedgerColumns } from "./useLedgerColumns";
import { WalletCard } from "./WalletCard";

type CustomerWalletTabProps = { country: string; id: string; currency: string; decimals: number; wallet: CustomerDetail["wallet"] };

export function CustomerWalletTab({ country, id, currency, decimals, wallet }: CustomerWalletTabProps) {
  const { t } = useI18n();
  const columns = useLedgerColumns(country, currency, decimals);

  return (
    <div className="flex flex-col gap-6">
      {wallet && <WalletCard wallet={wallet} />}
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
    </div>
  );
}
