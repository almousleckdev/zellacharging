"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { type Period, periodStart } from "../periods";
import { staffUsageSchema } from "../schemas";

export function useStaffUsage(country: string, period: Period) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "staff-usage", period),
    queryFn: () => {
      const query = new URLSearchParams({ country, category: "NDZELA_STAFF" });
      const from = periodStart(period);
      if (from) {
        query.set("from", from);
      }
      return adminFetch(`complimentary/usage?${query.toString()}`, staffUsageSchema);
    },
    refetchInterval: 60_000,
  });
}
