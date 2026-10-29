"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { systemSchema, tariffsSchema } from "../schemas";

export function useTariffs(country: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "tariffs"),
    queryFn: () => adminFetch(`tariffs?country=${encodeURIComponent(country)}`, tariffsSchema),
  });
}

export function useSystem(country: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "system"),
    queryFn: () => adminFetch("system", systemSchema),
    refetchInterval: 30_000,
  });
}
