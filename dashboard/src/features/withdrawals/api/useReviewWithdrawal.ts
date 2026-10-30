"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";

export type WithdrawalDecision = { country: string; id: string; action: "approve" | "reject"; note: string };

export function useReviewWithdrawal() {
  const client = useQueryClient();

  return useMutation({
    mutationFn: ({ country, id, action, note }: WithdrawalDecision) =>
      adminFetch(`withdrawals/${encodeURIComponent(country)}/${encodeURIComponent(id)}/${action}`, z.null(), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ note }),
      }),
    onSuccess: () => client.invalidateQueries({ queryKey: ["admin"] }),
  });
}
