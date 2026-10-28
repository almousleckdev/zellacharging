import type { IconName } from "@/shared/ui/Icon";

export type NavId =
  | "overview"
  | "stations"
  | "sessions"
  | "incidents"
  | "customers"
  | "payments"
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
  available: boolean;
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
      { id: "overview", href: "/", icon: "overview", available: true, permission: "OVERVIEW_READ" },
      { id: "stations", href: "/stations", icon: "station", available: false },
      { id: "sessions", href: "/sessions", icon: "bolt", available: true, permission: "SESSIONS_READ" },
      { id: "incidents", href: "/incidents", icon: "bell", available: false },
    ],
  },
  {
    id: "customersFinance",
    items: [
      { id: "customers", href: "/customers", icon: "users", available: true, permission: "CUSTOMERS_READ" },
      { id: "payments", href: "/payments", icon: "wallet", available: true, permission: "PAYMENTS_READ" },
      { id: "tariffs", href: "/tariffs", icon: "tag", available: false },
    ],
  },
  {
    id: "insights",
    items: [{ id: "reports", href: "/reports", icon: "chart", available: false }],
  },
  {
    id: "administration",
    items: [
      { id: "countries", href: "/countries", icon: "globe", available: false },
      { id: "users", href: "/users", icon: "shield", available: true, permission: "ADMINS_MANAGE" },
      { id: "audit", href: "/audit", icon: "clipboard", available: false },
      { id: "system", href: "/system", icon: "sliders", available: false },
    ],
  },
];
