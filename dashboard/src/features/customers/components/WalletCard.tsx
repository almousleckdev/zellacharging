"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Card } from "@/shared/ui/Card";
import { FactList } from "@/shared/ui/FactList";
import type { CustomerDetail } from "../schemas";

export function WalletCard({ wallet }: { wallet: CustomerDetail["wallet"] }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.customers.detail;

  if (!wallet) {
    return (
      <Card title={labels.wallet}>
        <p className="text-sm text-muted">{labels.noWallet}</p>
      </Card>
    );
  }
  const money = (minor: number) => format.money([{ currency: wallet.currency, decimals: wallet.decimals, minor }])[0];

  return (
    <Card title={labels.wallet}>
      <FactList
        facts={[
          { label: labels.balance, value: <span className="text-base font-semibold">{money(wallet.balanceMinor)}</span> },
          { label: labels.reserved, value: money(wallet.reservedMinor) },
          { label: labels.topups, value: money(wallet.topupsSucceededMinor) },
          { label: labels.pendingTopups, value: format.number(wallet.pendingTopups) },
          { label: labels.withdrawn, value: money(wallet.withdrawalsPaidMinor) },
        ]}
      />
    </Card>
  );
}
