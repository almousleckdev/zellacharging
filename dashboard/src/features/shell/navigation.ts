import type { IconName } from "@/shared/ui/Icon";

export type NavId =
  | "overview"
  | "stations"
  | "sessions"
  | "incidents"
  | "customers"
  | "payments"
  | "withdrawals"
  | "commissions"
  | "tariffs"
  | "reports"
  | "countries"
  | "users"
  | "audit"
  | "system";

export type NavGroupId = "operations" | "customersFinance" | "insights" | "administration";

export type NavItem = {
  id: NavId;
  href: string;
  icon: IconName;
  permission?: string;
};

export type NavGroup = {
  id: NavGroupId;
  items: NavItem[];
};

export const NAV_GROUPS: NavGroup[] = [
  {
    id: "operations",
    items: [
      { id: "overview", href: "/", icon: "overview", permission: "OVERVIEW_READ" },
      { id: "stations", href: "/stations", icon: "station", permission: "STATIONS_READ" },
      { id: "sessions", href: "/sessions", icon: "bolt", permission: "SESSIONS_READ" },
      { id: "incidents", href: "/incidents", icon: "bell", permission: "OVERVIEW_READ" },
    ],
  },
  {
    id: "customersFinance",
    items: [
      { id: "customers", href: "/customers", icon: "users", permission: "CUSTOMERS_READ" },
      { id: "payments", href: "/payments", icon: "wallet", permission: "PAYMENTS_READ" },
      { id: "withdrawals", href: "/withdrawals", icon: "clipboard", permission: "WITHDRAWALS_REVIEW" },
      { id: "commissions", href: "/commissions", icon: "percent", permission: "PAYMENTS_READ" },
      { id: "tariffs", href: "/tariffs", icon: "tag", permission: "OVERVIEW_READ"},
    ],
  },
  {
    id: "insights",
    items: [{ id: "reports", href: "/reports", icon: "chart", permission: "PAYMENTS_READ"}],
  },
  {
    id: "administration",
    items: [
      { id: "countries", href: "/countries", icon: "globe", permission: "OVERVIEW_READ"},
      { id: "users", href: "/users", icon: "shield", permission: "ADMINS_MANAGE" },
      { id: "audit", href: "/audit", icon: "clipboard", permission: "AUDIT_READ" },
      { id: "system", href: "/system", icon: "sliders", permission: "ADMINS_MANAGE"},
    ],
  },
];
