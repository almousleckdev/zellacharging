"use client";

import { useI18n } from "@/shared/i18n/I18nProvider";
import { useFormatters } from "@/shared/i18n/useFormatters";
import { Stat } from "@/shared/ui/Stat";
import type { CustomerDetail } from "../schemas";
import { IdentityCard } from "./IdentityCard";
import { WalletCard } from "./WalletCard";

export function CustomerOverviewTab({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const format = useFormatters();
  const labels = t.customers.detail;
  const { wallet, sessions } = customer;
  const currency = wallet?.currency ?? "XAF";
  const decimals = wallet?.decimals ?? 0;
  const money = (minor: number) => format.money([{ currency, decimals, minor }])[0];

  return (
    <div className="flex flex-col gap-6">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
        <Stat label={labels.spent} value={money(sessions.spentMinor)} />
        <Stat label={labels.completed} value={format.number(sessions.completed)} hint={sessions.live > 0 ? labels.live : undefined} />
        <Stat label={labels.energy} value={`${format.number(sessions.energyKwh, 1)} ${t.overview.unitKwh}`} />
        <Stat label={labels.balance} value={wallet ? money(wallet.balanceMinor) : t.common.none} />
        <Stat label={labels.lastSession} value={sessions.lastSessionAt ? format.date(sessions.lastSessionAt) : labels.never} />
      </div>
      <div className="grid items-start gap-6 lg:grid-cols-2">
        <IdentityCard customer={customer} />
        <WalletCard wallet={wallet} />
      </div>
    </div>
  );
}
