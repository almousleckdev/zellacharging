"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { overviewSchema } from "../schemas";

const REFRESH_MS = 30_000;

export function useOverview(country: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "overview"),
    queryFn: () => adminFetch(`overview?country=${encodeURIComponent(country)}`, overviewSchema),
    refetchInterval: REFRESH_MS,
  });
}
