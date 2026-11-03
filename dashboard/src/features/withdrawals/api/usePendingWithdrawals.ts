"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { withdrawalListSchema } from "../schemas";

export function usePendingWithdrawals(country: string, enabled: boolean, size = 1) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "withdrawals-pending", String(size)),
    queryFn: () => adminFetch(`withdrawals?country=${encodeURIComponent(country)}&state=PENDING_APPROVAL&size=${size}`, withdrawalListSchema),
    refetchInterval: 60_000,
    enabled,
  });
}
