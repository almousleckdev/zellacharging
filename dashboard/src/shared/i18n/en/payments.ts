import type { Dictionary } from "../types";

export const enPayments: Pick<Dictionary, "payments"> = {
  payments: {
    title: "Payments",
    subtitle: "Wallet top-ups and withdrawals, newest first.",
    results: "{count} operations",
    loadError: "We couldn't load the payments.",
    emptyTitle: "No payments",
    emptyText: "No operation matches these filters.",
    searchLabel: "Search payments",
    searchPlaceholder: "Reference, number or customer name",
    kindLabel: "Operation type",
    allKinds: "All types",
    stateLabel: "State",
    allStates: "All states",
    kinds: { TOPUP: "Top-up", WITHDRAWAL: "Withdrawal" },
    states: {
      SUCCEEDED: "Succeeded",
      PAID: "Paid",
      PENDING: "Pending",
      PROCESSING: "Processing",
      REQUESTED: "Requested",
      REVIEW: "To review",
      FAILED: "Failed",
    },
    columns: {
      payment: "Operation",
      customer: "Customer",
      state: "State",
      amount: "Amount",
      date: "Date",
    },
  },
};
