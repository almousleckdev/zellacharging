"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { z } from "zod";
import { adminFetch } from "@/shared/api/adminFetch";

type Review = { country: string; key: string; status: "ACKNOWLEDGED" | "RESOLVED"; note: string };

export function useReviewIncident() {
  const client = useQueryClient();

  return useMutation({
    mutationFn: ({ country, ...body }: Review) =>
      adminFetch(`incidents/${encodeURIComponent(country)}/review`, z.null(), {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      }),
    onSuccess: () => client.invalidateQueries({ queryKey: ["admin", "scoped"] }),
  });
}
