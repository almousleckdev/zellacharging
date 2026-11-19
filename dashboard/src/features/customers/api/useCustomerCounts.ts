"use client";

import { useQuery } from "@tanstack/react-query";
import { adminFetch } from "@/shared/api/adminFetch";
import { queryKeys } from "@/shared/api/queryKeys";
import { customerCountsSchema } from "../countsSchema";

export function useCustomerCounts(country: string) {
  return useQuery({
    queryKey: queryKeys.scoped(country, "customer-counts"),
    queryFn: () => adminFetch(`customer-counts?${new URLSearchParams({ country }).toString()}`, customerCountsSchema),
    refetchInterval: 15_000,
  });
}
