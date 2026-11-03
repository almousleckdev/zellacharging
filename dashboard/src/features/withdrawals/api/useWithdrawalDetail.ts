"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { withdrawalDetailSchema } from "../schemas";

export function useWithdrawalDetail(country: string, id: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "withdrawal", id),
    queryFn: () =>
      adminFetch(`withdrawals/${encodeURIComponent(country)}/${encodeURIComponent(id)}`, withdrawalDetailSchema),
  });
}
