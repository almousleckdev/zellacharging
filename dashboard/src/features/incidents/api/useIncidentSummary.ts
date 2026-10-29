"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { incidentListSchema } from "../schemas";

export function useIncidentSummary(country: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "incident-summary"),
    queryFn: () => adminFetch(`incidents?country=${encodeURIComponent(country)}&size=1`, incidentListSchema),
    refetchInterval: 60_000,
  });
}
