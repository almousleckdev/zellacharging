"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { usageSchema } from "../usageSchema";
import { LIVE_REFRESH_MS } from "./liveRefresh";

export type UsageScope = { station?: string; customerId?: string };

export function useUsage(country: string, scope: UsageScope) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "usage", scope.station ?? "", scope.customerId ?? ""),
    queryFn: () => {
      const query = new URLSearchParams({ country });
      if (scope.station) {
        query.set("station", scope.station);
      }
      if (scope.customerId) {
        query.set("customerId", scope.customerId);
      }
      return adminFetch(`usage?${query.toString()}`, usageSchema);
    },
    refetchInterval: LIVE_REFRESH_MS,
  });
}
