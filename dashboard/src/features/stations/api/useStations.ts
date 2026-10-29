"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { stationListSchema, stationViewSchema } from "../schemas";

export function useStations(country: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "stations"),
    queryFn: () => adminFetch(`stations?country=${encodeURIComponent(country)}`, stationListSchema),
    refetchInterval: 30_000,
  });
}

export function useStation(country: string, id: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "station", id),
    queryFn: () => adminFetch(`stations/${encodeURIComponent(id)}`, stationViewSchema),
    refetchInterval: 30_000,
  });
}
