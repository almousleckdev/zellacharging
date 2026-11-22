"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { freeWindowSchema } from "../schemas";
import type { FreeWindow } from "../schemas";

export const WINDOW_KEY = ["admin", "free-charging", "window"] as const;

export function useFreeWindow() {
  return useQuery({ queryKey: WINDOW_KEY, queryFn: () => adminFetch("free-charging/window", freeWindowSchema) });
}

export function useChangeWindow() {
  const client = useQueryClient();
  return useMutation({
    mutationFn: (window: FreeWindow) =>
      adminFetch("free-charging/window", freeWindowSchema, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(window),
      }),
    onSuccess: (saved) => client.setQueryData(WINDOW_KEY, saved),
  });
}
