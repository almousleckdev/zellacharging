"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";

export type FreeAccessCategory = "NDZELA_STAFF" | "OTHER";

export type FreeAccessRequest = { reason: string; category: FreeAccessCategory; expiresAt: string | null };

export function useFreeAccess(country: string, id: string) {
  const client = useQueryClient();
  const path = `customers/${encodeURIComponent(country)}/${encodeURIComponent(id)}/complimentary`;
  const refresh = () => client.invalidateQueries({ queryKey: ["admin", "scoped"] });

  const give = useMutation({
    mutationFn: (request: FreeAccessRequest) =>
      adminFetch(path, z.null(), {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(request),
      }),
    onSuccess: refresh,
  });
  const remove = useMutation({
    mutationFn: () => adminFetch(path, z.null(), { method: "DELETE" }),
    onSuccess: refresh,
  });
  return { give, remove };
}
