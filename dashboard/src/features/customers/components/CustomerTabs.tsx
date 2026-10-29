"use client";

import { PaymentsPage } from "@/features/payments/components/PaymentsPage";
import { SessionsPage } from "@/features/sessions/components/SessionsPage";
import { useSession } from "@/features/session/SessionProvider";
import { useI18n } from "@/shared/i18n/I18nProvider";
import { cn } from "@/shared/lib/cn";
import { useUrlParams } from "@/shared/lib/useUrlParams";
import type { CustomerDetail } from "../schemas";
import { CustomerActivityTab } from "./CustomerActivityTab";
import { CustomerOverviewTab } from "./CustomerOverviewTab";
import { CustomerWalletTab } from "./CustomerWalletTab";

type TabId = "overview" | "sessions" | "payments" | "wallet" | "activity";

const TABS: { id: TabId; permission?: string }[] = [
  { id: "overview" },
  { id: "sessions", permission: "SESSIONS_READ" },
  { id: "payments", permission: "PAYMENTS_READ" },
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
      <div role="tablist" aria-label={t.customers.detail.sections} className="flex gap-6 overflow-x-auto overflow-y-hidden border-b border-border">
        {visible.map((tab) => (
          <button
            key={tab.id}
            role="tab"
            type="button"
            aria-selected={active === tab.id}
            onClick={() => update({ ...RESET, tab: tab.id === "overview" ? null : tab.id })}
            className={cn(
              "-mb-px whitespace-nowrap border-b-2 px-1 pb-3 text-sm font-medium",
              active === tab.id ? "border-primary text-foreground" : "border-transparent text-muted hover:text-foreground",
            )}
          >
            {labels[tab.id]}
          </button>
        ))}
      </div>
      {active === "overview" && <CustomerOverviewTab customer={customer} />}
      {active === "sessions" && <SessionsPage customerId={customer.id} />}
      {active === "payments" && <PaymentsPage customerId={customer.id} />}
      {active === "wallet" && (
        <CustomerWalletTab country={customer.country} id={customer.id} currency={customer.wallet?.currency ?? "XAF"} decimals={customer.wallet?.decimals ?? 0} />
      )}
      {active === "activity" && <CustomerActivityTab country={customer.country} id={customer.id} />}
    </section>
  );
}
