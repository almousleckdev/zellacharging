"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";

export function useStopCharge() {
  const client = useQueryClient();

  return useMutation({
    mutationFn: ({ country, id }: { country: string; id: string }) =>
      adminFetch(`sessions/${encodeURIComponent(country)}/${encodeURIComponent(id)}/stop`, z.null(), { method: "POST" }),
    onSuccess: () => client.invalidateQueries({ queryKey: ["admin"] }),
  });
}
