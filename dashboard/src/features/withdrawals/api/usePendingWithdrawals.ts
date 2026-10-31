"use client";

import { useQuery } from "@tanstack/react-query";
import { paymentListSchema } from "@/features/payments/schemas";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";

export function usePendingWithdrawals(country: string, enabled: boolean, size = 1) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "withdrawals-pending", String(size)),
    queryFn: () => adminFetch(`payments?country=${encodeURIComponent(country)}&kind=WITHDRAWAL&state=PENDING_APPROVAL&size=${size}`, paymentListSchema),
    refetchInterval: 60_000,
    enabled,
  });
}
