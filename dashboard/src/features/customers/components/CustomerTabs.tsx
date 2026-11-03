"use client";

import { SessionsPage } from "@/features/sessions/components/SessionsPage";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import { PillTabs } from "@/shared/ui/PillTabs";
import type { CustomerDetail } from "../schemas";
import { CustomerActivityTab } from "./CustomerActivityTab";
import { CustomerOverviewTab } from "./CustomerOverviewTab";
import { CustomerWalletTab } from "./CustomerWalletTab";

type TabId = "overview" | "sessions" | "wallet" | "activity";

const TABS: { id: TabId; permission?: string }[] = [
  { id: "overview" },
  { id: "sessions", permission: "SESSIONS_READ" },
  { id: "wallet" },
  { id: "activity", permission: "AUDIT_READ" },
];

const RESET = { q: null, from: null, to: null, page: null, size: null, sort: null, state: null, kind: null };

export function CustomerTabs({ customer }: { customer: CustomerDetail }) {
  const { t } = useI18n();
  const { profile } = useSession();
  const { params, update } = useUrlParams();
  const visible = TABS.filter((tab) => !tab.permission || profile.permissions.includes(tab.permission));
  const requested = params.get("tab");
  const active = visible.find((tab) => tab.id === requested)?.id ?? "overview";
  const labels: Record<string, string> = t.customers.tabs;

  return (
    <section className="flex flex-col gap-6">
      <div>
        <PillTabs
          label={t.customers.detail.sections}
          options={visible.map((tab) => ({ value: tab.id, label: labels[tab.id] }))}
          value={active}
          onChange={(id) => update({ ...RESET, tab: id === "overview" ? null : id })}
        />
      </div>
      {active === "overview" && <CustomerOverviewTab customer={customer} />}
      {active === "sessions" && <SessionsPage customerId={customer.id} />}
      {active === "wallet" && (
        <CustomerWalletTab country={customer.country} id={customer.id} currency={customer.wallet?.currency ?? "XAF"} decimals={customer.wallet?.decimals ?? 0} wallet={customer.wallet} />
      )}
      {active === "activity" && <CustomerActivityTab country={customer.country} id={customer.id} />}
    </section>
  );
}
