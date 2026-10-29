import type { Dictionary } from "../types";

export const enCommissions: Pick<Dictionary, "commissions"> = {
  commissions: {
    title: "Commissions",
    subtitle: "Zella's share of every billed charge.",
    explained:
      "On each charge the customer pays the electricity used plus a service commission. The commission is taken from their wallet and stays in your PVit account: it is yours.",
    results: "{count} billed charges",
    searchLabel: "Search commissions",
    searchPlaceholder: "Customer or station",
    loadError: "We couldn't load the commissions.",
    emptyTitle: "No commissions yet",
    emptyText: "Commissions appear as soon as a charge is billed.",
    recent: "Latest billed charges",
    earned: "Commission earned ({rate}%)",
    earnedHint: "On {count} billed charges",
    electric: "Electricity billed",
    electricHint: "Before commission and VAT",
    vat: "VAT",
    vatHint: "Rate applied: {rate}%",
    total: "Total taken from customers",
    totalHint: "Electricity, commission and VAT",
    columns: {
      session: "Charge",
      date: "Date",
      electric: "Electricity",
      commission: "Commission",
      vat: "VAT",
      total: "Total taken",
    },
  },
};
