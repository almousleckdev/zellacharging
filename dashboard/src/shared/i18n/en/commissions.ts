import type { Dictionary } from "../types";

export const enCommissions: Pick<Dictionary, "commissions"> = {
  commissions: {
    title: "Commissions",
    subtitle: "Zella's commission per customer.",
    results: "{count} customers",
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
      customer: "Customer",
      charges: "Charges",
      electric: "Electricity",
      commission: "Commission",
      vat: "VAT",
      total: "Total taken",
      last: "Last charge",
    },
  },
};
