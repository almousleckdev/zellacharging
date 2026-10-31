import type { Dictionary } from "../types";

export const enWithdrawals: Pick<Dictionary, "withdrawals"> = {
  withdrawals: {
    title: "Withdrawals to approve",
    subtitle: "Every withdrawal a customer requests waits for your approval. Nothing is sent to the operator before you agree.",
    results: "{count} withdrawals",
    loadError: "Unable to load withdrawals.",
    emptyTitle: "No withdrawal waiting",
    emptyText: "Every withdrawal has been handled.",
    searchLabel: "Search withdrawals",
    searchPlaceholder: "Reference, number or customer name",
    actions: { approve: "Approve", reject: "Decline", refund: "Refund" },
    approve: {
      title: "Approve this withdrawal?",
      description: "{amount} will be sent to {customer} on {operator} {number}.",
      confirm: "Approve and send",
      noteLabel: "Note (optional)",
      notePlaceholder: "Add a detail for the audit log",
      saved: "Withdrawal approved: the payout is on its way.",
    },
    reject: {
      title: "Decline this withdrawal?",
      description: "{amount} will go back to {customer}'s wallet. They will be notified.",
      confirm: "Decline withdrawal",
      noteLabel: "Reason for declining",
      notePlaceholder: "Explain why this withdrawal is declined",
      noteRequired: "Give the reason for declining.",
      saved: "Withdrawal declined: the amount is back in the wallet.",
    },
    refund: {
      title: "Refund this withdrawal?",
      description: "The operator did not receive this withdrawal. {amount} will go back to {customer}'s wallet. They will be notified.",
      confirm: "Refund the customer",
      noteLabel: "Reason for the refund",
      notePlaceholder: "Explain why this withdrawal is refunded",
      noteRequired: "Give the reason for the refund.",
      saved: "Withdrawal refunded: the amount is back in the wallet.",
    },
  },
};
