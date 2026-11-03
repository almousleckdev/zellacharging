import type { Dictionary } from "../types";

export const enPayments: Pick<Dictionary, "payments"> = {
  payments: {
    title: "Payments",
    subtitle: "What each customer paid for their charges. Click a customer to see their charges.",
    results: "{count} customers",
    loadError: "We couldn't load the payments.",
    emptyTitle: "No payments",
    emptyText: "No billed charge matches this search.",
    searchLabel: "Search payments",
    searchPlaceholder: "Station or customer name",
  },
};
