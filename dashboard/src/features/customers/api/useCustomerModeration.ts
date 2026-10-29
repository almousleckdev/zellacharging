"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";

export type ModerationAction = "suspend" | "reactivate" | "delete";

export function useCustomerModeration(country: string, id: string) {
  const client = useQueryClient();
  const base = `customers/${encodeURIComponent(country)}/${encodeURIComponent(id)}`;

  return useMutation({
    mutationFn: (action: ModerationAction) =>
      action === "delete"
        ? adminFetch(base, z.null(), { method: "DELETE" })
        : adminFetch(`${base}/${action}`, z.null(), { method: "POST" }),
    onSuccess: () => client.invalidateQueries({ queryKey: ["admin", "scoped"] }),
  });
}
